package top.ialdaiaxiariyay.gtms.common.machine.part.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;

import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.api.wireless.IWirelessContainerHolder;
import top.ialdaiaxiariyay.gtms.api.wireless.WirelessContainer;
import top.ialdaiaxiariyay.gtms.utils.BigIntegerUtils;

import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

import static top.ialdaiaxiariyay.gtms.api.wireless.WirelessType.ENERGY;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessEnergyHatchPartMachine extends EnergyHatchPartMachine implements IWirelessContainerHolder {

    @Nullable
    private WirelessContainer wirelessEnergyCache;

    @Nullable
    private TickableSubscription wirelessTransferSub;

    public WirelessEnergyHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int amperage, Object... args) {
        super(holder, tier, io, amperage, args);
    }

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WirelessEnergyHatchPartMachine.class, EnergyHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // ========== 生命周期 ==========

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            // 订阅服务器 tick 进行无线能量传输
            wirelessTransferSub = subscribeServerTick(this::transferEnergyWireless);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (wirelessTransferSub != null) {
            wirelessTransferSub.unsubscribe();
            wirelessTransferSub = null;
        }
    }

    // ========== 无线传输逻辑 ==========

    private void transferEnergyWireless() {
        if (getUUID() == null || !isWorkingEnabled()) return;

        WirelessContainer wc = getWirelessContainer(ENERGY);
        if (wc == null) return;

        // 仓室硬件最大吞吐量 (EU/tick)
        long maxHardwareRate = GTValues.V[tier] * (long) amperage;

        if (io == IO.IN) {
            long capacity = energyContainer.getEnergyCapacity();
            long stored = energyContainer.getEnergyStored();
            long space = capacity - stored;
            if (space <= 0) return;

            long availableInNetwork = BigIntegerUtils.getLongValue(wc.getStorage());
            if (availableInNetwork <= 0) return;

            // 输入速率 = min(本地剩余空间, 网络可用能量, 硬件上限)
            long toTransfer = Math.min(space, Math.min(availableInNetwork, maxHardwareRate));

            long extracted = wc.removeResource(toTransfer, this, ENERGY);
            if (extracted > 0) {
                energyContainer.addEnergy(extracted);
            }
        } else if (io == IO.OUT) {
            long stored = energyContainer.getEnergyStored();
            if (stored <= 0) return;

            // 输出速率 = min(本地存量, 硬件上限)
            long toTransfer = Math.min(stored, maxHardwareRate);

            energyContainer.removeEnergy(toTransfer);
            wc.addResource(toTransfer, this, ENERGY);
        }
    }

    // ========== IWirelessContainerHolder 实现 ==========

    @Override
    @Nullable
    public UUID getUUID() {
        if (getOwnerUUID() != null) {
            return getOwnerUUID();
        }
        return null;
    }

    @Override
    @Nullable
    public WirelessContainer getWirelessContainerCache(String resourceType) {
        if (ENERGY.equals(resourceType)) {
            return wirelessEnergyCache;
        }
        return null;
    }

    @Override
    public void setWirelessContainerCache(String resourceType, @Nullable WirelessContainer container) {
        if (ENERGY.equals(resourceType)) {
            this.wirelessEnergyCache = container;
        }
    }
}

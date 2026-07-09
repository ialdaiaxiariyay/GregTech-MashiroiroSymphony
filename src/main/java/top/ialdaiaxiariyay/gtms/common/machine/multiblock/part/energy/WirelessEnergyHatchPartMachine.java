package top.ialdaiaxiariyay.gtms.common.machine.multiblock.part.energy;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;

import net.minecraft.MethodsReturnNonnullByDefault;

import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.api.wireless.IWirelessContainerHolder;
import top.ialdaiaxiariyay.gtms.api.wireless.WirelessContainer;

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

    public WirelessEnergyHatchPartMachine(BlockEntityCreationInfo info, int tier, IO io, int amperage) {
        super(info, tier, io, amperage);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
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

    private void transferEnergyWireless() {
        if (getUUID() == null || !isWorkingEnabled()) return;
        WirelessContainer wc = getWirelessContainer(ENERGY);
        if (wc == null) return;
        if (io == IO.IN) {
            long extracted = wc.removeResource(this.getMaxVoltage() * this.getAmperage(), this, ENERGY);
            if (extracted > 0) {
                energyContainer.addEnergy(extracted);
            }
        } else {
            long stored = energyContainer.getEnergyStored();
            wc.addResource(stored, this, ENERGY);
            energyContainer.removeEnergy(stored);

        }
    }

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

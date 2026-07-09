package top.ialdaiaxiariyay.gtms.common.machine.multiblock.part.mana;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.machine.trait.EnvironmentalExplosionTrait;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;
import top.ialdaiaxiariyay.gtms.api.machine.trait.NotifiableManaContainer;
import top.ialdaiaxiariyay.gtms.api.wireless.IWirelessContainerHolder;
import top.ialdaiaxiariyay.gtms.api.wireless.WirelessContainer;
import top.ialdaiaxiariyay.gtms.api.wireless.WirelessType;

import java.util.UUID;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessManaHatchPartMachine extends ManaHatchPartMachine
                                          implements IWirelessContainerHolder {
    @Nullable
    protected TickableSubscription wirelessTransferSub;

    @Nullable
    private WirelessContainer wirelessManaCache;

    public WirelessManaHatchPartMachine(BlockEntityCreationInfo info, int tier, IO io, int packetCount) {
        super(info, tier, io, packetCount);
    }

    @Override
    protected NotifiableManaContainer createManaContainer() {
        long packetSize = getPacketSizeForTier(tier);
        long capacity = getHatchManaCapacity(packetCount, packetSize);
        NotifiableManaContainer container;
        if (io == IO.OUT) {
            container = NotifiableManaContainer.emitterContainer(capacity, packetSize, packetCount);
            container.setSideOutputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
            container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        } else {
            container = NotifiableManaContainer.receiverContainer(capacity, packetSize, packetCount);
            container.setSideInputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
            container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        }
        return container;
    }

    public static long getPacketSizeForTier(int tier) {
        return GTValues.V[tier];
    }

    public static long getHatchManaCapacity(int packetCount, long packetSize) {
        return packetSize * 64L * packetCount;
    }

    public long getMaxManaPerTick() {
        return getPacketSizeForTier(tier) * packetCount;
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            wirelessTransferSub = subscribeServerTick(this::transferManaWireless);
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

    private void transferManaWireless() {
        if (getUUID() == null || !isWorkingEnabled()) return;
        WirelessContainer wc = getWirelessContainer(WirelessType.MANA);
        if (wc == null) return;
        long maxPerTick = getMaxManaPerTick();
        if (io == IO.IN) {
            long extracted = wc.removeResource(maxPerTick, this, WirelessType.MANA);
            if (extracted > 0) {
                long added = manaContainer.addMana(extracted);
                if (added < extracted) {
                    wc.addResource(extracted - added, this, WirelessType.MANA);
                }
            }
        } else {
            long stored = manaContainer.getManaStored();
            if (stored > 0) {
                long accepted = wc.addResource(stored, this, WirelessType.MANA);
                if (accepted > 0) {
                    manaContainer.removeMana(accepted);
                }
            }
        }
    }

    @Override
    @Nullable
    public UUID getUUID() {
        return getOwnerUUID();
    }

    @Override
    @Nullable
    public WirelessContainer getWirelessContainerCache(String resourceType) {
        if (WirelessType.MANA.equals(resourceType)) {
            return wirelessManaCache;
        }
        return null;
    }

    @Override
    public void setWirelessContainerCache(String resourceType, @Nullable WirelessContainer container) {
        if (WirelessType.MANA.equals(resourceType)) {
            this.wirelessManaCache = container;
        }
    }
}

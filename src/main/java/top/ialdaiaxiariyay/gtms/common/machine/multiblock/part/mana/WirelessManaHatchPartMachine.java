package top.ialdaiaxiariyay.gtms.common.machine.multiblock.part.mana;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

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
public class WirelessManaHatchPartMachine extends TieredIOPartMachine
                                          implements IExplosionMachine, IWirelessContainerHolder {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WirelessManaHatchPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    public final NotifiableManaContainer manaContainer;
    @Getter
    protected final int packetCount;
    protected TickableSubscription explosionSub;
    @Nullable
    protected TickableSubscription wirelessTransferSub;

    @Nullable
    private WirelessContainer wirelessManaCache;

    public WirelessManaHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int packetCount, Object... args) {
        super(holder, tier, io);
        this.packetCount = packetCount;
        this.manaContainer = createManaContainer();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableManaContainer createManaContainer() {
        long packetSize = getPacketSizeForTier(tier);
        long capacity = getHatchManaCapacity(packetCount, packetSize);
        NotifiableManaContainer container;
        if (io == IO.OUT) {
            container = NotifiableManaContainer.emitterContainer(this, capacity, packetSize, packetCount);
            container.setSideOutputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
            container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        } else {
            container = NotifiableManaContainer.receiverContainer(this, capacity, packetSize, packetCount);
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
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
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
            if (ConfigHolder.INSTANCE.machines.shouldWeatherOrTerrainExplosion &&
                    shouldWeatherOrTerrainExplosion()) {
                explosionSub = subscribeServerTick(this::checkExplosion);
                checkExplosion();
            }
            wirelessTransferSub = subscribeServerTick(this::transferManaWireless);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (explosionSub != null) {
            explosionSub.unsubscribe();
            explosionSub = null;
        }
        if (wirelessTransferSub != null) {
            wirelessTransferSub.unsubscribe();
            wirelessTransferSub = null;
        }
    }

    protected void checkExplosion() {
        if (manaContainer.getManaStored() > 0) {
            checkWeatherOrTerrainExplosion(tier, tier * 10);
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
                manaContainer.addMana(extracted);
            }
        } else {
            long stored = manaContainer.getManaStored();
            if (stored > 0) {
                wc.addResource(stored, this, WirelessType.MANA);
                manaContainer.removeMana(stored);
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

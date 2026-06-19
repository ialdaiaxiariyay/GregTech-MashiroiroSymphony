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
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import top.ialdaiaxiariyay.gtms.api.machine.trait.NotifiableManaContainer;
import vazkii.botania.api.mana.ManaReceiver;

import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ManaHatchPartMachine extends TieredIOPartMachine implements IExplosionMachine, ManaReceiver {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ManaHatchPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    public final NotifiableManaContainer manaContainer;
    @Getter
    protected final int packetCount;
    protected TickableSubscription explosionSub;
    protected TickableSubscription transferSub;

    public ManaHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int packetCount, Object... args) {
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
            transferSub = subscribeServerTick(this::tryTransferToAdjacent);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (explosionSub != null) {
            explosionSub.unsubscribe();
            explosionSub = null;
        }
        if (transferSub != null) {
            transferSub.unsubscribe();
            transferSub = null;
        }
    }

    protected void checkExplosion() {
        if (manaContainer.getManaStored() > 0) {
            checkWeatherOrTerrainExplosion(tier, tier * 10);
        }
    }

    private void tryTransferToAdjacent() {
        if (io != IO.OUT || !isWorkingEnabled()) return;
        Level level = getLevel();
        if (level == null) return;
        BlockPos frontPos = getPos().relative(getFrontFacing());
        var te = level.getBlockEntity(frontPos);
        if (te instanceof ManaReceiver receiver && receiver.canReceiveManaFromBursts()) {
            if (receiver.isFull()) return;
            long maxTransfer = getMaxManaPerTick();
            int toTransfer = (int) Math.min(manaContainer.getManaStored(), maxTransfer);
            if (toTransfer <= 0) return;
            receiver.receiveMana(toTransfer);
            manaContainer.removeMana(toTransfer);
        }
    }

    @Override
    public Level getManaReceiverLevel() {
        return Objects.requireNonNull(getLevel());
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getPos();
    }

    @Override
    public int getCurrentMana() {
        return (int) Math.min(manaContainer.getManaStored(), Integer.MAX_VALUE);
    }

    @Override
    public boolean isFull() {
        return manaContainer.getManaStored() >= manaContainer.getManaCapacity();
    }

    @Override
    public void receiveMana(int mana) {
        if (mana > 0 && io == IO.IN) {
            manaContainer.addMana(mana);
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return io == IO.IN && isWorkingEnabled();
    }
}

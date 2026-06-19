package top.ialdaiaxiariyay.gtms.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.api.capability.GTMSCapabilityHelper;
import top.ialdaiaxiariyay.gtms.api.capability.IManaContainer;
import top.ialdaiaxiariyay.gtms.api.capability.recipe.ManaRecipeCapability;
import top.ialdaiaxiariyay.gtms.api.recipe.ingredient.ManaStack;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class NotifiableManaContainer extends NotifiableRecipeHandlerTrait<ManaStack> implements IManaContainer {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableManaContainer.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

    @Getter
    protected IO handlerIO;
    @Getter
    @Persisted
    @DescSynced
    protected long manaStored;
    @Getter
    private final long manaCapacity;
    @Getter
    private final long inputPacketSize;
    @Getter
    private final long inputPacketCount;
    @Getter
    private final long outputPacketSize;
    @Getter
    private final long outputPacketCount;

    @Setter
    private Predicate<Direction> sideInputCondition, sideOutputCondition;

    protected long acceptedPacketsThisTick;
    protected long lastTimeStamp;
    @Nullable
    protected TickableSubscription outputSubs;
    @Nullable
    protected TickableSubscription updateSubs;

    protected long lastManaInputPerSec = 0;
    protected long lastManaOutputPerSec = 0;
    protected long manaInputPerSec = 0;
    protected long manaOutputPerSec = 0;

    public NotifiableManaContainer(MetaMachine machine, long manaCapacity,
                                   long inputPacketSize, long inputPacketCount,
                                   long outputPacketSize, long outputPacketCount) {
        super(machine);
        this.lastTimeStamp = Long.MIN_VALUE;
        this.manaCapacity = manaCapacity;
        this.inputPacketSize = inputPacketSize;
        this.inputPacketCount = inputPacketCount;
        this.outputPacketSize = outputPacketSize;
        this.outputPacketCount = outputPacketCount;

        boolean canInput = inputPacketSize > 0 && inputPacketCount > 0;
        boolean canOutput = outputPacketSize > 0 && outputPacketCount > 0;
        this.handlerIO = canInput && canOutput ? IO.BOTH : canInput ? IO.IN : canOutput ? IO.OUT : IO.NONE;
    }

    public static NotifiableManaContainer receiverContainer(MetaMachine machine, long manaCapacity,
                                                            long inputPacketSize, long inputPacketCount) {
        return new NotifiableManaContainer(machine, manaCapacity, inputPacketSize, inputPacketCount, 0L, 0L);
    }

    public static NotifiableManaContainer emitterContainer(MetaMachine machine, long manaCapacity,
                                                           long outputPacketSize, long outputPacketCount) {
        return new NotifiableManaContainer(machine, manaCapacity, 0L, 0L, outputPacketSize, outputPacketCount);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        checkOutputSubscription();
        updateSubs = getMachine().subscribeServerTick(updateSubs, this::updateTick);
    }

    @Override
    public void onMachineUnLoad() {
        super.onMachineUnLoad();
        if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    protected void checkOutputSubscription() {
        if (outputPacketSize > 0 && outputPacketCount > 0 && manaStored >= outputPacketSize) {
            outputSubs = getMachine().subscribeServerTick(outputSubs, this::serverTick);
        } else if (outputSubs != null) {
            outputSubs.unsubscribe();
            outputSubs = null;
        }
    }

    protected void setManaStored(long manaStored) {
        if (this.manaStored == manaStored) return;
        if (manaStored > this.manaStored) {
            manaInputPerSec += manaStored - this.manaStored;
        } else {
            manaOutputPerSec += this.manaStored - manaStored;
        }
        this.manaStored = manaStored;
        checkOutputSubscription();
        notifyListeners();
    }

    protected void updateTick() {
        if (getMachine().getOffsetTimer() % 20 == 0) {
            lastManaOutputPerSec = manaOutputPerSec;
            lastManaInputPerSec = manaInputPerSec;
            manaOutputPerSec = 0;
            manaInputPerSec = 0;
        }
    }

    protected void serverTick() {
        if (Objects.requireNonNull(getMachine().getLevel()).isClientSide) return;
        if (manaStored >= outputPacketSize && outputPacketSize > 0 && outputPacketCount > 0) {
            long packetsAvailable = Math.min(manaStored / outputPacketSize, outputPacketCount);
            if (packetsAvailable == 0) return;
            long packetsUsed = 0;
            for (Direction side : GTUtil.DIRECTIONS) {
                if (!outputsMana(side)) continue;
                var oppositeSide = side.getOpposite();
                var manaContainer = GTMSCapabilityHelper.getManaContainer(getMachine().getLevel(),
                        getMachine().getPos().relative(side), oppositeSide);
                if (manaContainer != null && manaContainer.inputsMana(oppositeSide)) {
                    packetsUsed += manaContainer.acceptManaFromNetwork(oppositeSide, outputPacketSize,
                            packetsAvailable - packetsUsed);
                    if (packetsUsed >= packetsAvailable) break;
                }
            }
            if (packetsUsed > 0) {
                setManaStored(manaStored - packetsUsed * outputPacketSize);
            }
        }
    }

    @Override
    public long acceptManaFromNetwork(Direction side, long manaPerPacket, long packetCount) {
        long currentTick = getMachine().getOffsetTimer();
        if (lastTimeStamp < currentTick) {
            acceptedPacketsThisTick = 0;
            lastTimeStamp = currentTick;
        }
        if (acceptedPacketsThisTick >= inputPacketCount) return 0;

        long canAcceptMana = manaCapacity - manaStored;
        if (manaPerPacket > 0 && (side == null || inputsMana(side))) {
            // Explosion if packet size exceeds maximum allowed
            if (manaPerPacket > inputPacketSize && getMachine() instanceof IExplosionMachine explosionMachine) {
                explosionMachine.doExplosion(GTUtil.getExplosionPower(manaPerPacket));
                return Math.min(packetCount, inputPacketCount - acceptedPacketsThisTick);
            }
            if (canAcceptMana >= manaPerPacket) {
                long packetsAccepted = Math.min(canAcceptMana / manaPerPacket,
                        Math.min(packetCount, inputPacketCount - acceptedPacketsThisTick));
                if (packetsAccepted > 0) {
                    setManaStored(manaStored + manaPerPacket * packetsAccepted);
                    acceptedPacketsThisTick += packetsAccepted;
                    return packetsAccepted;
                }
            }
        }
        return 0;
    }

    @Override
    public boolean inputsMana(Direction side) {
        return inputPacketSize > 0 &&
                (sideInputCondition == null || sideInputCondition.test(side));
    }

    @Override
    public boolean outputsMana(Direction side) {
        return outputPacketSize > 0 &&
                (sideOutputCondition == null || sideOutputCondition.test(side));
    }

    @Override
    public long changeMana(long differenceAmount) {
        long oldMana = manaStored;
        long newMana = Math.min(manaCapacity, Math.max(0, oldMana + differenceAmount));
        setManaStored(newMana);
        return newMana - oldMana;
    }

    @Override
    public List<ManaStack> handleRecipeInner(IO io, GTRecipe recipe, @NotNull List<ManaStack> left, boolean simulate) {
        for (var it = left.listIterator(); it.hasNext();) {
            ManaStack stack = it.next();
            if (stack.isEmpty()) {
                it.remove();
                continue;
            }

            long totalMana = stack.getTotalMana();
            long canTransfer = Math.min(totalMana, (io == IO.IN ? manaStored : manaCapacity - manaStored));
            if (!simulate) {
                changeMana(io == IO.IN ? -canTransfer : canTransfer);
            }

            totalMana -= canTransfer;
            if (totalMana <= 0) {
                it.remove();
            } else {
                it.set(new ManaStack(totalMana));
            }
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    public @NotNull List<Object> getContents() {
        return Collections.singletonList(new ManaStack(manaStored));
    }

    @Override
    public double getTotalContentAmount() {
        return manaStored;
    }

    @Override
    public RecipeCapability<ManaStack> getCapability() {
        return ManaRecipeCapability.CAP;
    }
}

package top.ialdaiaxiariyay.gtbss.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.trait.EnvironmentalExplosionTrait;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtbss.api.capability.IManaContainer;
import top.ialdaiaxiariyay.gtbss.api.capability.recipe.ManaRecipeCapability;
import top.ialdaiaxiariyay.gtbss.api.recipe.ingredient.ManaStack;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NotifiableManaContainer extends NotifiableRecipeHandlerTrait<ManaStack> implements IManaContainer {

    @Getter
    protected IO handlerIO;
    @Getter
    @SaveField
    @SyncToClient
    protected long manaStored;
    @Getter
    private long manaCapacity;
    @Getter
    private long inputPacketSize;      // Mana per input packet
    @Getter
    private long inputPacketCount;     // max input packets per tick
    @Getter
    private long outputPacketSize;     // Mana per output packet
    @Getter
    private long outputPacketCount;    // max output packets per tick

    @Setter
    private @Nullable Predicate<Direction> sideInputCondition, sideOutputCondition;

    protected long packetsReceivedThisTick;
    protected long lastTimeStamp;
    @Nullable
    protected TickableSubscription outputSubs;
    @Nullable
    protected TickableSubscription updateSubs;

    protected long lastManaInputPerSec = 0;
    protected long lastManaOutputPerSec = 0;
    protected long manaInputPerSec = 0;
    protected long manaOutputPerSec = 0;

    // Constructors
    public NotifiableManaContainer(long maxCapacity,
                                   long maxInputPacketSize,
                                   long maxInputPacketCount,
                                   long maxOutputPacketSize,
                                   long maxOutputPacketCount) {
        super();
        this.lastTimeStamp = Long.MIN_VALUE;
        this.manaCapacity = maxCapacity;
        this.inputPacketSize = maxInputPacketSize;
        this.inputPacketCount = maxInputPacketCount;
        this.outputPacketSize = maxOutputPacketSize;
        this.outputPacketCount = maxOutputPacketCount;
        var isIn = (inputPacketSize != 0 && inputPacketCount != 0);
        var isOut = (outputPacketSize != 0 && outputPacketCount != 0);
        this.handlerIO = (isIn && isOut) ? IO.BOTH : isIn ? IO.IN : isOut ? IO.OUT : IO.NONE;
    }

    public static NotifiableManaContainer emitterContainer(long maxCapacity,
                                                           long maxOutputPacketSize,
                                                           long maxOutputPacketCount) {
        return new NotifiableManaContainer(maxCapacity, 0L, 0L,
                maxOutputPacketSize, maxOutputPacketCount);
    }

    public static NotifiableManaContainer receiverContainer(long maxCapacity,
                                                            long maxInputPacketSize,
                                                            long maxInputPacketCount) {
        return new NotifiableManaContainer(maxCapacity, maxInputPacketSize, maxInputPacketCount, 0L, 0L);
    }

    public void resetBasicInfo(long maxCapacity,
                               long maxInputPacketSize,
                               long maxInputPacketCount,
                               long maxOutputPacketSize,
                               long maxOutputPacketCount) {
        this.manaCapacity = maxCapacity;
        this.inputPacketSize = maxInputPacketSize;
        this.inputPacketCount = maxInputPacketCount;
        this.outputPacketSize = maxOutputPacketSize;
        this.outputPacketCount = maxOutputPacketCount;
        var isIn = (inputPacketSize != 0 && inputPacketCount != 0);
        var isOut = (outputPacketSize != 0 && outputPacketCount != 0);
        this.handlerIO = (isIn && isOut) ? IO.BOTH : isIn ? IO.IN : isOut ? IO.OUT : IO.NONE;
        checkOutputSubscription();
    }

    // Lifecycle
    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        checkOutputSubscription();
        updateSubs = getMachine().subscribeServerTick(updateSubs, this::updateTick);
    }

    @Override
    public void onMachineUnload() {
        super.onMachineUnload();
        if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    // Output scheduling
    public void checkOutputSubscription() {
        if (getOutputPacketSize() > 0 && getOutputPacketCount() > 0) {
            if (getManaStored() >= getOutputPacketSize()) {
                outputSubs = getMachine().subscribeServerTick(outputSubs, this::serverTick);
            } else if (outputSubs != null) {
                outputSubs.unsubscribe();
                outputSubs = null;
            }
        }
    }

    // IManaInfoProvider stats
    @Override
    public long getInputPerSec() {
        return lastManaInputPerSec;
    }

    @Override
    public long getOutputPerSec() {
        return lastManaOutputPerSec;
    }

    // Internal state
    protected void setManaStored(long manaStored) {
        if (this.manaStored == manaStored) return;
        if (manaStored > this.manaStored) {
            manaInputPerSec += manaStored - this.manaStored;
        } else {
            manaOutputPerSec += this.manaStored - manaStored;
        }
        this.manaStored = manaStored;
        syncDataHolder.markClientSyncFieldDirty("manaStored");
        checkOutputSubscription();
        notifyListeners();
    }

    public void updateTick() {
        if (getMachine().getOffsetTimer() % 20 == 0) {
            lastManaOutputPerSec = manaOutputPerSec;
            lastManaInputPerSec = manaInputPerSec;
            manaOutputPerSec = 0;
            manaInputPerSec = 0;
        }
    }

    protected void serverTick() {
        if (getMachine().getLevel().isClientSide) return;
        if (getManaStored() >= getOutputPacketSize() && getOutputPacketSize() > 0 && getOutputPacketCount() > 0) {
            long packetSize = getOutputPacketSize();
            long maxPackets = Math.min(getManaStored() / packetSize, getOutputPacketCount());
            if (maxPackets == 0) return;
            long packetsSent = 0;
            for (Direction side : GTUtil.DIRECTIONS) {
                if (!outputsMana(side)) continue;
                BlockEntity neighbor = getLevel().getBlockEntity(getBlockPos().relative(side));
                if (neighbor instanceof IManaContainer target && target.inputsMana(side.getOpposite())) {
                    packetsSent += target.acceptManaFromNetwork(side.getOpposite(), packetSize,
                            maxPackets - packetsSent);
                    if (packetsSent >= maxPackets) break;
                }
            }
            if (packetsSent > 0) {
                setManaStored(getManaStored() - packetsSent * packetSize);
            }
        }
    }

    // ---- IManaContainer implementation ----

    @Override
    public long acceptManaFromNetwork(Direction side, long manaPerPacket, long packetCount) {
        var latestTimeStamp = getMachine().getOffsetTimer();
        if (lastTimeStamp < latestTimeStamp) {
            packetsReceivedThisTick = 0;
            lastTimeStamp = latestTimeStamp;
        }
        if (packetsReceivedThisTick >= getInputPacketCount()) return 0;
        long canAccept = getManaCapacity() - getManaStored();
        if (manaPerPacket > 0L && inputsMana(side)) {
            if (manaPerPacket > getInputPacketSize()) {
                var explodable = getMachine().getTrait(EnvironmentalExplosionTrait.class);
                if (explodable != null)
                    GTUtil.doExplosion(getLevel(), getBlockPos(), GTUtil.getExplosionPower(manaPerPacket));
                return Math.min(packetCount, getInputPacketCount() - packetsReceivedThisTick);
            }
            if (canAccept >= manaPerPacket) {
                long packetsAccepted = Math.min(canAccept / manaPerPacket,
                        Math.min(packetCount, getInputPacketCount() - packetsReceivedThisTick));
                if (packetsAccepted > 0) {
                    setManaStored(getManaStored() + manaPerPacket * packetsAccepted);
                    packetsReceivedThisTick += packetsAccepted;
                    return packetsAccepted;
                }
            }
        }
        return 0;
    }

    @Override
    public boolean inputsMana(Direction side) {
        return !outputsMana(side) && getInputPacketSize() > 0 &&
                (sideInputCondition == null || sideInputCondition.test(side));
    }

    @Override
    public boolean outputsMana(Direction side) {
        return getOutputPacketSize() > 0 && (sideOutputCondition == null || sideOutputCondition.test(side));
    }

    @Override
    public long changeMana(long differenceAmount) {
        long oldStored = getManaStored();
        long newStored = (manaCapacity - oldStored < differenceAmount) ? manaCapacity :
                (oldStored + differenceAmount);
        if (newStored < 0) newStored = 0;
        setManaStored(newStored);
        return newStored - oldStored;
    }

    // ---- Recipe handler ----

    @Override
    public List<ManaStack> handleRecipeInner(IO io, GTRecipe recipe, List<ManaStack> left,
                                             boolean simulate) {
        for (var it = left.listIterator(); it.hasNext();) {
            ManaStack stack = it.next();
            if (stack.isEmpty()) {
                it.remove();
                continue;
            }

            long totalMana = stack.getTotalMana();
            long canTransfer = Math.min(totalMana,
                    (io == IO.IN ? this.getManaStored() : this.getManaCapacity() - this.getManaStored()));
            if (!simulate) {
                this.changeMana(io == IO.IN ? -canTransfer : canTransfer);
            }

            totalMana -= canTransfer;
            if (totalMana <= 0) {
                it.remove();
            } else {
                it.set(new ManaStack(totalMana));
            }
        }
        return left;
    }

    @Override
    public List<Object> getContents() {
        long packetCount = Math.max(getInputPacketCount(), getOutputPacketCount());
        return Collections.singletonList(getManaStored());
    }

    @Override
    public double getTotalContentAmount() {
        return getManaStored();
    }

    @Override
    public RecipeCapability<ManaStack> getCapability() {
        return ManaRecipeCapability.CAP;
    }
}

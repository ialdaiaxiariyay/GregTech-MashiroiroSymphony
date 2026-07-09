package top.ialdaiaxiariyay.gtms.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.common.machine.trait.ProgrammableCircuitSlotTrait;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;

import javax.annotation.Nullable;
import java.util.*;

/**
 * All simple single machines are implemented here.
 */
public class SimpleTieredManaMachine extends WorkableTieredManaMachine implements SparkAttachable {

    @Nullable
    private ManaSpark attachedSpark;

    public SimpleTieredManaMachine(BlockEntityCreationInfo info, int tier, Int2IntFunction tankScalingFunction) {
        super(info, tier, false, tankScalingFunction);

        attachPersistentTrait("autoOutput", new AutoOutputTrait(List.of(exportItems), List.of(exportFluids)));
        attachPersistentTrait("circuit", new ProgrammableCircuitSlotTrait());
    }

    public SimpleTieredManaMachine(BlockEntityCreationInfo info, int tier) {
        this(info, tier, GTMachineUtils.defaultTankSizeFunction);
    }

    @Override
    public long getDisplayRecipeVoltage() {
        return GTValues.V[this.tier];
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public void attachSpark(ManaSpark spark) {
        this.attachedSpark = spark;
    }

    @Override
    public int getAvailableSpaceForMana() {
        long space = manaContainer.getManaCapacity() - manaContainer.getManaStored();
        return space > 0 ? (int) Math.min(space, Integer.MAX_VALUE) : 0;
    }

    @Override
    @Nullable
    public ManaSpark getAttachedSpark() {
        return attachedSpark;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return isFull() || !isWorkingEnabled();
    }

    @Override
    public Level getManaReceiverLevel() {
        return Objects.requireNonNull(getLevel());
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getBlockPos();
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
        if (mana > 0 && canReceiveManaFromBursts()) {
            manaContainer.addMana(mana);
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return isWorkingEnabled() && !isFull();
    }
}
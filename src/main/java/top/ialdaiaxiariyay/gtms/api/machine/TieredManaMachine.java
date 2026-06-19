package top.ialdaiaxiariyay.gtms.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.editor.EditableUI;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.api.machine.trait.NotifiableManaContainer;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;

import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TieredManaMachine extends TieredMachine implements ITieredMachine, IExplosionMachine,
                               ManaReceiver, SparkAttachable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(TieredManaMachine.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    public final NotifiableManaContainer manaContainer;

    protected TickableSubscription explosionSub;

    @Nullable
    @Getter
    protected ManaSpark attachedSpark; // 当前附着的火花

    public TieredManaMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier);
        manaContainer = createManaContainer(args);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableManaContainer createManaContainer(Object... args) {
        long tierMana = GTValues.V[tier];
        if (isManaEmitter()) {
            return NotifiableManaContainer.emitterContainer(this,
                    tierMana * 64L, tierMana, getMaxManaTransferAmperage());
        } else {
            return NotifiableManaContainer.receiverContainer(this,
                    tierMana * 64L, tierMana, getMaxManaTransferAmperage());
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && ConfigHolder.INSTANCE.machines.shouldWeatherOrTerrainExplosion &&
                shouldWeatherOrTerrainExplosion()) {
            explosionSub = subscribeServerTick(this::checkExplosion);
            checkExplosion();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (explosionSub != null) {
            explosionSub.unsubscribe();
            explosionSub = null;
        }
        attachedSpark = null;
    }

    //////////////////////////////////////
    // ******** Explosion ********//
    //////////////////////////////////////
    protected void checkExplosion() {
        if (manaContainer.getManaStored() > 0) {
            checkWeatherOrTerrainExplosion(tier, tier * 10);
        }
    }

    //////////////////////////////////////
    // ********** MISC ***********//
    //////////////////////////////////////
    @Override
    public int getAnalogOutputSignal() {
        long manaStored = manaContainer.getManaStored();
        long manaCapacity = manaContainer.getManaCapacity();
        float f = manaCapacity == 0L ? 0.0f : manaStored / (manaCapacity * 1.0f);
        return Mth.floor(f * 14.0f) + (manaStored > 0 ? 1 : 0);
    }

    /**
     * Determines max transfer rate (amperage) of mana.
     */
    protected long getMaxManaTransferAmperage() {
        return 1L;
    }

    /**
     * Determines if this machine is in mana emitter or receiver mode.
     */
    protected boolean isManaEmitter() {
        return false;
    }

    /**
     * Create a mana bar widget.
     */
    protected static EditableUI<ProgressWidget, TieredManaMachine> createManaBar() {
        return new EditableUI<>("mana_container", ProgressWidget.class, () -> {
            var progressBar = new ProgressWidget(ProgressWidget.JEIProgress, 0, 0, 18, 60,
                    new ProgressTexture(IGuiTexture.EMPTY, GuiTextures.ENERGY_BAR_BASE));
            progressBar.setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP);
            progressBar.setBackground(GuiTextures.ENERGY_BAR_BACKGROUND);
            return progressBar;
        }, (progressBar, machine) -> progressBar.setProgressSupplier(
                () -> machine.manaContainer.getManaStored() * 1d / machine.manaContainer.getManaCapacity()));
    }

    // ========== ManaReceiver 实现 ==========
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
        if (mana > 0 && !isManaEmitter()) {
            manaContainer.changeMana(mana);
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return !isManaEmitter();
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public void attachSpark(ManaSpark entity) {
        this.attachedSpark = entity;
    }

    @Override
    public int getAvailableSpaceForMana() {
        long space = manaContainer.getManaCapacity() - manaContainer.getManaStored();
        return (int) Math.min(space, Integer.MAX_VALUE);
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return false;
    }

    public void setAttachedSpark(@Nullable ManaSpark spark) {
        this.attachedSpark = spark;
    }
}

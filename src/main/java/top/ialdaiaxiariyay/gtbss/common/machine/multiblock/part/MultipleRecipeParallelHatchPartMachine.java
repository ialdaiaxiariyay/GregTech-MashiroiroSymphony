package top.ialdaiaxiariyay.gtbss.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.minecraft.util.Mth;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class MultipleRecipeParallelHatchPartMachine extends TieredPartMachine implements IMuiMachine {

    @Getter
    private int multipleRecipeParallel = 1;

    @Getter
    @SaveField
    private int minRecipeDuration = 20;

    public MultipleRecipeParallelHatchPartMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        switch (tier) {
            case GTValues.LuV -> this.multipleRecipeParallel = 4;
            case GTValues.UV -> this.multipleRecipeParallel = 8;
            case GTValues.UHV -> this.multipleRecipeParallel = 16;
            case GTValues.UEV -> this.multipleRecipeParallel = 32;
            case GTValues.UIV -> this.multipleRecipeParallel = 64;
            case GTValues.UXV -> this.multipleRecipeParallel = 128;
            case GTValues.OpV -> this.multipleRecipeParallel = 256;
            case GTValues.MAX -> this.multipleRecipeParallel = 512;
        }
    }

    public void setMinRecipeDuration(int duration) {
        this.minRecipeDuration = Mth.clamp(duration, 20, 200);
        for (MultiblockControllerMachine controller : this.getControllers()) {
            if (controller instanceof IRecipeLogicMachine rlm) {
                rlm.getRecipeLogic().markLastRecipeDirty();
            }
        }
    }

    @Override
    public boolean canShared(@NotNull MultiblockControllerMachine controller, @NotNull String substructureName) {
        return false;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        IntSyncValue duration = new IntSyncValue(this::getMinRecipeDuration, this::setMinRecipeDuration).allowC2S();
        mainWidget.child(Flow.row()
                .size(180, 60)
                .child(
                        new TextFieldWidget()
                                .width(40)
                                .setTextAlignment(Alignment.CENTER)
                                .setNumbers(20, 200)
                                .value(duration)
                                .setDefaultNumber(1)
                                .marginLeft(4)
                                .verticalCenter())
                .child(Text.lang("gtbss.machine.multiple_recipe_parallel_hatch.parallel_ui")
                        .asWidget()
                        .marginLeft(4)
                        .marginRight(4)
                        .verticalCenter()));
    }
}

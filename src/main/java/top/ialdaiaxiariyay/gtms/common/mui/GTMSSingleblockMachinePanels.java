package top.ialdaiaxiariyay.gtms.common.mui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeMachineWidget;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import top.ialdaiaxiariyay.gtms.api.machine.SimpleTieredManaMachine;

public class GTMSSingleblockMachinePanels {

    public static PanelFactory GENERAL_MACHINE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                                  MetaMachine machine) -> {

        GTRecipeType type;
        RecipeLogic recipeLogic;
        boolean isSteam = false;

        if (machine instanceof SimpleTieredManaMachine simpleTieredManaMachine) {
            type = simpleTieredManaMachine.getRecipeType();
            recipeLogic = simpleTieredManaMachine.getRecipeLogic();
        } else if (machine instanceof SimpleGeneratorMachine simpleGeneratorMachine) {
            type = simpleGeneratorMachine.getRecipeType();
            recipeLogic = simpleGeneratorMachine.recipeLogic;
        } else {
            GTCEu.LOGGER.error(
                    "{} is not a Machine, cannot add slots to its content",
                    machine.getDefinition().getName());
            return new ModularPanel<>(machine.getDefinition().getName());
        }

        if (type.getUiLayout() == null) {
            GTCEu.LOGGER.error(
                    "Tried to draw a singleblock recipe type UI for {}, but it does not have a recipe type UI",
                    machine.getDefinition().getName());
            return new ModularPanel<>(machine.getDefinition().getName());
        }

        var builder = !isSteam ? MachineUIPanelBuilder.panelBuilder(machine).drawGTLogo(true) :
                MachineUIPanelBuilder.defaultSteamMachinePanelBuilder(machine);
        return builder.mainContents(
                        (parent) -> {
                            int maxInputs = type.maxInputs.values().stream().mapToInt(Integer::intValue).max().orElse(0);
                            int maxOutputs = type.maxOutputs.values().stream().mapToInt(Integer::intValue).max().orElse(0);
                            int offset = maxOutputs - maxInputs;
                            float progressSize = type.getUiLayout().getProgressBar().progressSize();
                            int leftOffset = (int)(offset * (progressSize / 2.f + 2));
                            parent.child(
                                            new GTRecipeTypeMachineWidget(type, syncManager, machine, recipeLogic::getProgressPercent))
                                    .left(leftOffset);
                        })
                .build(syncManager, settings)
                .excludeAreaInRecipeViewer();
    };
}

package top.ialdaiaxiariyay.gtbss.api.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtbss.api.GTBSSValues;
import top.ialdaiaxiariyay.gtbss.api.capability.IManaContainer;
import top.ialdaiaxiariyay.gtbss.api.capability.recipe.ManaRecipeCapability;
import top.ialdaiaxiariyay.gtbss.api.misc.ManaContainerList;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkableManaMultiblockMachine extends WorkableMultiblockMachine
                                           implements IMuiMachine, ITieredMachine, IOverclockMachine, IVoidable {

    // runtime
    protected @Nullable ManaContainerList manaContainer;
    @Getter
    protected int tier;
    @SaveField
    @Getter
    protected boolean batchEnabled;

    public WorkableManaMultiblockMachine(BlockEntityCreationInfo info, RecipeLogic recipeLogic) {
        super(info, recipeLogic);
    }

    public WorkableManaMultiblockMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public WorkableManaMultiblockMachine self() {
        return this;
    }

    //////////////////////////////////////
    // *** Multiblock Lifecycle ***//
    //////////////////////////////////////
    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        this.manaContainer = null;
        this.tier = 0;
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        this.manaContainer = getManaContainer();
        this.tier = GTUtil.getFloorTierByVoltage(getMaxManaPacketSize());
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        this.manaContainer = null;
        this.tier = 0;
    }

    @Override
    public void setBatchEnabled(boolean batch) {
        this.batchEnabled = batch;
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    public static final int MULTI_UI_TEXT_PANEL_WIDTH = 172;
    public static final int MULTI_UI_TEXT_PANEL_HEIGHT = 136;

    public Widget<?> getMainTextPanel(PanelSyncManager syncManager) {
        var parentWidget = new ParentWidget<>();
        var listWidget = new ListWidget<>()
                .width(MULTI_UI_TEXT_PANEL_WIDTH - 6)
                .height(MULTI_UI_TEXT_PANEL_HEIGHT - 6)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .collapseDisabledChildren()
                .posRel(Alignment.CenterLeft);
        parentWidget.size(MULTI_UI_TEXT_PANEL_WIDTH, MULTI_UI_TEXT_PANEL_HEIGHT).background(GuiTextures.DISPLAY);

        listWidget.children(getWidgetsForDisplay(syncManager));
        parentWidget.child(listWidget.left(3).top(3));
        return parentWidget;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        mainWidget.child(getMainTextPanel(syncManager).margin(4, 2));
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));
        widgets.add(addManaTierLine(this, syncManager));
        widgets.add(addManaUsageLine(this, syncManager));
        widgets.addAll(super.getWidgetsForDisplay(syncManager));
        return widgets;
    }

    public static TextWidget<?> addManaTierLine(WorkableManaMultiblockMachine rlMachine,
                                                PanelSyncManager syncManager) {
        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(rlMachine::isFormed));

        IntSyncValue tier = syncManager.getOrCreateSyncHandler("manaTier", IntSyncValue.class,
                () -> new IntSyncValue(rlMachine::getTier));

        return Text.dynamic(() -> {
            Component recipeTierNama = Component.literal(GTBSSValues.MNF[tier.getIntValue()]);
            return Component.translatable(
                    "gtceu.multiblock.max_recipe_tier",
                    recipeTierNama).withStyle(ChatFormatting.GRAY);
        })
                .asWidget()
                .tooltip(new RichTooltip().add(Component.translatable("gtceu.multiblock.max_recipe_tier_hover")
                        .withStyle(ChatFormatting.GRAY)))
                .setEnabledIf(widget -> isFormed.getBoolValue());
    }

    public static TextWidget<?> addManaUsageLine(WorkableManaMultiblockMachine weMachine,
                                                 PanelSyncManager syncManager) {
        LongSyncValue manaUsage = syncManager.getOrCreateSyncHandler("manaUsage", LongSyncValue.class,
                () -> new LongSyncValue(() -> {
                    var manaContainerList = weMachine.getManaContainer();
                    return Math.max(manaContainerList.getInputPacketSize(), manaContainerList.getOutputPacketSize());
                }));
        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(weMachine::isFormed));
        BooleanSyncValue isActive = syncManager.getOrCreateSyncHandler("isActive", BooleanSyncValue.class,
                () -> new BooleanSyncValue(() -> weMachine.getRecipeLogic().isActive()));

        return Text.dynamic(() -> {
            String manaFormatted = FormattingUtil.formatNumbers(manaUsage.getLongValue());

            byte voltageTier = GTUtil.getFloorTierByVoltage(manaUsage.getLongValue());
            Component voltageName = Component.literal(
                    GTValues.VNF[voltageTier]);

            MutableComponent bodyText = Component.translatable("gtbss.multiblock.max_mana_per_tick",
                    manaFormatted, voltageName).withStyle(ChatFormatting.GRAY);
            Component hoverText = Component.translatable("gtbss.multiblock.max_mana_per_tick_hover")
                    .withStyle(ChatFormatting.GRAY);
            return bodyText
                    .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)))
                    .withStyle(ChatFormatting.WHITE);
        })
                .asWidget()
                .setEnabledIf($ -> isFormed.getBoolValue() && isActive.getBoolValue());
    }

    //////////////////////////////////////
    // ******** OVERCLOCK *********//
    //////////////////////////////////////
    @Override
    public int getOverclockTier() {
        return getTier();
    }

    @Override
    public int getMaxOverclockTier() {
        return getTier();
    }

    @Override
    public int getMinOverclockTier() {
        return getTier();
    }

    @Override
    public void setOverclockTier(int tier) {}

    @Override
    public long getOverclockVoltage() {
        return getMaxManaPacketSize();
    }

    //////////////////////////////////////
    // ****** RECIPE LOGIC *******//
    //////////////////////////////////////

    public ManaContainerList getManaContainer() {
        List<IManaContainer> containers = new ArrayList<>();
        var handlers = getCapabilitiesFlat(IO.IN, ManaRecipeCapability.CAP);
        if (handlers.isEmpty()) handlers = getCapabilitiesFlat(IO.OUT, ManaRecipeCapability.CAP);
        for (IRecipeHandler<?> handler : handlers) {
            if (handler instanceof IManaContainer container) {
                containers.add(container);
            }
        }
        return new ManaContainerList(containers);
    }

    public long getMaxManaPacketSize() {
        if (this.manaContainer == null) {
            this.manaContainer = getManaContainer();
        }
        if (this.isGenerator()) {
            long packetSize = manaContainer.getOutputPacketSize();
            long packetCount = manaContainer.getOutputPacketCount();
            if (packetCount == 1) {
                return GTValues.VEX[GTUtil.getFloorTierByVoltage(packetSize)];
            } else {
                return packetSize;
            }
        } else {
            long highest = manaContainer.getHighestInputPacketSize();
            int numHighest = manaContainer.getNumHighestInputContainers();
            if (numHighest > 1) {
                int tier = GTUtil.getTierByVoltage(highest);
                return GTValues.V[Math.min(tier + 1, GTValues.MAX)];
            } else {
                return highest;
            }
        }
    }

    public long getDisplayManaPacketSize() {
        long size = -1;
        var handlers = getCapabilitiesFlat(IO.IN, ManaRecipeCapability.CAP);
        if (handlers.isEmpty()) handlers = getCapabilitiesFlat(IO.OUT, ManaRecipeCapability.CAP);
        for (IRecipeHandler<?> handler : handlers) {
            if (handler instanceof IManaContainer container) {
                size = Math.max(size, Math.max(container.getInputPacketSize(), container.getOutputPacketSize()));
            }
        }
        return size;
    }

    public long getDisplayGeneratorManaPower() {
        if (this.isGenerator()) {
            long total = -1;
            var handlers = getCapabilitiesFlat(IO.OUT, ManaRecipeCapability.CAP);
            for (IRecipeHandler<?> handler : handlers) {
                if (handler instanceof IManaContainer container) {
                    total += container.getOutputPacketSize() * container.getOutputPacketCount();
                }
            }
            return total;
        }
        return -1;
    }

    public boolean isGenerator() {
        return getDefinition().isGenerator();
    }

    @Deprecated
    public long getMaxVoltage() {
        return getMaxManaPacketSize();
    }

    @Deprecated
    public long getDisplayRecipeVoltage() {
        return getDisplayManaPacketSize();
    }

    @Deprecated
    public long getDisplayGeneratorPower() {
        return getDisplayGeneratorManaPower();
    }
}

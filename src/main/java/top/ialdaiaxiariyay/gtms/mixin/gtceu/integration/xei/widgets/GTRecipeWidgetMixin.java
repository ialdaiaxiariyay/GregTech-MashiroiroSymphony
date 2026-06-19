package top.ialdaiaxiariyay.gtms.mixin.gtceu.integration.xei.widgets;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ialdaiaxiariyay.bettergtae.utils.NumberUtil;
import top.ialdaiaxiariyay.gtms.api.GTMSValues;
import top.ialdaiaxiariyay.gtms.api.recipe.ManaRecipeHelper;
import top.ialdaiaxiariyay.gtms.api.recipe.ingredient.ManaStack;

import java.util.ArrayList;
import java.util.List;

@Mixin(GTRecipeWidget.class)
public abstract class GTRecipeWidgetMixin extends WidgetGroup {

    @Final
    @Shadow(remap = false)
    private GTRecipe recipe;
    @Final
    @Shadow(remap = false)
    private int xOffset;
    @Shadow(remap = false)
    private int yOffset;
    @Final
    @Shadow(remap = false)
    private List<LabelWidget> recipeParaTexts;
    @Shadow(remap = false)
    private LabelWidget recipeVoltageText;
    @Unique
    private int gtms$currentManaTier;
    @Unique
    private int gtms$baseManaTier;
    @Unique
    private LabelWidget gtms$manaTierLabel;
    @Unique
    private LabelWidget gtms$manaPerTickLabel;
    @Unique
    private LabelWidget gtms$totalManaLabel;

    @Inject(method = "initializeRecipeTextWidget", at = @At("TAIL"), remap = false)
    private void onInitializeRecipeTextWidget(CallbackInfo ci) {
        ManaStack.WithIO manaIO = ManaRecipeHelper.getRealManaWithIO(recipe);
        if (manaIO.isEmpty()) return;
        gtms$baseManaTier = ManaRecipeHelper.getTierFromMana(manaIO.amount());
        gtms$currentManaTier = gtms$baseManaTier;

        int textsBottom = yOffset;
        if (!recipeParaTexts.isEmpty()) {
            LabelWidget lastPara = recipeParaTexts.get(recipeParaTexts.size() - 1);
            textsBottom = lastPara.getPositionY() + 10;
        }
        if (recipeVoltageText != null) {
            textsBottom = Math.max(textsBottom, recipeVoltageText.getPositionY() + 10);
        }

        int guiHeight = getSize().height;

        int leftX = 3 - xOffset;
        int leftStartY = textsBottom;
        if (leftStartY + 20 > guiHeight) {
            leftStartY = guiHeight - 20;
        }
        gtms$manaPerTickLabel = new LabelWidget(leftX, leftStartY, "");
        gtms$totalManaLabel = new LabelWidget(leftX, leftStartY + 10, "");
        this.addWidget(gtms$manaPerTickLabel);
        this.addWidget(gtms$totalManaLabel);

        int rightX = getSize().width - xOffset - 65;
        int tierY = leftStartY + 10;
        int labelWidth = 60;
        int labelHeight = 10;

        ButtonWidget manaTierButton = new ButtonWidget(
                rightX, tierY, labelWidth, labelHeight,
                cd -> gtms$onManaTierClick(cd.button, cd.isShiftClick));
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(Component.translatable("gtceu.oc.tooltip.0", gtms$getManaTierSymbol(gtms$baseManaTier)));
        tooltips.add(Component.translatable("gtceu.oc.tooltip.1"));
        tooltips.add(Component.translatable("gtceu.oc.tooltip.2"));
        tooltips.add(Component.translatable("gtceu.oc.tooltip.3"));
        tooltips.add(Component.translatable("gtceu.oc.tooltip.4"));
        manaTierButton.setHoverTooltips(tooltips.toArray(new Component[0]));
        this.addWidget(manaTierButton);

        if (ChatFormatting.GREEN.getColor() != null) {
            gtms$manaTierLabel = new LabelWidget(rightX, tierY,
                    Component.translatable("gtms.recipe.mana_tier", gtms$getManaTierSymbol(gtms$currentManaTier)))
                    .setTextColor(ChatFormatting.GREEN.getColor());
        }
        this.addWidget(gtms$manaTierLabel);

        gtms$updateDisplayAndUI(false);
    }

    @Unique
    private void gtms$onManaTierClick(int button, boolean shift) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            gtms$setManaTier(gtms$currentManaTier + 1);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            gtms$setManaTier(gtms$currentManaTier - 1);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            gtms$setManaTier(gtms$baseManaTier);
        } else {
            return;
        }
        gtms$updateDisplayAndUI(shift);
    }

    @Unique
    private void gtms$setManaTier(int newTier) {
        gtms$currentManaTier = Math.max(gtms$baseManaTier, Math.min(newTier, 14));
        gtms$manaTierLabel.setComponent(
                Component.translatable("gtms.recipe.mana_tier", (gtms$getManaTierSymbol(gtms$currentManaTier))));
    }

    @Contract(pure = true)
    @Unique
    private @NotNull String gtms$getManaTierSymbol(int tier) {
        if (tier >= 0 && tier < GTMSValues.MNF.length) {
            return GTMSValues.MNF[tier];
        }
        return "";
    }

    @Unique
    private void gtms$restoreOriginalDisplay() {
        ManaStack.WithIO originalMana = ManaRecipeHelper.getRealManaWithIO(recipe);
        if (originalMana.isEmpty()) return;
        long manaPerTick = originalMana.amount();
        long totalMana = manaPerTick * recipe.duration;
        if (gtms$manaPerTickLabel != null) {
            gtms$manaPerTickLabel.setComponent(Component.translatable(
                    "gtms.recipe.mana_per_tick", NumberUtil.formatLong(manaPerTick)));
        }
        if (gtms$totalManaLabel != null) {
            gtms$totalManaLabel.setComponent(Component.translatable(
                    "gtms.recipe.total_mana", NumberUtil.formatLong(totalMana)));
        }
        if (!recipeParaTexts.isEmpty()) {
            Component durText = Component.translatable("gtceu.recipe.duration",
                    FormattingUtil.formatNumbers(recipe.duration / 20f));
            recipeParaTexts.get(0).setComponent(durText);
        }
    }

    @Unique
    private void gtms$updateDisplayAndUI(boolean perfect) {
        ManaStack.WithIO originalMana = ManaRecipeHelper.getRealManaWithIO(recipe);
        if (originalMana.isEmpty()) {
            if (gtms$manaPerTickLabel != null) {
                gtms$manaPerTickLabel.setComponent(Component.empty());
            }
            if (gtms$totalManaLabel != null) {
                gtms$totalManaLabel.setComponent(Component.empty());
            }
            return;
        }

        int ocLevel = gtms$currentManaTier - gtms$baseManaTier;
        if (ocLevel <= 0) {
            gtms$restoreOriginalDisplay();
            return;
        }

        int maxOC = 0;
        int tempDuration = recipe.duration;
        int divisor = perfect ? 4 : 2;
        while (tempDuration > 1) {
            tempDuration /= divisor;
            maxOC++;
        }
        int effectiveOC = Math.min(ocLevel, maxOC);

        double durationMul;
        double manaMul;
        if (perfect) {
            durationMul = Math.pow(0.25, effectiveOC);
        } else {
            durationMul = Math.pow(0.5, effectiveOC);
        }
        manaMul = Math.pow(4, effectiveOC);

        long baseManaPerTick = originalMana.amount();
        int newDuration = (int) Math.max(1, recipe.duration * durationMul);
        long newManaPerTick = (long) Math.ceil(baseManaPerTick * manaMul);
        long totalMana = newManaPerTick * newDuration;

        if (gtms$manaPerTickLabel != null) {
            gtms$manaPerTickLabel.setComponent(Component.translatable(
                    "gtms.recipe.mana_per_tick", NumberUtil.formatLong(newManaPerTick)));
        }
        if (gtms$totalManaLabel != null) {
            gtms$totalManaLabel.setComponent(Component.translatable(
                    "gtms.recipe.total_mana", NumberUtil.formatLong(totalMana)));
        }

        if (!recipeParaTexts.isEmpty()) {
            Component durText = Component.translatable("gtceu.recipe.duration",
                    FormattingUtil.formatNumbers(newDuration / 20f));
            recipeParaTexts.get(0).setComponent(durText);
        }
    }
}

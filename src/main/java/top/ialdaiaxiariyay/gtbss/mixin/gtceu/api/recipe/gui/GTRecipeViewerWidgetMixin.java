package top.ialdaiaxiariyay.gtbss.mixin.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ialdaiaxiariyay.bettergtae.utils.NumberUtil;
import top.ialdaiaxiariyay.gtbss.api.GTBSSValues;
import top.ialdaiaxiariyay.gtbss.api.recipe.ManaRecipeHelper;
import top.ialdaiaxiariyay.gtbss.api.recipe.ingredient.ManaStack;

import java.util.List;

@Mixin(value = GTRecipeViewerWidget.class, remap = false)
public abstract class GTRecipeViewerWidgetMixin {

    @Final
    @Shadow
    private GTRecipe baseRecipe;

    @Final
    @Shadow
    public Flow textComponents;

    @Unique
    private int gtbss$currentManaTier;
    @Unique
    private int gtbss$baseManaTier;
    @Unique
    private long gtbss$baseManaPerTick;
    @Unique
    private int gtbss$baseDuration;
    @Unique
    private boolean gtbss$hasMana = false;
    @Getter
    @Unique
    private ButtonWidget<?> gtbss$manaTierButton;

    @Inject(method = "buildAdditionalRecipeContent", at = @At("TAIL"), remap = false)
    private void onBuildAdditionalRecipeContent(CallbackInfo ci) {
        ManaStack.WithIO manaIO = ManaRecipeHelper.getRealManaWithIO(baseRecipe);
        if (manaIO.isEmpty()) {
            gtbss$hasMana = false;
            return;
        }

        gtbss$hasMana = true;
        gtbss$baseManaPerTick = manaIO.amount();
        gtbss$baseDuration = baseRecipe.duration;
        gtbss$baseManaTier = ManaRecipeHelper.getTierFromMana(gtbss$baseManaPerTick);
        gtbss$currentManaTier = gtbss$baseManaTier;

        List<IWidget> children = textComponents.getChildren();
        if (!children.isEmpty()) {
            // noinspection DataFlowIssue
            children.remove(0);
            IWidget durationLine = Text.dynamic(() -> Component.translatable("gtceu.recipe.duration",
                    FormattingUtil.formatNumbers(gtbss$getCurrentDuration() / 20f))).asWidget();
            children.add(0, durationLine);
        }

        IWidget manaPerTickLine = Text.dynamic(() -> Component.translatable("gtbss.recipe.mana_per_tick",
                NumberUtil.formatLong(gtbss$getCurrentManaPerTick()))).asWidget();
        textComponents.child(manaPerTickLine);

        IWidget totalManaLine = Text.dynamic(() -> Component.translatable("gtbss.recipe.total_mana",
                NumberUtil.formatLong(gtbss$getCurrentManaPerTick() * gtbss$getCurrentDuration()))).asWidget();
        textComponents.child(totalManaLine);

        ButtonWidget<?> tierBtn = new ButtonWidget<>()
                .background(IDrawable.NONE)
                .hoverBackground(IDrawable.NONE)
                .size(22, 12)
                .rightRel(0.0f)
                .overlay(Text.dynamic(() -> Component.literal(GTBSSValues.MNF[gtbss$currentManaTier])))
                .tooltipBuilder(tooltip -> {
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.0",
                            GTBSSValues.MNF[gtbss$baseManaTier]));
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.1"));
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.2"));
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.3"));
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.4"));
                })
                .onMousePressed((ctx, btn) -> {
                    long window = Minecraft.getInstance().getWindow().getWindow();
                    boolean shift = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

                    int maxOC = gtbss$getMaxOC(shift);
                    int maxTier = gtbss$baseManaTier + maxOC;

                    if (btn == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        gtbss$currentManaTier = Math.min(maxTier, gtbss$currentManaTier + 1);
                    } else if (btn == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        gtbss$currentManaTier = Math.max(gtbss$baseManaTier, gtbss$currentManaTier - 1);
                    } else if (btn == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                        gtbss$currentManaTier = gtbss$baseManaTier;
                    } else {
                        return true;
                    }
                    return true;
                })
                .setEnabledIf(w -> gtbss$hasMana);
        textComponents.child(tierBtn);
        gtbss$manaTierButton = tierBtn;
    }

    @Unique
    private int gtbss$getMaxOC(boolean perfect) {
        if (gtbss$baseDuration <= 1) return 0;
        int temp = gtbss$baseDuration;
        int max = 0;
        int divisor = perfect ? 4 : 2;
        while (temp > 1) {
            temp /= divisor;
            max++;
        }
        return max;
    }

    @Unique
    private long gtbss$getCurrentManaPerTick() {
        int ocLevel = gtbss$currentManaTier - gtbss$baseManaTier;
        if (ocLevel <= 0) return gtbss$baseManaPerTick;
        return (long) Math.ceil(gtbss$baseManaPerTick * Math.pow(4, ocLevel));
    }

    @Unique
    private int gtbss$getCurrentDuration() {
        int ocLevel = gtbss$currentManaTier - gtbss$baseManaTier;
        if (ocLevel <= 0) return gtbss$baseDuration;
        long window = Minecraft.getInstance().getWindow().getWindow();
        boolean perfect = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        int divisor = perfect ? 4 : 2;
        double durationMul = Math.pow(1.0 / divisor, ocLevel);
        return Math.max(1, (int) (gtbss$baseDuration * durationMul));
    }
}

package top.ialdaiaxiariyay.gtms.mixin.gtceu.api.recipe.gui;

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
import top.ialdaiaxiariyay.gtms.api.GTMSValues;
import top.ialdaiaxiariyay.gtms.api.recipe.ManaRecipeHelper;
import top.ialdaiaxiariyay.gtms.api.recipe.ingredient.ManaStack;

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
    private int gtms$currentManaTier;
    @Unique
    private int gtms$baseManaTier;
    @Unique
    private long gtms$baseManaPerTick;
    @Unique
    private int gtms$baseDuration;
    @Unique
    private boolean gtms$hasMana = false;
    @Getter
    @Unique
    private ButtonWidget<?> gtms$manaTierButton;

    @Inject(method = "buildAdditionalRecipeContent", at = @At("TAIL"), remap = false)
    private void onBuildAdditionalRecipeContent(CallbackInfo ci) {
        ManaStack.WithIO manaIO = ManaRecipeHelper.getRealManaWithIO(baseRecipe);
        if (manaIO.isEmpty()) {
            gtms$hasMana = false;
            return;
        }

        gtms$hasMana = true;
        gtms$baseManaPerTick = manaIO.amount();
        gtms$baseDuration = baseRecipe.duration;
        gtms$baseManaTier = ManaRecipeHelper.getTierFromMana(gtms$baseManaPerTick);
        gtms$currentManaTier = gtms$baseManaTier;

        List<IWidget> children = textComponents.getChildren();
        if (!children.isEmpty()) {
            //noinspection DataFlowIssue
            children.remove(0);
            IWidget durationLine = Text.dynamic(() -> Component.translatable("gtceu.recipe.duration",
                    FormattingUtil.formatNumbers(gtms$getCurrentDuration() / 20f))).asWidget();
            children.add(0, durationLine);
        }

        IWidget manaPerTickLine = Text.dynamic(() -> Component.translatable("gtms.recipe.mana_per_tick",
                NumberUtil.formatLong(gtms$getCurrentManaPerTick()))).asWidget();
        textComponents.child(manaPerTickLine);

        IWidget totalManaLine = Text.dynamic(() -> Component.translatable("gtms.recipe.total_mana",
                NumberUtil.formatLong(gtms$getCurrentManaPerTick() * gtms$getCurrentDuration()))).asWidget();
        textComponents.child(totalManaLine);

        ButtonWidget<?> tierBtn = new ButtonWidget<>()
                .background(IDrawable.NONE)
                .hoverBackground(IDrawable.NONE)
                .size(22, 12)
                .rightRel(0.0f)
                .overlay(Text.dynamic(() -> Component.literal(GTMSValues.MNF[gtms$currentManaTier])))
                .tooltipBuilder(tooltip -> {
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.0",
                            GTMSValues.MNF[gtms$baseManaTier]));
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.1"));
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.2"));
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.3"));
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.4"));
                })
                .onMousePressed((ctx, btn) -> {
                    long window = Minecraft.getInstance().getWindow().getWindow();
                    boolean shift = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

                    int maxOC = gtms$getMaxOC(shift);
                    int maxTier = gtms$baseManaTier + maxOC;

                    if (btn == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        gtms$currentManaTier = Math.min(maxTier, gtms$currentManaTier + 1);
                    } else if (btn == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        gtms$currentManaTier = Math.max(gtms$baseManaTier, gtms$currentManaTier - 1);
                    } else if (btn == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                        gtms$currentManaTier = gtms$baseManaTier;
                    } else {
                        return true;
                    }
                    return true;
                })
                .setEnabledIf(w -> gtms$hasMana);
        textComponents.child(tierBtn);
        gtms$manaTierButton = tierBtn;
    }

    @Unique
    private int gtms$getMaxOC(boolean perfect) {
        if (gtms$baseDuration <= 1) return 0;
        int temp = gtms$baseDuration;
        int max = 0;
        int divisor = perfect ? 4 : 2;
        while (temp > 1) {
            temp /= divisor;
            max++;
        }
        return max;
    }

    @Unique
    private long gtms$getCurrentManaPerTick() {
        int ocLevel = gtms$currentManaTier - gtms$baseManaTier;
        if (ocLevel <= 0) return gtms$baseManaPerTick;
        return (long) Math.ceil(gtms$baseManaPerTick * Math.pow(4, ocLevel));
    }

    @Unique
    private int gtms$getCurrentDuration() {
        int ocLevel = gtms$currentManaTier - gtms$baseManaTier;
        if (ocLevel <= 0) return gtms$baseDuration;
        long window = Minecraft.getInstance().getWindow().getWindow();
        boolean perfect = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        int divisor = perfect ? 4 : 2;
        double durationMul = Math.pow(1.0 / divisor, ocLevel);
        return Math.max(1, (int) (gtms$baseDuration * durationMul));
    }
}
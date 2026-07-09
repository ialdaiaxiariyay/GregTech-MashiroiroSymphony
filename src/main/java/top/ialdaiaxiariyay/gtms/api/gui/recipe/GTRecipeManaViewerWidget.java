package top.ialdaiaxiariyay.gtms.api.gui.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;

import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.widgets.ButtonWidget;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import top.ialdaiaxiariyay.bettergtae.utils.NumberUtil;
import top.ialdaiaxiariyay.gtms.api.GTMSValues;
import top.ialdaiaxiariyay.gtms.api.recipe.ManaRecipeHelper;
import top.ialdaiaxiariyay.gtms.api.recipe.ingredient.ManaStack;

public class GTRecipeManaViewerWidget extends GTRecipeViewerWidget {

    private final int baseManaTier;
    private int currentManaTier;
    private final long baseManaPerTick;
    private final int baseDuration;

    private IWidget manaTierButton;

    public GTRecipeManaViewerWidget(GTRecipe recipe) {
        super(recipe);
        ManaStack.WithIO manaIO = ManaRecipeHelper.getRealManaWithIO(recipe);
        if (manaIO.isEmpty()) {
            baseManaPerTick = 0;
            baseDuration = recipe.duration;
            baseManaTier = 0;
            currentManaTier = 0;
            return;
        }

        this.baseManaPerTick = manaIO.amount();
        this.baseDuration = recipe.duration;
        this.baseManaTier = ManaRecipeHelper.getTierFromMana(baseManaPerTick);
        this.currentManaTier = this.baseManaTier;

        addManaDisplay();
    }

    private void addManaDisplay() {
        IWidget manaPerTickLine = Text.dynamic(() -> Component.translatable("gtms.recipe.mana_per_tick",
                NumberUtil.formatLong(getCurrentManaPerTick()))).asWidget();
        textComponents.child(1, manaPerTickLine);
        IWidget totalManaLine = Text.dynamic(() -> Component.translatable("gtms.recipe.total_mana",
                NumberUtil.formatLong(getCurrentManaPerTick() * getCurrentDuration()))).asWidget();
        textComponents.child(2, totalManaLine);
        ButtonWidget<?> tierBtn = new ButtonWidget<>()
                .background(IDrawable.NONE)
                .hoverBackground(IDrawable.NONE)
                .size(22, 12)
                .rightRel(0.0f)
                .overlay(Text.dynamic(() -> Component.literal(GTMSValues.MNF[currentManaTier])))
                .tooltipBuilder(tooltip -> {
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.0",
                            GTMSValues.MNF[baseManaTier]));
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.1"));
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.2"));
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.3"));
                    tooltip.addLine(Component.translatable("gtceu.oc.tooltip.4"));
                })
                .onMousePressed((ctx, btn) -> {
                    if (btn == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        currentManaTier = Math.min(14, currentManaTier + 1);
                    } else if (btn == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        currentManaTier = Math.max(baseManaTier, currentManaTier - 1);
                    } else if (btn == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                        currentManaTier = baseManaTier;
                    } else {
                        return true;
                    }
                    updateManaDisplay();
                    return true;
                })
                .setEnabledIf(w -> baseManaPerTick > 0);
        textComponents.child(tierBtn);
        this.manaTierButton = tierBtn;
    }

    private long getCurrentManaPerTick() {
        int ocLevel = currentManaTier - baseManaTier;
        if (ocLevel <= 0) return baseManaPerTick;
        return (long) Math.ceil(baseManaPerTick * Math.pow(4, ocLevel));
    }

    private int getCurrentDuration() {
        int ocLevel = currentManaTier - baseManaTier;
        if (ocLevel <= 0) return baseDuration;
        return Math.max(1, (int) (baseDuration * Math.pow(0.5, ocLevel)));
    }

    private void updateManaDisplay() {
        if (manaTierButton != null) {
            ((ButtonWidget<?>) manaTierButton).overlay(
                    Text.dynamic(() -> Component.literal(GTMSValues.MNF[currentManaTier])));
        }
    }

    @Contract("_, _ -> new")
    public static @NotNull IWidget create(GTRecipe recipe, GTRecipeType recipeType) {
        return new GTRecipeManaViewerWidget(recipe);
    }
}

package top.ialdaiaxiariyay.gtbss.integration.emi.provider;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.api.recipe.type.ItemPureDaisyRecipe;
import top.ialdaiaxiariyay.gtbss.integration.emi.GTBSSEMIPlugin;
import vazkii.botania.common.block.BotaniaFlowerBlocks;

import java.util.ArrayList;
import java.util.List;

public class ItemPureDaisyEmiRecipe implements EmiRecipe {

    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final List<Integer> inputCounts;
    private final List<EmiStack> outputs;
    private final int time;

    private static final EmiStack PURE_DAISY = EmiStack.of(BotaniaFlowerBlocks.pureDaisy);

    public ItemPureDaisyEmiRecipe(@NotNull ItemPureDaisyRecipe recipe) {
        this.id = recipe.getId();
        this.time = recipe.getTime();

        this.inputs = new ArrayList<>();
        this.inputCounts = new ArrayList<>(recipe.getInputCounts());
        for (int i = 0; i < recipe.getInputs().size(); i++) {
            this.inputs.add(EmiIngredient.of(recipe.getInputs().get(i)));
        }

        if (recipe.outputsBlock() && recipe.getOutputState() != null) {
            ItemStack stack = new ItemStack(recipe.getOutputState().getBlock());
            this.outputs = List.of(EmiStack.of(stack));
        } else if (recipe.outputsItem() && recipe.getOutputItem() != null) {
            ItemStack outStack = recipe.getOutputItem().copy();
            outStack.setCount(outStack.getCount() * recipe.getOutputCount());
            this.outputs = List.of(EmiStack.of(outStack));
        } else {
            this.outputs = List.of(EmiStack.of(Blocks.AIR));
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTBSSEMIPlugin.PURE_DAISY_CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        int baseWidth = 10 + inputs.size() * 22 + 50 + 10;
        return Math.max(120, baseWidth);
    }

    @Override
    public int getDisplayHeight() {
        return 60;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int x = 10;
        for (int i = 0; i < inputs.size(); i++) {
            EmiIngredient ing = inputs.get(i);
            int count = inputCounts.get(i);
            widgets.addSlot(ing, x, 13).drawBack(false);
            if (count > 1) {
                widgets.addText(
                        Component.literal("x" + count).withStyle(ChatFormatting.WHITE),
                        x + 6, 13, 0xFFFFFF, true);
            }
            x += 22;
        }

        int catalystX = x + 4;
        widgets.addSlot(PURE_DAISY, catalystX, 13).catalyst(true).drawBack(false);

        int outputX = catalystX + 22 + 4;
        widgets.addSlot(outputs.get(0), outputX, 13).drawBack(false).recipeContext(this);

        widgets.addText(
                Component.translatable("gtceu.recipe.duration", time).withStyle(ChatFormatting.GRAY),
                10, 50, 0xFFFFFF, false);
    }
}

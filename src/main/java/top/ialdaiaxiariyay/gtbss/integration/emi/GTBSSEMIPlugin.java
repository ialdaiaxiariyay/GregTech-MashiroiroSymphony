package top.ialdaiaxiariyay.gtbss.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.recipe.type.ItemPureDaisyRecipe;
import top.ialdaiaxiariyay.gtbss.common.data.VanillaRecipeType;
import top.ialdaiaxiariyay.gtbss.integration.emi.provider.ItemPureDaisyEmiRecipe;
import vazkii.botania.common.block.BotaniaFlowerBlocks;

@EmiEntrypoint
public class GTBSSEMIPlugin implements EmiPlugin {

    public static final EmiRecipeCategory PURE_DAISY_CATEGORY = new EmiRecipeCategory(
            GTBSS.id("pure_daisy"),
            EmiStack.of(BotaniaFlowerBlocks.pureDaisy));

    @Override
    public void register(@NotNull EmiRegistry registry) {
        registry.addCategory(PURE_DAISY_CATEGORY);
        registry.addWorkstation(PURE_DAISY_CATEGORY,
                EmiStack.of(BotaniaFlowerBlocks.pureDaisy));
        var recipes = registry.getRecipeManager()
                .getAllRecipesFor(VanillaRecipeType.ITEM_PURE_DAISY_TYPE.get());
        for (ItemPureDaisyRecipe recipe : recipes) {
            registry.addRecipe(new ItemPureDaisyEmiRecipe(recipe));
        }
    }
}

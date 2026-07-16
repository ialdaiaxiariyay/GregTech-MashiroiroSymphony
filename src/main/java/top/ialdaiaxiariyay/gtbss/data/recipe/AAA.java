package top.ialdaiaxiariyay.gtbss.data.recipe;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSRecipeTypes;

import java.util.function.Consumer;

public class AAA {

    public static void init(Consumer<FinishedRecipe> provider) {
        GTBSSRecipeTypes.FURNACE_RECIPES.recipeBuilder(GTBSS.id("aa"))
                .inputItems(Items.IRON_INGOT)
                .outputItems(Items.GOLD_INGOT)
                .Manat(8)
                .duration(20 * 5)
                .save(provider);
    }
}

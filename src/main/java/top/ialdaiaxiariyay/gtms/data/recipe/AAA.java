package top.ialdaiaxiariyay.gtms.data.recipe;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.common.data.GTMSRecipeTypes;

import java.util.function.Consumer;

public class AAA {

    public static void init(Consumer<FinishedRecipe> provider) {
        GTMSRecipeTypes.FURNACE_RECIPES.recipeBuilder(GTMS.id("aa"))
                .inputItems(Items.IRON_INGOT)
                .outputItems(Items.GOLD_INGOT)
                .Manat(8)
                .duration(20 * 5)
                .save(provider);
    }
}

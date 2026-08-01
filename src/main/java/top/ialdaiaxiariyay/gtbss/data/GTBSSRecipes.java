package top.ialdaiaxiariyay.gtbss.data;

import net.minecraft.data.recipes.FinishedRecipe;

import top.ialdaiaxiariyay.gtbss.data.recipe.CraftingTable;
import top.ialdaiaxiariyay.gtbss.data.recipe.machine.AAA;
import top.ialdaiaxiariyay.gtbss.data.recipe.machine.Assembler;

import java.util.function.Consumer;

public class GTBSSRecipes {

    public static void init(Consumer<FinishedRecipe> consumer) {
        AAA.init(consumer);
        Assembler.init(consumer);
        CraftingTable.init(consumer);
    }
}

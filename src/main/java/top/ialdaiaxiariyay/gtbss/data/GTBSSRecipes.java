package top.ialdaiaxiariyay.gtbss.data;

import net.minecraft.data.recipes.FinishedRecipe;

import top.ialdaiaxiariyay.gtbss.data.recipe.botania.PureDaisy;
import top.ialdaiaxiariyay.gtbss.data.recipe.machine.AAA;
import top.ialdaiaxiariyay.gtbss.data.recipe.machine.Assembler;
import top.ialdaiaxiariyay.gtbss.data.recipe.machine.Mixer;
import top.ialdaiaxiariyay.gtbss.data.recipe.vanilla.CraftingTable;
import top.ialdaiaxiariyay.gtbss.data.recipe.vanilla.SmeltingRecipes;

import java.util.function.Consumer;

public class GTBSSRecipes {

    public static void init(Consumer<FinishedRecipe> consumer) {
        AAA.init(consumer);
        Assembler.init(consumer);
        CraftingTable.init(consumer);
        Mixer.init(consumer);
        SmeltingRecipes.init(consumer);
        PureDaisy.init(consumer);
    }
}

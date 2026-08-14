package top.ialdaiaxiariyay.gtbss.data.recipe.vanilla;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;

import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSMaterials;
import top.ialdaiaxiariyay.gtbss.utils.GTBSSUtil;
import top.ialdaiaxiariyay.gtbss.utils.ModIdUtil;

import java.util.function.Consumer;

public class SmeltingRecipes {

    public static void init(Consumer<FinishedRecipe> consumer) {
        VanillaRecipeHelper.addSmeltingRecipe(consumer, GTBSS.id(""),
                GTBSSUtil.getItemTag(ModIdUtil.Botania("mystical_flowers")),
                ChemicalHelper.get(TagPrefix.dustSmall, GTBSSMaterials.Dream));
    }
}

package top.ialdaiaxiariyay.gtbss.data.recipe.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import net.minecraft.data.recipes.FinishedRecipe;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSItems;
import top.ialdaiaxiariyay.gtbss.utils.GTBSSUtil;
import top.ialdaiaxiariyay.gtbss.utils.ModIdUtil;
import vazkii.botania.common.item.BotaniaItems;

import java.util.function.Consumer;

public class Mixer {

    private static final GTRecipeType TYPE = GTRecipeTypes.MIXER_RECIPES;

    public static void init(Consumer<FinishedRecipe> consumer){
        TYPE.recipeBuilder(GTBSS.id(""))
                .inputItems(GTBSSUtil.getItemTag(ModIdUtil.Botania("petals")), 4)
                .inputItems(GTBSSItems.MATERIALIZED_SPIRIT_SHARD, 2)
                .outputItems(BotaniaItems.fertilizer, 4)
                .duration(20*2)
                .EUt(GTValues.VA[GTValues.LV])
                .save(consumer);
    }
}

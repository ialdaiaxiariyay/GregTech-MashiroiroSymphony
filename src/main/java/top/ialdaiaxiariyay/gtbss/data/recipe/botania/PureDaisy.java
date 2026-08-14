package top.ialdaiaxiariyay.gtbss.data.recipe.botania;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import appeng.core.definitions.AEItems;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSItems;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSMaterials;
import top.ialdaiaxiariyay.gtbss.data.recipe.ItemPureDaisyRecipeBuilder;

import java.util.function.Consumer;

public class PureDaisy {

    public static void init(Consumer<FinishedRecipe> consumer) {
        item(consumer);
        block(consumer);
    }

    private static void item(Consumer<FinishedRecipe> consumer) {
        ItemPureDaisyRecipeBuilder.builder()
                .id(GTBSS.id("materialized_spirit_shard"))
                .input(AEItems.FLUIX_CRYSTAL, 4)
                .input(ChemicalHelper.getItem(TagPrefix.dust, GTBSSMaterials.Dream))
                .output(GTBSSItems.MATERIALIZED_SPIRIT_SHARD)
                .time(5)
                .save(consumer);
    }

    private static void block(Consumer<FinishedRecipe> consumer) {
//        ItemPureDaisyRecipeBuilder.builder()
//                .id(GTBSS.id("test_2"))
//                .input(Items.GOLD_INGOT)
//                .output(Blocks.REDSTONE_BLOCK)
//                .time(5)
//                .save(consumer);
    }
}

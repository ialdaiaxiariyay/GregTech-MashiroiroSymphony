package top.ialdaiaxiariyay.gtbss.data.recipe.vanilla;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSMachines;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSMaterials;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.function.Consumer;

public class CraftingTable {

    public static void init(Consumer<FinishedRecipe> consumer) {
        VanillaRecipeHelper.addShapedRecipe(consumer, true, GTBSS.id("emitter_ev"),
                GTBSSMachines.WIRELESS_RESOURCE_MONITOR.asStack(),
                "AB", "CD",
                'A', Items.ENDER_EYE,
                'B', GTItems.SENSOR_LV.asStack(),
                'C', GTBlocks.MACHINE_CASING_LV.asStack(),
                'D', GTItems.COVER_SCREEN.asStack());

        VanillaRecipeHelper.addShapedRecipe(consumer, true, GTBSS.id("apothecary_default"),
                BotaniaBlocks.defaultAltar.asItem().getDefaultInstance(),
                "ABA", " A ", "AAA",
                'A', GTBlocks.CASING_PRIMITIVE_BRICKS.asStack(),
                'B', ChemicalHelper.get(TagPrefix.dust, GTBSSMaterials.Dream));
    }
}

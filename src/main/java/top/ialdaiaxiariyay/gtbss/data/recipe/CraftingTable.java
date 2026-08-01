package top.ialdaiaxiariyay.gtbss.data.recipe;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSMachines;

import java.util.function.Consumer;

public class CraftingTable {
    public static void init(Consumer<FinishedRecipe> consumer){
        VanillaRecipeHelper.addShapedRecipe(consumer, true, "emitter_ev", GTBSSMachines.WIRELESS_RESOURCE_MONITOR.asStack(),
                "AB", "CD",
                'A', Items.ENDER_EYE,
                'B', GTItems.SENSOR_LV.asStack(),
                'C', GTBlocks.MACHINE_CASING_LV.asStack(),
                'D', GTItems.COVER_SCREEN.asStack());
    }
}

package top.ialdaiaxiariyay.gtbss.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.recipe.GTBSSRecipeType;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ELECTRIC;

public class GTBSSRecipeTypes {

    public static final GTBSSRecipeType FURNACE_RECIPES = register("electric_furnace", ELECTRIC)
            .setMaxIOSize(1, 1, 0, 0).setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.FURNACE_OVERLAY_1))
            .setSound(GTSoundEntries.FURNACE);

    public static void init() {}

    @SuppressWarnings("deprecation")
    public static @NotNull GTBSSRecipeType register(String name, String group, RecipeType<?>... proxyRecipes) {
        var recipeType = new GTBSSRecipeType(GTBSS.id(name), group, proxyRecipes);
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
        return recipeType;
    }
}

package top.ialdaiaxiariyay.gtbss.common.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.tterrag.registrate.util.entry.RegistryEntry;
import top.ialdaiaxiariyay.gtbss.api.recipe.type.ItemPureDaisyRecipe;
import top.ialdaiaxiariyay.gtbss.api.registrate.GTBSSRegistrate;

public class VanillaRecipeType {

    public static void init() {}

    public static final RegistryEntry<RecipeSerializer<ItemPureDaisyRecipe>> ITEM_PURE_DAISY_SERIALIZER = GTBSSRegistrate.REGISTRATION
            .simple(
                    "item_pure_daisy",
                    Registries.RECIPE_SERIALIZER,
                    ItemPureDaisyRecipe.Serializer::new);

    public static final RegistryEntry<RecipeType<ItemPureDaisyRecipe>> ITEM_PURE_DAISY_TYPE = GTBSSRegistrate.REGISTRATION
            .simple(
                    "item_pure_daisy",
                    Registries.RECIPE_TYPE,
                    () -> new RecipeType<>() {});
}

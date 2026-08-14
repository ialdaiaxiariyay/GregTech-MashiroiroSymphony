package top.ialdaiaxiariyay.gtbss.data.recipe;

import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import top.ialdaiaxiariyay.gtbss.utils.ModIdUtil;
import vazkii.botania.api.BotaniaAPI;

import java.util.HashSet;
import java.util.Set;

public class RemoveRecipe {

    public static Object2ObjectMap<String, String> PrefixRecipe = new Object2ObjectOpenHashMap<>();

    public static Set<ResourceLocation> recipe = new HashSet<>();

    public static void init() {
        removeMatchingPrefixRecipe(BotaniaAPI.MODID, "apothecary_");
        removeRecipe(ModIdUtil.Botania("fertilizer_dye"));
    }

    public static void removeRecipe(ResourceLocation id) {
        recipe.add(id);
    }

    public static void removeMatchingPrefixRecipe(String modId, String prefix) {
        PrefixRecipe.put(modId, prefix);
    }
}

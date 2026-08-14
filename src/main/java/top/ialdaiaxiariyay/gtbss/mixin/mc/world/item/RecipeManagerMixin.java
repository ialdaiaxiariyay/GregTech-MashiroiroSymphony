package top.ialdaiaxiariyay.gtbss.mixin.mc.world.item;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ialdaiaxiariyay.gtbss.data.recipe.RemoveRecipe;

import java.util.Iterator;
import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 900)
public abstract class RecipeManagerMixin {

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "HEAD"))
    private void gtceu$cloneVanillaRecipes(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
                                            ProfilerFiller profiler, CallbackInfo ci) {
        for (ResourceLocation toRemove : RemoveRecipe.recipe) {
            map.remove(toRemove);
        }

        Iterator<Map.Entry<ResourceLocation, JsonElement>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, JsonElement> entry = it.next();
            ResourceLocation id = entry.getKey();
            for (Map.Entry<String, String> prefix : RemoveRecipe.PrefixRecipe.entrySet()) {
                if (id.getNamespace().equals(prefix.getKey()) && id.getPath().startsWith(prefix.getValue())) {
                    it.remove();
                    break;
                }
            }
        }
    }

}

package top.ialdaiaxiariyay.gtbss.mixin.mc.client;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.RegistryAccess;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.Recipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(ClientRecipeBook.class)
public abstract class ClientRecipeBookMixin extends RecipeBook {

    @Shadow
    private Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab;

    @Shadow
    private List<RecipeCollection> allCollections;

    /**
     * @author GTBSS
     * @reason Overrides setup to clear all collections – no recipe groups are built.
     */
    @Overwrite
    public void setupCollections(Iterable<Recipe<?>> recipes, RegistryAccess registryAccess) {
        this.collectionsByTab = ImmutableMap.of();
        this.allCollections = ImmutableList.of();
    }

    /**
     * @author GTBSS
     * @reason Returns an empty list – no recipe collections are available on client.
     */
    @Overwrite
    public List<RecipeCollection> getCollections() {
        return ImmutableList.of();
    }

    /**
     * @author GTBSS
     * @reason Returns an empty list for any category – hides all recipe tabs.
     */
    @Overwrite
    public List<RecipeCollection> getCollection(RecipeBookCategories categories) {
        return ImmutableList.of();
    }
}

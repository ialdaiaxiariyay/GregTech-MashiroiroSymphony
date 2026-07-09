package top.ialdaiaxiariyay.gtms.mixin.mc.stats;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(RecipeBook.class)
public abstract class RecipeBookMixin {

    @Shadow
    @Final
    protected Set<ResourceLocation> known;

    @Shadow
    @Final
    protected Set<ResourceLocation> highlight;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(CallbackInfo ci) {
        known.clear();
        highlight.clear();
    }

    /**
     * @author GTMS
     * @reason Prevents copying data from another RecipeBook – clears own collections instead.
     */
    @Overwrite
    public void copyOverData(RecipeBook other) {
        known.clear();
        highlight.clear();
    }

    /**
     * @author GTMS
     * @reason Disables adding any recipe – no-op to prevent storing known recipes.
     */
    @Overwrite
    public void add(Recipe<?> recipe) {
    }

    /**
     * @author GTMS
     * @reason Disables adding any recipe ID – ensures no recipe identifier is stored.
     */
    @Overwrite
    protected void add(ResourceLocation recipeId) {
    }

    /**
     * @author GTMS
     * @reason Always returns false – no recipe is ever recognized as known.
     */
    @Overwrite
    public boolean contains(@Nullable Recipe<?> recipe) {
        return false;
    }

    /**
     * @author GTMS
     * @reason Always returns false for any recipe ID – effectively hides all recipes.
     */
    @Overwrite
    public boolean contains(ResourceLocation recipeId) {
        return false;
    }

    /**
     * @author GTMS
     * @reason Does nothing when removing a recipe – since none are stored.
     */
    @Overwrite
    public void remove(Recipe<?> recipe) {
    }

    /**
     * @author GTMS
     * @reason Does nothing when removing a recipe ID – no effect.
     */
    @Overwrite
    protected void remove(ResourceLocation recipeId) {
    }

    /**
     * @author GTMS
     * @reason Always returns false – no recipe is ever highlighted.
     */
    @Overwrite
    public boolean willHighlight(Recipe<?> recipe) {
        return false;
    }

    /**
     * @author GTMS
     * @reason Ignores highlight removal – no highlights exist.
     */
    @Overwrite
    public void removeHighlight(Recipe<?> recipe) {
    }

    /**
     * @author GTMS
     * @reason Ignores adding a highlight – prevents any visual indication.
     */
    @Overwrite
    public void addHighlight(Recipe<?> recipe) {
    }

    /**
     * @author GTMS
     * @reason Ignores adding highlight by ID – no effect.
     */
    @Overwrite
    protected void addHighlight(ResourceLocation recipeId) {
    }

    /**
     * @author GTMS
     * @reason Always returns false – recipe book is never considered open.
     */
    @Overwrite
    public boolean isOpen(RecipeBookType bookType) {
        return false;
    }

    /**
     * @author GTMS
     * @reason Ignores any attempt to set the open state.
     */
    @Overwrite
    public void setOpen(RecipeBookType bookType, boolean open) {
    }

    /**
     * @author GTMS
     * @reason Always returns false – filtering is disabled for any menu.
     */
    @Overwrite
    public boolean isFiltering(RecipeBookMenu<?> bookMenu) {
        return false;
    }

    /**
     * @author GTMS
     * @reason Always returns false – filtering is disabled for any book type.
     */
    @Overwrite
    public boolean isFiltering(RecipeBookType bookType) {
        return false;
    }

    /**
     * @author GTMS
     * @reason Ignores attempts to set filtering state.
     */
    @Overwrite
    public void setFiltering(RecipeBookType bookType, boolean filtering) {
    }

    /**
     * @author GTMS
     * @reason Ignores external settings – prevents modification of internal flags.
     */
    @Overwrite
    public void setBookSettings(RecipeBookSettings settings) {
    }

    /**
     * @author GTMS
     * @reason Returns a fresh RecipeBookSettings instance with all flags forced to false.
     */
    @Overwrite
    public RecipeBookSettings getBookSettings() {
        RecipeBookSettings settings = new RecipeBookSettings();
        for (RecipeBookType type : RecipeBookType.values()) {
            settings.setOpen(type, false);
            settings.setFiltering(type, false);
        }
        return settings;
    }

    /**
     * @author GTMS
     * @reason Ignores setting both open and filtering at once – no effect.
     */
    @Overwrite
    public void setBookSetting(RecipeBookType bookType, boolean open, boolean filtering) {
    }
}
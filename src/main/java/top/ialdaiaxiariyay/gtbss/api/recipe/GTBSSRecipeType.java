package top.ialdaiaxiariyay.gtbss.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeAdditionHandler;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.recipe.builder.GTBSSRecipeBuilder;

import java.util.*;
import java.util.function.*;

public class GTBSSRecipeType extends GTRecipeType {

    @Getter
    private GTBSSRecipeBuilder gtbssRecipeBuilder;

    public GTBSSRecipeType(ResourceLocation registryName, String group, RecipeType<?>... proxyRecipes) {
        super(registryName, group, proxyRecipes);
        this.gtbssRecipeBuilder = new GTBSSRecipeBuilder(registryName, this);
    }

    @Override
    public GTBSSRecipeType setMaxIOSize(int maxItemInputs, int maxItemOutputs, int maxFluidInputs,
                                        int maxFluidOutputs) {
        return (GTBSSRecipeType) super.setMaxIOSize(maxItemInputs, maxItemOutputs, maxFluidInputs, maxFluidOutputs);
    }

    @Override
    public GTBSSRecipeType setEUIO(IO io) {
        return (GTBSSRecipeType) super.setEUIO(io);
    }

    @Override
    public GTBSSRecipeType setMaxSize(IO io, RecipeCapability<?> cap, int max) {
        return (GTBSSRecipeType) super.setMaxSize(io, cap, max);
    }

    @Override
    public GTBSSRecipeType UI(UnaryOperator<GTRecipeTypeUILayout.Builder> builder) {
        return (GTBSSRecipeType) super.UI(builder);
    }

    @Override
    public GTBSSRecipeType setMaxTooltips(int maxTooltips) {
        return (GTBSSRecipeType) super.setMaxTooltips(maxTooltips);
    }

    @Override
    public GTBSSRecipeType setXEIVisible(boolean XEIVisible) {
        return (GTBSSRecipeType) super.setXEIVisible(XEIVisible);
    }

    @Override
    public void setMinRecipeConditions(int n) {
        super.setMinRecipeConditions(n);
    }

    @Override
    public GTBSSRecipeType addCustomRecipeLogic(ICustomRecipeLogic recipeLogic) {
        return (GTBSSRecipeType) super.addCustomRecipeLogic(recipeLogic);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public @NotNull Iterator<GTRecipe> searchRecipe(IRecipeCapabilityHolder holder, Predicate<GTRecipe> canHandle) {
        return super.searchRecipe(holder, canHandle);
    }

    @Override
    public int getMaxInputs(RecipeCapability<?> cap) {
        return super.getMaxInputs(cap);
    }

    @Override
    public int getMaxOutputs(RecipeCapability<?> cap) {
        return super.getMaxOutputs(cap);
    }

    @Override
    public int getMaxSlots(RecipeCapability<?> cap, IO io) {
        return super.getMaxSlots(cap, io);
    }

    @Override
    public GTBSSRecipeType prepareBuilder(@NotNull Consumer<GTRecipeBuilder> onPrepare) {
        onPrepare.accept(gtbssRecipeBuilder);
        return this;
    }

    @Override
    public GTBSSRecipeBuilder recipeBuilder(ResourceLocation id) {
        return gtbssRecipeBuilder.copy(id);
    }

    @Override
    public GTBSSRecipeBuilder recipeBuilder(ResourceLocation id, Object... append) {
        if (append.length > 0) {
            String toAppend = Arrays.stream(append)
                    .map(Object::toString)
                    .map(FormattingUtil::toLowerCaseUnderscore)
                    .reduce("", (a, b) -> a + "_" + b);
            id = id.withSuffix(toAppend);
        }
        return recipeBuilder(id);
    }

    @Override
    public GTBSSRecipeBuilder recipeBuilder(String id) {
        return recipeBuilder(GTBSS.id(id));
    }

    @Override
    public GTBSSRecipeBuilder recipeBuilder(String id, Object... append) {
        return recipeBuilder(GTBSS.id(id), append);
    }

    public GTBSSRecipeBuilder copyFrom(GTBSSRecipeBuilder builder) {
        return gtbssRecipeBuilder.copyFrom(builder);
    }

    @Override
    public GTBSSRecipeType onRecipeBuild(BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onBuild) {
        gtbssRecipeBuilder.onSave(onBuild);
        return this;
    }

    @Override
    public void addDataStickEntry(@NotNull String researchId, @NotNull GTRecipe recipe) {
        super.addDataStickEntry(researchId, recipe);
    }

    @Override
    public @Nullable Collection<GTRecipe> getDataStickEntry(@NotNull String researchId) {
        return super.getDataStickEntry(researchId);
    }

    @Override
    public boolean removeDataStickEntry(@NotNull String researchId, @NotNull GTRecipe recipe) {
        return super.removeDataStickEntry(researchId, recipe);
    }

    @Override
    public GTRecipe toGTrecipe(ResourceLocation id, Recipe<?> recipe) {
        return super.toGTrecipe(id, recipe);
    }

    @Override
    public void buildRepresentativeRecipes() {
        super.buildRepresentativeRecipes();
    }

    @Override
    public void addToMainCategory(GTRecipe recipe) {
        super.addToMainCategory(recipe);
    }

    @Override
    public void addToCategoryMap(GTRecipeCategory category, GTRecipe recipe) {
        super.addToCategoryMap(category, recipe);
    }

    @Override
    public Set<GTRecipeCategory> getCategories() {
        return super.getCategories();
    }

    @Override
    public Set<GTRecipe> getRecipesInCategory(GTRecipeCategory category) {
        return super.getRecipesInCategory(category);
    }

    @Override
    public @NotNull RecipeDB db() {
        return super.db();
    }

    @Override
    public void beginStagingRecipes() {
        super.beginStagingRecipes();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return super.getRegistryName();
    }

    public GTBSSRecipeType setRecipeBuilder(GTBSSRecipeBuilder recipeBuilder) {
        this.gtbssRecipeBuilder = recipeBuilder;
        return this;
    }

    @Override
    public @NotNull GTBSSRecipeType setSmallRecipeMap(GTRecipeType smallRecipeMap) {
        return (GTBSSRecipeType) super.setSmallRecipeMap(smallRecipeMap);
    }

    @Override
    public GTBSSRecipeType getSmallRecipeMap() {
        return (GTBSSRecipeType) super.getSmallRecipeMap();
    }

    @Override
    public @NotNull GTBSSRecipeType setIconSupplier(@Nullable Supplier<ItemStack> iconSupplier) {
        return (GTBSSRecipeType) super.setIconSupplier(iconSupplier);
    }

    @Override
    public @Nullable Supplier<ItemStack> getIconSupplier() {
        return super.getIconSupplier();
    }

    @Override
    public @NotNull GTBSSRecipeType setSound(@Nullable SoundEntry sound) {
        return (GTBSSRecipeType) super.setSound(sound);
    }

    @Override
    public @Nullable SoundEntry getSound() {
        return super.getSound();
    }

    @Override
    public List<Function<CompoundTag, String>> getDataInfos() {
        return super.getDataInfos();
    }

    @Override
    public boolean isScanner() {
        return super.isScanner();
    }

    @Override
    public @NotNull GTBSSRecipeType setScanner(boolean isScanner) {
        return (GTBSSRecipeType) super.setScanner(isScanner);
    }

    @Override
    public boolean isHasResearchSlot() {
        return super.isHasResearchSlot();
    }

    @Override
    public @NotNull GTBSSRecipeType setHasResearchSlot(boolean hasResearchSlot) {
        return (GTBSSRecipeType) super.setHasResearchSlot(hasResearchSlot);
    }

    @Override
    public Map<RecipeType<?>, List<GTRecipe>> getProxyRecipes() {
        return super.getProxyRecipes();
    }

    @Override
    public GTRecipeCategory getCategory() {
        return super.getCategory();
    }

    @Override
    public Map<GTRecipeCategory, Set<GTRecipe>> getCategoryMap() {
        return super.getCategoryMap();
    }

    @Override
    public RecipeAdditionHandler getAdditionHandler() {
        return super.getAdditionHandler();
    }

    @Override
    public @NotNull GTBSSRecipeType setOffsetVoltageText(boolean offsetVoltageText) {
        return (GTBSSRecipeType) super.setOffsetVoltageText(offsetVoltageText);
    }

    @Override
    public boolean isOffsetVoltageText() {
        return super.isOffsetVoltageText();
    }

    @Override
    public @NotNull GTBSSRecipeType setVoltageTextOffset(int voltageTextOffset) {
        return (GTBSSRecipeType) super.setVoltageTextOffset(voltageTextOffset);
    }

    @Override
    public int getVoltageTextOffset() {
        return super.getVoltageTextOffset();
    }

    @Override
    public List<ICustomRecipeLogic> getCustomRecipeLogicRunners() {
        return super.getCustomRecipeLogicRunners();
    }

    @Override
    public int getMinRecipeConditions() {
        return super.getMinRecipeConditions();
    }

    @Override
    public GTRecipeTypeUILayout getUiLayout() {
        return super.getUiLayout();
    }

    @Override
    public @NotNull GTBSSRecipeType setUiLayout(GTRecipeTypeUILayout uiLayout) {
        return (GTBSSRecipeType) super.setUiLayout(uiLayout);
    }
}

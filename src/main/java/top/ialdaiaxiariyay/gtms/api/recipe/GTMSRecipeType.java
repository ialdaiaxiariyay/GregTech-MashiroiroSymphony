package top.ialdaiaxiariyay.gtms.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.SteamTexture;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeAdditionHandler;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.recipe.builder.GTMSRecipeBuilder;

import java.util.*;
import java.util.function.*;

public class GTMSRecipeType extends GTRecipeType {

    @Getter
    private GTMSRecipeBuilder gtmsRecipeBuilder;

    public GTMSRecipeType(ResourceLocation registryName, String group, RecipeType<?>... proxyRecipes) {
        super(registryName, group, proxyRecipes);
        this.gtmsRecipeBuilder = new GTMSRecipeBuilder(registryName, this);
    }

    @Override
    public GTMSRecipeType setMaxIOSize(int maxInputs, int maxOutputs, int maxFluidInputs, int maxFluidOutputs) {
        return (GTMSRecipeType) super.setMaxIOSize(maxInputs, maxOutputs, maxFluidInputs, maxFluidOutputs);
    }

    @Override
    public GTMSRecipeType setEUIO(IO io) {
        return (GTMSRecipeType) super.setEUIO(io);
    }

    @Override
    public GTMSRecipeType setMaxSize(IO io, RecipeCapability<?> cap, int max) {
        return (GTMSRecipeType) super.setMaxSize(io, cap, max);
    }

    @Override
    public GTMSRecipeType setSlotOverlay(boolean isOutput, boolean isFluid, IGuiTexture slotOverlay) {
        return (GTMSRecipeType) super.setSlotOverlay(isOutput, isFluid, slotOverlay);
    }

    @Override
    public GTMSRecipeType setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast, IGuiTexture slotOverlay) {
        return (GTMSRecipeType) super.setSlotOverlay(isOutput, isFluid, isLast, slotOverlay);
    }

    @Override
    public GTMSRecipeType setProgressBar(ResourceTexture progressBar, ProgressTexture.FillDirection moveType) {
        return (GTMSRecipeType) super.setProgressBar(progressBar, moveType);
    }

    @Override
    public GTMSRecipeType setSteamProgressBar(SteamTexture progressBar, ProgressTexture.FillDirection moveType) {
        return (GTMSRecipeType) super.setSteamProgressBar(progressBar, moveType);
    }

    @Override
    public GTMSRecipeType setUiBuilder(BiConsumer<GTRecipe, WidgetGroup> uiBuilder) {
        return (GTMSRecipeType) super.setUiBuilder(uiBuilder);
    }

    @Override
    public GTMSRecipeType setMaxTooltips(int maxTooltips) {
        return (GTMSRecipeType) super.setMaxTooltips(maxTooltips);
    }

    @Override
    public GTMSRecipeType setXEIVisible(boolean XEIVisible) {
        return (GTMSRecipeType) super.setXEIVisible(XEIVisible);
    }

    @Override
    public GTMSRecipeType addDataInfo(Function<CompoundTag, String> dataInfo) {
        return (GTMSRecipeType) super.addDataInfo(dataInfo);
    }

    @Override
    public void setMinRecipeConditions(int n) {
        super.setMinRecipeConditions(n);
    }

    @Override
    public GTMSRecipeType addCustomRecipeLogic(ICustomRecipeLogic recipeLogic) {
        return (GTMSRecipeType) super.addCustomRecipeLogic(recipeLogic);
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
    public GTMSRecipeType prepareBuilder(@NotNull Consumer<GTRecipeBuilder> onPrepare) {
        onPrepare.accept(gtmsRecipeBuilder);
        return this;
    }

    @Override
    public GTMSRecipeBuilder recipeBuilder(ResourceLocation id) {
        return gtmsRecipeBuilder.copy(id);
    }

    @Override
    public GTMSRecipeBuilder recipeBuilder(ResourceLocation id, Object... append) {
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
    public GTMSRecipeBuilder recipeBuilder(String id) {
        return recipeBuilder(GTMS.id(id));
    }

    @Override
    public GTMSRecipeBuilder recipeBuilder(String id, Object... append) {
        return recipeBuilder(GTMS.id(id), append);
    }

    @Override
    public GTMSRecipeBuilder copyFrom(GTRecipeBuilder builder) {
        return gtmsRecipeBuilder.copyFrom(builder);
    }

    @Override
    public GTMSRecipeType onRecipeBuild(BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onBuild) {
        gtmsRecipeBuilder.onSave(onBuild);
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
    public @NotNull GTMSRecipeType setRecipeBuilder(GTRecipeBuilder recipeBuilder) {
        if (recipeBuilder instanceof GTMSRecipeBuilder msBuilder) {
            this.gtmsRecipeBuilder = msBuilder;
        } else {
            this.gtmsRecipeBuilder = new GTMSRecipeBuilder(recipeBuilder.buildRawRecipe(), this);
        }
        super.setRecipeBuilder(this.gtmsRecipeBuilder);
        return this;
    }

    @Override
    public ChanceBoostFunction getChanceFunction() {
        return super.getChanceFunction();
    }

    @Override
    public @NotNull GTMSRecipeType setChanceFunction(ChanceBoostFunction chanceFunction) {
        return (GTMSRecipeType) super.setChanceFunction(chanceFunction);
    }

    @Override
    public GTRecipeTypeUI getRecipeUI() {
        return super.getRecipeUI();
    }

    @Override
    public @NotNull GTMSRecipeType setRecipeUI(GTRecipeTypeUI recipeUI) {
        return (GTMSRecipeType) super.setRecipeUI(recipeUI);
    }

    @Override
    public @NotNull GTMSRecipeType setSmallRecipeMap(GTRecipeType smallRecipeMap) {
        return (GTMSRecipeType) super.setSmallRecipeMap(smallRecipeMap);
    }

    @Override
    public GTMSRecipeType getSmallRecipeMap() {
        return (GTMSRecipeType) super.getSmallRecipeMap();
    }

    @Override
    public @NotNull GTMSRecipeType setIconSupplier(@Nullable Supplier<ItemStack> iconSupplier) {
        return (GTMSRecipeType) super.setIconSupplier(iconSupplier);
    }

    @Override
    public @Nullable Supplier<ItemStack> getIconSupplier() {
        return super.getIconSupplier();
    }

    @Override
    public @NotNull GTMSRecipeType setSound(@Nullable SoundEntry sound) {
        return (GTMSRecipeType) super.setSound(sound);
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
    public @NotNull GTMSRecipeType setScanner(boolean isScanner) {
        return (GTMSRecipeType) super.setScanner(isScanner);
    }

    @Override
    public boolean isHasResearchSlot() {
        return super.isHasResearchSlot();
    }

    @Override
    public @NotNull GTMSRecipeType setHasResearchSlot(boolean hasResearchSlot) {
        return (GTMSRecipeType) super.setHasResearchSlot(hasResearchSlot);
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
    public @NotNull GTMSRecipeType setOffsetVoltageText(boolean offsetVoltageText) {
        return (GTMSRecipeType) super.setOffsetVoltageText(offsetVoltageText);
    }

    @Override
    public boolean isOffsetVoltageText() {
        return super.isOffsetVoltageText();
    }

    @Override
    public @NotNull GTMSRecipeType setVoltageTextOffset(int voltageTextOffset) {
        return (GTMSRecipeType) super.setVoltageTextOffset(voltageTextOffset);
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
}

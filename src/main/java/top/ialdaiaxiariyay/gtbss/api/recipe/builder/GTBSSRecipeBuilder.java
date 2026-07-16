package top.ialdaiaxiariyay.gtbss.api.recipe.builder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.ResearchRecipeBuilder;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicate;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.capability.recipe.ManaRecipeCapability;
import top.ialdaiaxiariyay.gtbss.api.recipe.ingredient.ManaStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTBSSRecipeBuilder extends GTRecipeBuilder {

    public GTBSSRecipeBuilder(ResourceLocation id, GTRecipeType recipeType) {
        super(id, recipeType);
    }

    public GTBSSRecipeBuilder(GTRecipe toCopy, GTRecipeType recipeType) {
        super(toCopy, recipeType);
    }

    public GTBSSRecipeBuilder copy(String id) {
        return copy(GTCEu.id(id));
    }

    public GTBSSRecipeBuilder copy(ResourceLocation id) {
        GTBSSRecipeBuilder copy = new GTBSSRecipeBuilder(id, this.recipeType);
        this.input.forEach((k, v) -> copy.input.put(k, new ArrayList<>(v)));
        this.output.forEach((k, v) -> copy.output.put(k, new ArrayList<>(v)));
        this.tickInput.forEach((k, v) -> copy.tickInput.put(k, new ArrayList<>(v)));
        this.tickOutput.forEach((k, v) -> copy.tickOutput.put(k, new ArrayList<>(v)));
        copy.inputChanceLogic.putAll(this.inputChanceLogic);
        copy.outputChanceLogic.putAll(this.outputChanceLogic);
        copy.tickInputChanceLogic.putAll(this.tickInputChanceLogic);
        copy.tickOutputChanceLogic.putAll(this.tickOutputChanceLogic);
        copy.conditions.addAll(this.conditions);
        copy.data = this.data.copy();
        copy.duration = this.duration;
        copy.chance = this.chance;
        copy.perTick = this.perTick;
        copy.recipeCategory = this.recipeCategory;
        copy.onSave = this.onSave;
        return copy;
    }

    @Override
    public GTBSSRecipeBuilder copyFrom(GTRecipeBuilder builder) {
        return (GTBSSRecipeBuilder) builder.copy(builder.id)
                .onSave(null)
                .recipeType(this.recipeType)
                .category(this.recipeCategory);
    }

    @Override
    protected Content makeContent(Object o) {
        return super.makeContent(o);
    }

    @Override
    public <T> GTBSSRecipeBuilder input(RecipeCapability<T> capability, T obj) {
        return (GTBSSRecipeBuilder) super.input(capability, obj);
    }

    @SafeVarargs
    @Override
    public final <T> GTBSSRecipeBuilder input(RecipeCapability<T> capability, T... obj) {
        return (GTBSSRecipeBuilder) super.input(capability, obj);
    }

    @Override
    public <T> GTBSSRecipeBuilder output(RecipeCapability<T> capability, T obj) {
        return (GTBSSRecipeBuilder) super.output(capability, obj);
    }

    @SafeVarargs
    @Override
    public final <T> GTBSSRecipeBuilder output(RecipeCapability<T> capability, T... obj) {
        return (GTBSSRecipeBuilder) super.output(capability, obj);
    }

    @Override
    public GTBSSRecipeBuilder addCondition(RecipeCondition<?> condition) {
        return (GTBSSRecipeBuilder) super.addCondition(condition);
    }

    @Override
    public GTBSSRecipeBuilder duration(int duration) {
        return (GTBSSRecipeBuilder) super.duration(duration);
    }

    public GTBSSRecipeBuilder inputMana(long mana) {
        return inputMana(mana, 1);
    }

    public GTBSSRecipeBuilder inputMana(long size, long count) {
        return input(ManaRecipeCapability.CAP, new ManaStack(size * count));
    }

    public GTBSSRecipeBuilder Manat(long mana) {
        return Manat(mana, 1);
    }

    public GTBSSRecipeBuilder Manat(long size, long count) {
        if (size == 0) {
            GTBSS.LOGGER.error("Manat can't be explicitly set to 0, id: {}", id);
        }
        if (count < 1) {
            GTCEu.LOGGER.error("Amperage must be a positive integer, id: {}", id);
        }
        var lastPerTick = perTick;
        perTick = true;
        if (size > 0) {
            tickInput.remove(ManaRecipeCapability.CAP);
            inputMana(size, count);
        } else if (size < 0) {
            tickOutput.remove(ManaRecipeCapability.CAP);
            outputMana(-size, count);
        }
        perTick = lastPerTick;
        return this;
    }

    public GTBSSRecipeBuilder outputMana(long mana) {
        return outputMana(mana, 1);
    }

    public GTBSSRecipeBuilder outputMana(long size, long count) {
        return output(ManaRecipeCapability.CAP, new ManaStack(size * count));
    }

    @Override
    public GTBSSRecipeBuilder inputEU(long eu) {
        return (GTBSSRecipeBuilder) super.inputEU(eu);
    }

    @Override
    public GTBSSRecipeBuilder inputEU(long voltage, long amperage) {
        return (GTBSSRecipeBuilder) super.inputEU(voltage, amperage);
    }

    @Override
    public GTBSSRecipeBuilder EUt(long eu) {
        return (GTBSSRecipeBuilder) super.EUt(eu);
    }

    @Override
    public GTBSSRecipeBuilder EUt(long voltage, long amperage) {
        return (GTBSSRecipeBuilder) super.EUt(voltage, amperage);
    }

    @Override
    public GTBSSRecipeBuilder outputEU(long eu) {
        return (GTBSSRecipeBuilder) super.outputEU(eu);
    }

    @Override
    public GTBSSRecipeBuilder outputEU(long voltage, long amperage) {
        return (GTBSSRecipeBuilder) super.outputEU(voltage, amperage);
    }

    @Override
    public GTBSSRecipeBuilder inputCWU(int cwu) {
        return (GTBSSRecipeBuilder) super.inputCWU(cwu);
    }

    @Override
    public GTBSSRecipeBuilder CWUt(int cwu) {
        return (GTBSSRecipeBuilder) super.CWUt(cwu);
    }

    @Override
    public GTBSSRecipeBuilder totalCWU(int cwu) {
        return (GTBSSRecipeBuilder) super.totalCWU(cwu);
    }

    @Override
    public GTBSSRecipeBuilder outputCWU(int cwu) {
        return (GTBSSRecipeBuilder) super.outputCWU(cwu);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(Object input) {
        return (GTBSSRecipeBuilder) super.inputItems(input);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(Object input, int count) {
        return (GTBSSRecipeBuilder) super.inputItems(input, count);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(Ingredient inputs) {
        return (GTBSSRecipeBuilder) super.inputItems(inputs);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(Ingredient... inputs) {
        return (GTBSSRecipeBuilder) super.inputItems(inputs);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(Ingredient inputs, int count) {
        return (GTBSSRecipeBuilder) super.inputItems(inputs, count);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(ItemStack input) {
        return (GTBSSRecipeBuilder) super.inputItems(input);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(ItemStack... inputs) {
        return (GTBSSRecipeBuilder) super.inputItems(inputs);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(TagKey<Item> tag, int amount) {
        return (GTBSSRecipeBuilder) super.inputItems(tag, amount);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(TagKey<Item> tag) {
        return (GTBSSRecipeBuilder) super.inputItems(tag);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(Item input, int amount) {
        return (GTBSSRecipeBuilder) super.inputItems(input, amount);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(Item input) {
        return (GTBSSRecipeBuilder) super.inputItems(input);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(Supplier<? extends Item> input) {
        return (GTBSSRecipeBuilder) super.inputItems(input);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(Supplier<? extends Item> input, int amount) {
        return (GTBSSRecipeBuilder) super.inputItems(input, amount);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(TagPrefix orePrefix, Material material) {
        return (GTBSSRecipeBuilder) super.inputItems(orePrefix, material);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(MaterialEntry input) {
        return (GTBSSRecipeBuilder) super.inputItems(input);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(MaterialEntry input, int count) {
        return (GTBSSRecipeBuilder) super.inputItems(input, count);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(TagPrefix tagPrefix, @NotNull Material material, int count) {
        return (GTBSSRecipeBuilder) super.inputItems(tagPrefix, material, count);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(MachineDefinition machine) {
        return (GTBSSRecipeBuilder) super.inputItems(machine);
    }

    @Override
    public GTBSSRecipeBuilder inputItems(MachineDefinition machine, int count) {
        return (GTBSSRecipeBuilder) super.inputItems(machine, count);
    }

    @Override
    public GTBSSRecipeBuilder inputItemRanged(IntProviderIngredient provider) {
        return (GTBSSRecipeBuilder) super.inputItemRanged(provider);
    }

    @Override
    public GTBSSRecipeBuilder inputItemsRanged(ItemStack input, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.inputItemsRanged(input, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder inputItemsRanged(Item input, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.inputItemsRanged(input, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder inputItemsRanged(Supplier<? extends ItemLike> input, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.inputItemsRanged(input, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder inputItemsRanged(TagPrefix orePrefix, Material material, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.inputItemsRanged(orePrefix, material, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder inputItemsRanged(MachineDefinition machine, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.inputItemsRanged(machine, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder inputItemNbtPredicate(ItemStack stack, NBTPredicate predicate) {
        return (GTBSSRecipeBuilder) super.inputItemNbtPredicate(stack, predicate);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(Object output) {
        return (GTBSSRecipeBuilder) super.outputItems(output);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(Object output, int count) {
        return (GTBSSRecipeBuilder) super.outputItems(output, count);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(ItemStack output) {
        return (GTBSSRecipeBuilder) super.outputItems(output);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(ItemStack... outputs) {
        return (GTBSSRecipeBuilder) super.outputItems(outputs);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(Item output, int amount) {
        return (GTBSSRecipeBuilder) super.outputItems(output, amount);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(Item output) {
        return (GTBSSRecipeBuilder) super.outputItems(output);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(Supplier<? extends ItemLike> input) {
        return (GTBSSRecipeBuilder) super.outputItems(input);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(Supplier<? extends ItemLike> input, int amount) {
        return (GTBSSRecipeBuilder) super.outputItems(input, amount);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(TagPrefix orePrefix, Material material) {
        return (GTBSSRecipeBuilder) super.outputItems(orePrefix, material);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(TagPrefix orePrefix, @NotNull Material material, int count) {
        return (GTBSSRecipeBuilder) super.outputItems(orePrefix, material, count);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(MaterialEntry entry) {
        return (GTBSSRecipeBuilder) super.outputItems(entry);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(MaterialEntry entry, int count) {
        return (GTBSSRecipeBuilder) super.outputItems(entry, count);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(MachineDefinition machine) {
        return (GTBSSRecipeBuilder) super.outputItems(machine);
    }

    @Override
    public GTBSSRecipeBuilder outputItems(MachineDefinition machine, int count) {
        return (GTBSSRecipeBuilder) super.outputItems(machine, count);
    }

    @Override
    protected GTBSSRecipeBuilder outputItems(Ingredient ingredient) {
        return (GTBSSRecipeBuilder) super.outputItems(ingredient);
    }

    @Override
    public GTBSSRecipeBuilder outputItemRanged(IntProviderIngredient provider) {
        return (GTBSSRecipeBuilder) super.outputItemRanged(provider);
    }

    @Override
    public GTBSSRecipeBuilder outputItemsRanged(ItemStack output, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.outputItemsRanged(output, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder outputItemsRanged(Item input, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.outputItemsRanged(input, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder outputItemsRanged(Supplier<? extends ItemLike> output, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.outputItemsRanged(output, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder outputItemsRanged(TagPrefix orePrefix, Material material, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.outputItemsRanged(orePrefix, material, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder outputItemsRanged(MachineDefinition machine, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.outputItemsRanged(machine, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder notConsumable(ItemStack itemStack) {
        return (GTBSSRecipeBuilder) super.notConsumable(itemStack);
    }

    @Override
    public GTBSSRecipeBuilder notConsumable(Ingredient ingredient) {
        return (GTBSSRecipeBuilder) super.notConsumable(ingredient);
    }

    @Override
    public GTBSSRecipeBuilder notConsumable(Item item) {
        return (GTBSSRecipeBuilder) super.notConsumable(item);
    }

    @Override
    public GTBSSRecipeBuilder notConsumable(Supplier<? extends Item> item) {
        return (GTBSSRecipeBuilder) super.notConsumable(item);
    }

    @Override
    public GTBSSRecipeBuilder notConsumable(TagPrefix orePrefix, Material material) {
        return (GTBSSRecipeBuilder) super.notConsumable(orePrefix, material);
    }

    @Override
    public GTBSSRecipeBuilder notConsumable(TagPrefix orePrefix, Material material, int count) {
        return (GTBSSRecipeBuilder) super.notConsumable(orePrefix, material, count);
    }

    @Override
    public GTBSSRecipeBuilder notConsumableFluid(FluidStack fluid) {
        return (GTBSSRecipeBuilder) super.notConsumableFluid(fluid);
    }

    @Override
    public GTBSSRecipeBuilder notConsumableFluid(FluidIngredient ingredient) {
        return (GTBSSRecipeBuilder) super.notConsumableFluid(ingredient);
    }

    @Override
    public GTBSSRecipeBuilder circuitMeta(int configuration) {
        return (GTBSSRecipeBuilder) super.circuitMeta(configuration);
    }

    @Override
    public GTBSSRecipeBuilder chancedInput(Ingredient stack, int chance) {
        return (GTBSSRecipeBuilder) super.chancedInput(stack, chance);
    }

    @Override
    public GTBSSRecipeBuilder chancedInput(FluidIngredient stack, int chance) {
        return (GTBSSRecipeBuilder) super.chancedInput(stack, chance);
    }

    @Override
    public GTBSSRecipeBuilder chancedOutput(Ingredient stack, int chance) {
        return (GTBSSRecipeBuilder) super.chancedOutput(stack, chance);
    }

    @Override
    public GTBSSRecipeBuilder chancedOutput(FluidIngredient stack, int chance) {
        return (GTBSSRecipeBuilder) super.chancedOutput(stack, chance);
    }

    @Override
    public GTBSSRecipeBuilder chancedInput(ItemStack stack, int chance) {
        return (GTBSSRecipeBuilder) super.chancedInput(stack, chance);
    }

    @Override
    public GTBSSRecipeBuilder chancedInput(FluidStack stack, int chance) {
        return (GTBSSRecipeBuilder) super.chancedInput(stack, chance);
    }

    @Override
    public GTBSSRecipeBuilder chancedOutput(ItemStack stack, int chance) {
        return (GTBSSRecipeBuilder) super.chancedOutput(stack, chance);
    }

    @Override
    public GTBSSRecipeBuilder chancedOutput(FluidStack stack, int chance) {
        return (GTBSSRecipeBuilder) super.chancedOutput(stack, chance);
    }

    @Override
    public GTBSSRecipeBuilder chancedOutput(TagPrefix tag, Material mat, int chance) {
        return (GTBSSRecipeBuilder) super.chancedOutput(tag, mat, chance);
    }

    @Override
    public GTBSSRecipeBuilder chancedOutput(TagPrefix tag, Material mat, int count, int chance) {
        return (GTBSSRecipeBuilder) super.chancedOutput(tag, mat, count, chance);
    }

    @Override
    public GTBSSRecipeBuilder chancedOutput(ItemStack stack, String fraction) {
        return (GTBSSRecipeBuilder) super.chancedOutput(stack, fraction);
    }

    @Override
    public GTBSSRecipeBuilder chancedOutput(TagPrefix prefix, Material material, int count, String fraction) {
        return (GTBSSRecipeBuilder) super.chancedOutput(prefix, material, count, fraction);
    }

    @Override
    public GTBSSRecipeBuilder chancedOutput(TagPrefix prefix, Material material, String fraction) {
        return (GTBSSRecipeBuilder) super.chancedOutput(prefix, material, fraction);
    }

    @Override
    public GTBSSRecipeBuilder chancedOutput(Item item, int count, String fraction) {
        return (GTBSSRecipeBuilder) super.chancedOutput(item, count, fraction);
    }

    @Override
    public GTBSSRecipeBuilder chancedOutput(Item item, String fraction) {
        return (GTBSSRecipeBuilder) super.chancedOutput(item, fraction);
    }

    @Override
    public GTBSSRecipeBuilder chancedFluidOutput(FluidStack stack, String fraction) {
        return (GTBSSRecipeBuilder) super.chancedFluidOutput(stack, fraction);
    }

    @Override
    public GTBSSRecipeBuilder chancedOutputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
        return (GTBSSRecipeBuilder) super.chancedOutputLogic(cap, logic);
    }

    @Override
    public GTBSSRecipeBuilder chancedItemOutputLogic(ChanceLogic logic) {
        return (GTBSSRecipeBuilder) super.chancedItemOutputLogic(logic);
    }

    @Override
    public GTBSSRecipeBuilder chancedFluidOutputLogic(ChanceLogic logic) {
        return (GTBSSRecipeBuilder) super.chancedFluidOutputLogic(logic);
    }

    @Override
    public GTBSSRecipeBuilder chancedInputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
        return (GTBSSRecipeBuilder) super.chancedInputLogic(cap, logic);
    }

    @Override
    public GTBSSRecipeBuilder chancedItemInputLogic(ChanceLogic logic) {
        return (GTBSSRecipeBuilder) super.chancedItemInputLogic(logic);
    }

    @Override
    public GTBSSRecipeBuilder chancedFluidInputLogic(ChanceLogic logic) {
        return (GTBSSRecipeBuilder) super.chancedFluidInputLogic(logic);
    }

    @Override
    public GTBSSRecipeBuilder chancedTickOutputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
        return (GTBSSRecipeBuilder) super.chancedTickOutputLogic(cap, logic);
    }

    @Override
    public GTBSSRecipeBuilder chancedTickInputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
        return (GTBSSRecipeBuilder) super.chancedTickInputLogic(cap, logic);
    }

    @Override
    public GTBSSRecipeBuilder inputFluids(@NotNull Material material, int amount) {
        return (GTBSSRecipeBuilder) super.inputFluids(material, amount);
    }

    @Override
    public GTBSSRecipeBuilder inputFluids(FluidStack input) {
        return (GTBSSRecipeBuilder) super.inputFluids(input);
    }

    @Override
    public GTBSSRecipeBuilder inputFluids(FluidStack... inputs) {
        return (GTBSSRecipeBuilder) super.inputFluids(inputs);
    }

    @Override
    public GTBSSRecipeBuilder inputFluidsRanged(IntProviderFluidIngredient provider) {
        return (GTBSSRecipeBuilder) super.inputFluidsRanged(provider);
    }

    @Override
    protected GTBSSRecipeBuilder inputFluidsRanged(FluidIngredient input, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.inputFluidsRanged(input, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder inputFluidsRanged(FluidStack input, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.inputFluidsRanged(input, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder inputFluids(FluidIngredient... inputs) {
        return (GTBSSRecipeBuilder) super.inputFluids(inputs);
    }

    @Override
    public GTBSSRecipeBuilder outputFluids(FluidStack output) {
        return (GTBSSRecipeBuilder) super.outputFluids(output);
    }

    @Override
    public GTBSSRecipeBuilder outputFluids(FluidStack... outputs) {
        return (GTBSSRecipeBuilder) super.outputFluids(outputs);
    }

    @Override
    public GTBSSRecipeBuilder outputFluids(FluidIngredient... outputs) {
        return (GTBSSRecipeBuilder) super.outputFluids(outputs);
    }

    @Override
    public GTBSSRecipeBuilder outputFluidsRanged(IntProviderFluidIngredient provider) {
        return (GTBSSRecipeBuilder) super.outputFluidsRanged(provider);
    }

    @Override
    protected GTBSSRecipeBuilder outputFluidsRanged(FluidIngredient output, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.outputFluidsRanged(output, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder outputFluidsRanged(FluidStack output, IntProvider intProvider) {
        return (GTBSSRecipeBuilder) super.outputFluidsRanged(output, intProvider);
    }

    @Override
    public GTBSSRecipeBuilder addData(String key, Tag data) {
        return (GTBSSRecipeBuilder) super.addData(key, data);
    }

    @Override
    public GTBSSRecipeBuilder addData(String key, int data) {
        return (GTBSSRecipeBuilder) super.addData(key, data);
    }

    @Override
    public GTBSSRecipeBuilder addData(String key, long data) {
        return (GTBSSRecipeBuilder) super.addData(key, data);
    }

    @Override
    public GTBSSRecipeBuilder addData(String key, String data) {
        return (GTBSSRecipeBuilder) super.addData(key, data);
    }

    @Override
    public GTBSSRecipeBuilder addData(String key, float data) {
        return (GTBSSRecipeBuilder) super.addData(key, data);
    }

    @Override
    public GTBSSRecipeBuilder addData(String key, boolean data) {
        return (GTBSSRecipeBuilder) super.addData(key, data);
    }

    @Override
    public GTBSSRecipeBuilder blastFurnaceTemp(int blastTemp) {
        return (GTBSSRecipeBuilder) super.blastFurnaceTemp(blastTemp);
    }

    @Override
    public GTBSSRecipeBuilder explosivesAmount(int explosivesAmount) {
        return (GTBSSRecipeBuilder) super.explosivesAmount(explosivesAmount);
    }

    @Override
    public GTBSSRecipeBuilder explosivesType(ItemStack explosivesType) {
        return (GTBSSRecipeBuilder) super.explosivesType(explosivesType);
    }

    @Override
    public GTBSSRecipeBuilder solderMultiplier(int multiplier) {
        return (GTBSSRecipeBuilder) super.solderMultiplier(multiplier);
    }

    @Override
    public GTBSSRecipeBuilder disableDistilleryRecipes(boolean flag) {
        return (GTBSSRecipeBuilder) super.disableDistilleryRecipes(flag);
    }

    @Override
    public GTBSSRecipeBuilder fusionStartEU(long eu) {
        return (GTBSSRecipeBuilder) super.fusionStartEU(eu);
    }

    @Override
    public GTBSSRecipeBuilder researchScan(boolean isScan) {
        return (GTBSSRecipeBuilder) super.researchScan(isScan);
    }

    @Override
    public GTBSSRecipeBuilder durationIsTotalCWU(boolean durationIsTotalCWU) {
        return (GTBSSRecipeBuilder) super.durationIsTotalCWU(durationIsTotalCWU);
    }

    @Override
    public GTBSSRecipeBuilder hideDuration(boolean hideDuration) {
        return (GTBSSRecipeBuilder) super.hideDuration(hideDuration);
    }

    @Override
    public GTBSSRecipeBuilder cleanroom(CleanroomType cleanroomType) {
        return (GTBSSRecipeBuilder) super.cleanroom(cleanroomType);
    }

    @Override
    public GTBSSRecipeBuilder dimension(ResourceLocation dimension, boolean reverse) {
        return (GTBSSRecipeBuilder) super.dimension(dimension, reverse);
    }

    @Override
    public GTBSSRecipeBuilder dimension(ResourceLocation dimension) {
        return (GTBSSRecipeBuilder) super.dimension(dimension);
    }

    @Override
    public GTBSSRecipeBuilder dimension(ResourceKey<Level> dimension, boolean reverse) {
        return (GTBSSRecipeBuilder) super.dimension(dimension, reverse);
    }

    @Override
    public GTBSSRecipeBuilder dimension(ResourceKey<Level> dimension) {
        return (GTBSSRecipeBuilder) super.dimension(dimension);
    }

    @Override
    public GTBSSRecipeBuilder biome(ResourceLocation biome, boolean reverse) {
        return (GTBSSRecipeBuilder) super.biome(biome, reverse);
    }

    @Override
    public GTBSSRecipeBuilder biome(ResourceLocation biome) {
        return (GTBSSRecipeBuilder) super.biome(biome);
    }

    @Override
    public GTBSSRecipeBuilder biome(ResourceKey<Biome> biome, boolean reverse) {
        return (GTBSSRecipeBuilder) super.biome(biome, reverse);
    }

    @Override
    public GTBSSRecipeBuilder biome(ResourceKey<Biome> biome) {
        return (GTBSSRecipeBuilder) super.biome(biome);
    }

    @Override
    public GTBSSRecipeBuilder biomeTag(TagKey<Biome> biome, boolean reverse) {
        return (GTBSSRecipeBuilder) super.biomeTag(biome, reverse);
    }

    @Override
    public GTBSSRecipeBuilder biomeTag(TagKey<Biome> biome) {
        return (GTBSSRecipeBuilder) super.biomeTag(biome);
    }

    @Override
    public GTBSSRecipeBuilder rain(float level, boolean reverse) {
        return (GTBSSRecipeBuilder) super.rain(level, reverse);
    }

    @Override
    public GTBSSRecipeBuilder rain(float level) {
        return (GTBSSRecipeBuilder) super.rain(level);
    }

    @Override
    public GTBSSRecipeBuilder thunder(float level, boolean reverse) {
        return (GTBSSRecipeBuilder) super.thunder(level, reverse);
    }

    @Override
    public GTBSSRecipeBuilder thunder(float level) {
        return (GTBSSRecipeBuilder) super.thunder(level);
    }

    @Override
    public GTBSSRecipeBuilder posY(int min, int max, boolean reverse) {
        return (GTBSSRecipeBuilder) super.posY(min, max, reverse);
    }

    @Override
    public GTBSSRecipeBuilder posY(int min, int max) {
        return (GTBSSRecipeBuilder) super.posY(min, max);
    }

    @Override
    public GTBSSRecipeBuilder environmentalHazard(MedicalCondition condition, boolean reverse) {
        return (GTBSSRecipeBuilder) super.environmentalHazard(condition, reverse);
    }

    @Override
    public GTBSSRecipeBuilder environmentalHazard(MedicalCondition condition) {
        return (GTBSSRecipeBuilder) super.environmentalHazard(condition);
    }

    @Override
    public GTBSSRecipeBuilder adjacentFluids(Collection<HolderSet<Fluid>> fluids) {
        return (GTBSSRecipeBuilder) super.adjacentFluids(fluids);
    }

    @Override
    public GTBSSRecipeBuilder adjacentFluids(Collection<HolderSet<Fluid>> fluids, boolean isReverse) {
        return (GTBSSRecipeBuilder) super.adjacentFluids(fluids, isReverse);
    }

    @Override
    public GTBSSRecipeBuilder adjacentBlocks(Block... blocks) {
        return (GTBSSRecipeBuilder) super.adjacentBlocks(blocks);
    }

    @Override
    public GTBSSRecipeBuilder adjacentBlocks(boolean isReverse, Block... blocks) {
        return (GTBSSRecipeBuilder) super.adjacentBlocks(isReverse, blocks);
    }

    @Override
    public GTBSSRecipeBuilder adjacentBlocks(Collection<HolderSet<Block>> blocks) {
        return (GTBSSRecipeBuilder) super.adjacentBlocks(blocks);
    }

    @Override
    public GTBSSRecipeBuilder adjacentBlocks(Collection<HolderSet<Block>> blocks, boolean isReverse) {
        return (GTBSSRecipeBuilder) super.adjacentBlocks(blocks, isReverse);
    }

    @Override
    public GTBSSRecipeBuilder daytime(boolean isNight) {
        return (GTBSSRecipeBuilder) super.daytime(isNight);
    }

    @Override
    public GTBSSRecipeBuilder daytime() {
        return (GTBSSRecipeBuilder) super.daytime();
    }

    @Override
    public GTBSSRecipeBuilder heraclesQuest(String questId, boolean isReverse) {
        return (GTBSSRecipeBuilder) super.heraclesQuest(questId, isReverse);
    }

    @Override
    public GTBSSRecipeBuilder heraclesQuest(String questId) {
        return (GTBSSRecipeBuilder) super.heraclesQuest(questId);
    }

    @Override
    public GTBSSRecipeBuilder gameStage(String stageName) {
        return (GTBSSRecipeBuilder) super.gameStage(stageName);
    }

    @Override
    public GTBSSRecipeBuilder gameStage(String stageName, boolean isReverse) {
        return (GTBSSRecipeBuilder) super.gameStage(stageName, isReverse);
    }

    @Override
    public GTBSSRecipeBuilder ftbQuest(String questId, boolean isReverse) {
        return (GTBSSRecipeBuilder) super.ftbQuest(questId, isReverse);
    }

    @Override
    public GTBSSRecipeBuilder ftbQuest(String questId) {
        return (GTBSSRecipeBuilder) super.ftbQuest(questId);
    }

    @Override
    public GTBSSRecipeBuilder researchWithoutRecipe(@NotNull String researchId) {
        return (GTBSSRecipeBuilder) super.researchWithoutRecipe(researchId);
    }

    @Override
    public GTBSSRecipeBuilder researchWithoutRecipe(@NotNull String researchId, @NotNull ItemStack dataStack) {
        return (GTBSSRecipeBuilder) super.researchWithoutRecipe(researchId, dataStack);
    }

    @Override
    public GTBSSRecipeBuilder scannerResearch(UnaryOperator<ResearchRecipeBuilder.ScannerRecipeBuilder> research) {
        return (GTBSSRecipeBuilder) super.scannerResearch(research);
    }

    @Override
    public GTBSSRecipeBuilder scannerResearch(@NotNull ItemStack researchStack) {
        return (GTBSSRecipeBuilder) super.scannerResearch(researchStack);
    }

    @Override
    public GTBSSRecipeBuilder stationResearch(UnaryOperator<ResearchRecipeBuilder.StationRecipeBuilder> research) {
        return (GTBSSRecipeBuilder) super.stationResearch(research);
    }

    @Override
    public GTBSSRecipeBuilder category(@NotNull GTRecipeCategory category) {
        return (GTBSSRecipeBuilder) super.category(category);
    }

    @Override
    public GTBSSRecipeBuilder addMaterialInfo(boolean item) {
        return (GTBSSRecipeBuilder) super.addMaterialInfo(item);
    }

    @Override
    public GTBSSRecipeBuilder addMaterialInfo(boolean item, boolean fluid) {
        return (GTBSSRecipeBuilder) super.addMaterialInfo(item, fluid);
    }

    @Override
    public GTBSSRecipeBuilder removePreviousMaterialInfo() {
        return (GTBSSRecipeBuilder) super.removePreviousMaterialInfo();
    }

    @Override
    public GTBSSRecipeBuilder setTempItemMaterialStacks(List<MaterialStack> stacks) {
        return (GTBSSRecipeBuilder) super.setTempItemMaterialStacks(stacks);
    }

    @Override
    public GTBSSRecipeBuilder setTempFluidMaterialStacks(List<MaterialStack> stacks) {
        return (GTBSSRecipeBuilder) super.setTempFluidMaterialStacks(stacks);
    }

    @Override
    public GTBSSRecipeBuilder setTempItemStacks(List<ItemStack> stacks) {
        return (GTBSSRecipeBuilder) super.setTempItemStacks(stacks);
    }

    @Override
    public void toJson(JsonObject json) {
        super.toJson(json);
    }

    @Override
    public JsonObject capabilitiesToJson(Map<RecipeCapability<?>, List<Content>> contents) {
        return super.capabilitiesToJson(contents);
    }

    @Override
    public JsonObject chanceLogicsToJson(Map<RecipeCapability<?>, ChanceLogic> chanceLogics) {
        return super.chanceLogicsToJson(chanceLogics);
    }

    @Override
    public FinishedRecipe build() {
        return super.build();
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer) {
        super.save(consumer);
    }

    @Override
    public GTRecipe buildRawRecipe() {
        return super.buildRawRecipe();
    }

    @Override
    protected void warnTooManyIngredients(RecipeCapability<?> capability, boolean isInput,
                                          Map<RecipeCapability<?>, List<Content>> table, int addedEntries) {
        super.warnTooManyIngredients(capability, isInput, table, addedEntries);
    }

    @Override
    protected boolean missingIngredientError(int index, boolean isInput, RecipeCapability<?> cap,
                                             BooleanSupplier empty) {
        return super.missingIngredientError(index, isInput, cap, empty);
    }

    @Override
    protected boolean checkChanceAndPrintError(int chance) {
        return super.checkChanceAndPrintError(chance);
    }

    @Override
    public EnergyStack EUt() {
        return super.EUt();
    }

    @Override
    public int getSolderMultiplier() {
        return super.getSolderMultiplier();
    }

    @Override
    public @NotNull GTRecipeBuilder id(ResourceLocation id) {
        return super.id(id);
    }

    @Override
    public @NotNull GTRecipeBuilder recipeType(GTRecipeType recipeType) {
        return super.recipeType(recipeType);
    }

    @Override
    public @NotNull GTRecipeBuilder perTick(boolean perTick) {
        return super.perTick(perTick);
    }

    @Override
    public @NotNull GTBSSRecipeBuilder chance(int chance) {
        return (GTBSSRecipeBuilder) super.chance(chance);
    }

    @Override
    public @NotNull GTBSSRecipeBuilder maxChance(int maxChance) {
        return (GTBSSRecipeBuilder) super.maxChance(maxChance);
    }

    @Override
    public @NotNull GTBSSRecipeBuilder onSave(@Nullable BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onSave) {
        return (GTBSSRecipeBuilder) super.onSave(onSave);
    }

    @Override
    public Collection<ResearchRecipeEntry> researchRecipeEntries() {
        return super.researchRecipeEntries();
    }
}

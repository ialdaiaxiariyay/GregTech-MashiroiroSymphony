package top.ialdaiaxiariyay.gtms.api.recipe.builder;

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
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.capability.recipe.ManaRecipeCapability;
import top.ialdaiaxiariyay.gtms.api.recipe.ingredient.ManaStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTMSRecipeBuilder extends GTRecipeBuilder {

    public GTMSRecipeBuilder(ResourceLocation id, GTRecipeType recipeType) {
        super(id, recipeType);
    }

    public GTMSRecipeBuilder(GTRecipe toCopy, GTRecipeType recipeType) {
        super(toCopy, recipeType);
    }

    public GTMSRecipeBuilder copy(String id) {
        return copy(GTCEu.id(id));
    }

    public GTMSRecipeBuilder copy(ResourceLocation id) {
        GTMSRecipeBuilder copy = new GTMSRecipeBuilder(id, this.recipeType);
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
    public GTMSRecipeBuilder copyFrom(GTRecipeBuilder builder) {
        return (GTMSRecipeBuilder) builder.copy(builder.id)
                .onSave(null)
                .recipeType(this.recipeType)
                .category(this.recipeCategory);
    }

    @Override
    protected Content makeContent(Object o) {
        return super.makeContent(o);
    }

    @Override
    public <T> GTMSRecipeBuilder input(RecipeCapability<T> capability, T obj) {
        return (GTMSRecipeBuilder) super.input(capability, obj);
    }

    @SafeVarargs
    @Override
    public final <T> GTMSRecipeBuilder input(RecipeCapability<T> capability, T... obj) {
        return (GTMSRecipeBuilder) super.input(capability, obj);
    }

    @Override
    public <T> GTMSRecipeBuilder output(RecipeCapability<T> capability, T obj) {
        return (GTMSRecipeBuilder) super.output(capability, obj);
    }

    @SafeVarargs
    @Override
    public final <T> GTMSRecipeBuilder output(RecipeCapability<T> capability, T... obj) {
        return (GTMSRecipeBuilder) super.output(capability, obj);
    }

    @Override
    public GTMSRecipeBuilder addCondition(RecipeCondition<?> condition) {
        return (GTMSRecipeBuilder) super.addCondition(condition);
    }

    @Override
    public GTMSRecipeBuilder duration(int duration) {
        return (GTMSRecipeBuilder) super.duration(duration);
    }

    public GTMSRecipeBuilder inputMana(long mana) {
        return inputMana(mana, 1);
    }

    public GTMSRecipeBuilder inputMana(long size, long count) {
        return input(ManaRecipeCapability.CAP, new ManaStack(size * count));
    }

    public GTMSRecipeBuilder Manat(long mana) {
        return Manat(mana, 1);
    }

    public GTMSRecipeBuilder Manat(long size, long count) {
        if (size == 0) {
            GTMS.LOGGER.error("Manat can't be explicitly set to 0, id: {}", id);
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

    public GTMSRecipeBuilder outputMana(long mana) {
        return outputMana(mana, 1);
    }

    public GTMSRecipeBuilder outputMana(long size, long count) {
        return output(ManaRecipeCapability.CAP, new ManaStack(size * count));
    }

    @Override
    public GTMSRecipeBuilder inputEU(long eu) {
        return (GTMSRecipeBuilder) super.inputEU(eu);
    }

    @Override
    public GTMSRecipeBuilder inputEU(long voltage, long amperage) {
        return (GTMSRecipeBuilder) super.inputEU(voltage, amperage);
    }

    @Override
    public GTMSRecipeBuilder EUt(long eu) {
        return (GTMSRecipeBuilder) super.EUt(eu);
    }

    @Override
    public GTMSRecipeBuilder EUt(long voltage, long amperage) {
        return (GTMSRecipeBuilder) super.EUt(voltage, amperage);
    }

    @Override
    public GTMSRecipeBuilder outputEU(long eu) {
        return (GTMSRecipeBuilder) super.outputEU(eu);
    }

    @Override
    public GTMSRecipeBuilder outputEU(long voltage, long amperage) {
        return (GTMSRecipeBuilder) super.outputEU(voltage, amperage);
    }

    @Override
    public GTMSRecipeBuilder inputCWU(int cwu) {
        return (GTMSRecipeBuilder) super.inputCWU(cwu);
    }

    @Override
    public GTMSRecipeBuilder CWUt(int cwu) {
        return (GTMSRecipeBuilder) super.CWUt(cwu);
    }

    @Override
    public GTMSRecipeBuilder totalCWU(int cwu) {
        return (GTMSRecipeBuilder) super.totalCWU(cwu);
    }

    @Override
    public GTMSRecipeBuilder outputCWU(int cwu) {
        return (GTMSRecipeBuilder) super.outputCWU(cwu);
    }

    @Override
    public GTMSRecipeBuilder inputItems(Object input) {
        return (GTMSRecipeBuilder) super.inputItems(input);
    }

    @Override
    public GTMSRecipeBuilder inputItems(Object input, int count) {
        return (GTMSRecipeBuilder) super.inputItems(input, count);
    }

    @Override
    public GTMSRecipeBuilder inputItems(Ingredient inputs) {
        return (GTMSRecipeBuilder) super.inputItems(inputs);
    }

    @Override
    public GTMSRecipeBuilder inputItems(Ingredient... inputs) {
        return (GTMSRecipeBuilder) super.inputItems(inputs);
    }

    @Override
    public GTMSRecipeBuilder inputItems(Ingredient inputs, int count) {
        return (GTMSRecipeBuilder) super.inputItems(inputs, count);
    }

    @Override
    public GTMSRecipeBuilder inputItems(ItemStack input) {
        return (GTMSRecipeBuilder) super.inputItems(input);
    }

    @Override
    public GTMSRecipeBuilder inputItems(ItemStack... inputs) {
        return (GTMSRecipeBuilder) super.inputItems(inputs);
    }

    @Override
    public GTMSRecipeBuilder inputItems(TagKey<Item> tag, int amount) {
        return (GTMSRecipeBuilder) super.inputItems(tag, amount);
    }

    @Override
    public GTMSRecipeBuilder inputItems(TagKey<Item> tag) {
        return (GTMSRecipeBuilder) super.inputItems(tag);
    }

    @Override
    public GTMSRecipeBuilder inputItems(Item input, int amount) {
        return (GTMSRecipeBuilder) super.inputItems(input, amount);
    }

    @Override
    public GTMSRecipeBuilder inputItems(Item input) {
        return (GTMSRecipeBuilder) super.inputItems(input);
    }

    @Override
    public GTMSRecipeBuilder inputItems(Supplier<? extends Item> input) {
        return (GTMSRecipeBuilder) super.inputItems(input);
    }

    @Override
    public GTMSRecipeBuilder inputItems(Supplier<? extends Item> input, int amount) {
        return (GTMSRecipeBuilder) super.inputItems(input, amount);
    }

    @Override
    public GTMSRecipeBuilder inputItems(TagPrefix orePrefix, Material material) {
        return (GTMSRecipeBuilder) super.inputItems(orePrefix, material);
    }

    @Override
    public GTMSRecipeBuilder inputItems(MaterialEntry input) {
        return (GTMSRecipeBuilder) super.inputItems(input);
    }

    @Override
    public GTMSRecipeBuilder inputItems(MaterialEntry input, int count) {
        return (GTMSRecipeBuilder) super.inputItems(input, count);
    }

    @Override
    public GTMSRecipeBuilder inputItems(TagPrefix tagPrefix, @NotNull Material material, int count) {
        return (GTMSRecipeBuilder) super.inputItems(tagPrefix, material, count);
    }

    @Override
    public GTMSRecipeBuilder inputItems(MachineDefinition machine) {
        return (GTMSRecipeBuilder) super.inputItems(machine);
    }

    @Override
    public GTMSRecipeBuilder inputItems(MachineDefinition machine, int count) {
        return (GTMSRecipeBuilder) super.inputItems(machine, count);
    }

    @Override
    public GTMSRecipeBuilder inputItemRanged(IntProviderIngredient provider) {
        return (GTMSRecipeBuilder) super.inputItemRanged(provider);
    }

    @Override
    public GTMSRecipeBuilder inputItemsRanged(ItemStack input, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.inputItemsRanged(input, intProvider);
    }

    @Override
    public GTMSRecipeBuilder inputItemsRanged(Item input, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.inputItemsRanged(input, intProvider);
    }

    @Override
    public GTMSRecipeBuilder inputItemsRanged(Supplier<? extends ItemLike> input, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.inputItemsRanged(input, intProvider);
    }

    @Override
    public GTMSRecipeBuilder inputItemsRanged(TagPrefix orePrefix, Material material, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.inputItemsRanged(orePrefix, material, intProvider);
    }

    @Override
    public GTMSRecipeBuilder inputItemsRanged(MachineDefinition machine, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.inputItemsRanged(machine, intProvider);
    }

    @Override
    public GTMSRecipeBuilder inputItemNbtPredicate(ItemStack stack, NBTPredicate predicate) {
        return (GTMSRecipeBuilder) super.inputItemNbtPredicate(stack, predicate);
    }

    @Override
    public GTMSRecipeBuilder outputItems(Object output) {
        return (GTMSRecipeBuilder) super.outputItems(output);
    }

    @Override
    public GTMSRecipeBuilder outputItems(Object output, int count) {
        return (GTMSRecipeBuilder) super.outputItems(output, count);
    }

    @Override
    public GTMSRecipeBuilder outputItems(ItemStack output) {
        return (GTMSRecipeBuilder) super.outputItems(output);
    }

    @Override
    public GTMSRecipeBuilder outputItems(ItemStack... outputs) {
        return (GTMSRecipeBuilder) super.outputItems(outputs);
    }

    @Override
    public GTMSRecipeBuilder outputItems(Item output, int amount) {
        return (GTMSRecipeBuilder) super.outputItems(output, amount);
    }

    @Override
    public GTMSRecipeBuilder outputItems(Item output) {
        return (GTMSRecipeBuilder) super.outputItems(output);
    }

    @Override
    public GTMSRecipeBuilder outputItems(Supplier<? extends ItemLike> input) {
        return (GTMSRecipeBuilder) super.outputItems(input);
    }

    @Override
    public GTMSRecipeBuilder outputItems(Supplier<? extends ItemLike> input, int amount) {
        return (GTMSRecipeBuilder) super.outputItems(input, amount);
    }

    @Override
    public GTMSRecipeBuilder outputItems(TagPrefix orePrefix, Material material) {
        return (GTMSRecipeBuilder) super.outputItems(orePrefix, material);
    }

    @Override
    public GTMSRecipeBuilder outputItems(TagPrefix orePrefix, @NotNull Material material, int count) {
        return (GTMSRecipeBuilder) super.outputItems(orePrefix, material, count);
    }

    @Override
    public GTMSRecipeBuilder outputItems(MaterialEntry entry) {
        return (GTMSRecipeBuilder) super.outputItems(entry);
    }

    @Override
    public GTMSRecipeBuilder outputItems(MaterialEntry entry, int count) {
        return (GTMSRecipeBuilder) super.outputItems(entry, count);
    }

    @Override
    public GTMSRecipeBuilder outputItems(MachineDefinition machine) {
        return (GTMSRecipeBuilder) super.outputItems(machine);
    }

    @Override
    public GTMSRecipeBuilder outputItems(MachineDefinition machine, int count) {
        return (GTMSRecipeBuilder) super.outputItems(machine, count);
    }

    @Override
    protected GTMSRecipeBuilder outputItems(Ingredient ingredient) {
        return (GTMSRecipeBuilder) super.outputItems(ingredient);
    }

    @Override
    public GTMSRecipeBuilder outputItemRanged(IntProviderIngredient provider) {
        return (GTMSRecipeBuilder) super.outputItemRanged(provider);
    }

    @Override
    public GTMSRecipeBuilder outputItemsRanged(ItemStack output, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.outputItemsRanged(output, intProvider);
    }

    @Override
    public GTMSRecipeBuilder outputItemsRanged(Item input, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.outputItemsRanged(input, intProvider);
    }

    @Override
    public GTMSRecipeBuilder outputItemsRanged(Supplier<? extends ItemLike> output, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.outputItemsRanged(output, intProvider);
    }

    @Override
    public GTMSRecipeBuilder outputItemsRanged(TagPrefix orePrefix, Material material, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.outputItemsRanged(orePrefix, material, intProvider);
    }

    @Override
    public GTMSRecipeBuilder outputItemsRanged(MachineDefinition machine, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.outputItemsRanged(machine, intProvider);
    }

    @Override
    public GTMSRecipeBuilder notConsumable(ItemStack itemStack) {
        return (GTMSRecipeBuilder) super.notConsumable(itemStack);
    }

    @Override
    public GTMSRecipeBuilder notConsumable(Ingredient ingredient) {
        return (GTMSRecipeBuilder) super.notConsumable(ingredient);
    }

    @Override
    public GTMSRecipeBuilder notConsumable(Item item) {
        return (GTMSRecipeBuilder) super.notConsumable(item);
    }

    @Override
    public GTMSRecipeBuilder notConsumable(Supplier<? extends Item> item) {
        return (GTMSRecipeBuilder) super.notConsumable(item);
    }

    @Override
    public GTMSRecipeBuilder notConsumable(TagPrefix orePrefix, Material material) {
        return (GTMSRecipeBuilder) super.notConsumable(orePrefix, material);
    }

    @Override
    public GTMSRecipeBuilder notConsumable(TagPrefix orePrefix, Material material, int count) {
        return (GTMSRecipeBuilder) super.notConsumable(orePrefix, material, count);
    }

    @Override
    public GTMSRecipeBuilder notConsumableFluid(FluidStack fluid) {
        return (GTMSRecipeBuilder) super.notConsumableFluid(fluid);
    }

    @Override
    public GTMSRecipeBuilder notConsumableFluid(FluidIngredient ingredient) {
        return (GTMSRecipeBuilder) super.notConsumableFluid(ingredient);
    }

    @Override
    public GTMSRecipeBuilder circuitMeta(int configuration) {
        return (GTMSRecipeBuilder) super.circuitMeta(configuration);
    }

    @Override
    public GTMSRecipeBuilder chancedInput(Ingredient stack, int chance) {
        return (GTMSRecipeBuilder) super.chancedInput(stack, chance);
    }

    @Override
    public GTMSRecipeBuilder chancedInput(FluidIngredient stack, int chance) {
        return (GTMSRecipeBuilder) super.chancedInput(stack, chance);
    }

    @Override
    public GTMSRecipeBuilder chancedOutput(Ingredient stack, int chance) {
        return (GTMSRecipeBuilder) super.chancedOutput(stack, chance);
    }

    @Override
    public GTMSRecipeBuilder chancedOutput(FluidIngredient stack, int chance) {
        return (GTMSRecipeBuilder) super.chancedOutput(stack, chance);
    }

    @Override
    public GTMSRecipeBuilder chancedInput(ItemStack stack, int chance) {
        return (GTMSRecipeBuilder) super.chancedInput(stack, chance);
    }

    @Override
    public GTMSRecipeBuilder chancedInput(FluidStack stack, int chance) {
        return (GTMSRecipeBuilder) super.chancedInput(stack, chance);
    }

    @Override
    public GTMSRecipeBuilder chancedOutput(ItemStack stack, int chance) {
        return (GTMSRecipeBuilder) super.chancedOutput(stack, chance);
    }

    @Override
    public GTMSRecipeBuilder chancedOutput(FluidStack stack, int chance) {
        return (GTMSRecipeBuilder) super.chancedOutput(stack, chance);
    }

    @Override
    public GTMSRecipeBuilder chancedOutput(TagPrefix tag, Material mat, int chance) {
        return (GTMSRecipeBuilder) super.chancedOutput(tag, mat, chance);
    }

    @Override
    public GTMSRecipeBuilder chancedOutput(TagPrefix tag, Material mat, int count, int chance) {
        return (GTMSRecipeBuilder) super.chancedOutput(tag, mat, count, chance);
    }

    @Override
    public GTMSRecipeBuilder chancedOutput(ItemStack stack, String fraction) {
        return (GTMSRecipeBuilder) super.chancedOutput(stack, fraction);
    }

    @Override
    public GTMSRecipeBuilder chancedOutput(TagPrefix prefix, Material material, int count, String fraction) {
        return (GTMSRecipeBuilder) super.chancedOutput(prefix, material, count, fraction);
    }

    @Override
    public GTMSRecipeBuilder chancedOutput(TagPrefix prefix, Material material, String fraction) {
        return (GTMSRecipeBuilder) super.chancedOutput(prefix, material, fraction);
    }

    @Override
    public GTMSRecipeBuilder chancedOutput(Item item, int count, String fraction) {
        return (GTMSRecipeBuilder) super.chancedOutput(item, count, fraction);
    }

    @Override
    public GTMSRecipeBuilder chancedOutput(Item item, String fraction) {
        return (GTMSRecipeBuilder) super.chancedOutput(item, fraction);
    }

    @Override
    public GTMSRecipeBuilder chancedFluidOutput(FluidStack stack, String fraction) {
        return (GTMSRecipeBuilder) super.chancedFluidOutput(stack, fraction);
    }

    @Override
    public GTMSRecipeBuilder chancedOutputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
        return (GTMSRecipeBuilder) super.chancedOutputLogic(cap, logic);
    }

    @Override
    public GTMSRecipeBuilder chancedItemOutputLogic(ChanceLogic logic) {
        return (GTMSRecipeBuilder) super.chancedItemOutputLogic(logic);
    }

    @Override
    public GTMSRecipeBuilder chancedFluidOutputLogic(ChanceLogic logic) {
        return (GTMSRecipeBuilder) super.chancedFluidOutputLogic(logic);
    }

    @Override
    public GTMSRecipeBuilder chancedInputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
        return (GTMSRecipeBuilder) super.chancedInputLogic(cap, logic);
    }

    @Override
    public GTMSRecipeBuilder chancedItemInputLogic(ChanceLogic logic) {
        return (GTMSRecipeBuilder) super.chancedItemInputLogic(logic);
    }

    @Override
    public GTMSRecipeBuilder chancedFluidInputLogic(ChanceLogic logic) {
        return (GTMSRecipeBuilder) super.chancedFluidInputLogic(logic);
    }

    @Override
    public GTMSRecipeBuilder chancedTickOutputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
        return (GTMSRecipeBuilder) super.chancedTickOutputLogic(cap, logic);
    }

    @Override
    public GTMSRecipeBuilder chancedTickInputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
        return (GTMSRecipeBuilder) super.chancedTickInputLogic(cap, logic);
    }

    @Override
    public GTMSRecipeBuilder inputFluids(@NotNull Material material, int amount) {
        return (GTMSRecipeBuilder) super.inputFluids(material, amount);
    }

    @Override
    public GTMSRecipeBuilder inputFluids(FluidStack input) {
        return (GTMSRecipeBuilder) super.inputFluids(input);
    }

    @Override
    public GTMSRecipeBuilder inputFluids(FluidStack... inputs) {
        return (GTMSRecipeBuilder) super.inputFluids(inputs);
    }

    @Override
    public GTMSRecipeBuilder inputFluidsRanged(IntProviderFluidIngredient provider) {
        return (GTMSRecipeBuilder) super.inputFluidsRanged(provider);
    }

    @Override
    protected GTMSRecipeBuilder inputFluidsRanged(FluidIngredient input, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.inputFluidsRanged(input, intProvider);
    }

    @Override
    public GTMSRecipeBuilder inputFluidsRanged(FluidStack input, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.inputFluidsRanged(input, intProvider);
    }

    @Override
    public GTMSRecipeBuilder inputFluids(FluidIngredient... inputs) {
        return (GTMSRecipeBuilder) super.inputFluids(inputs);
    }

    @Override
    public GTMSRecipeBuilder outputFluids(FluidStack output) {
        return (GTMSRecipeBuilder) super.outputFluids(output);
    }

    @Override
    public GTMSRecipeBuilder outputFluids(FluidStack... outputs) {
        return (GTMSRecipeBuilder) super.outputFluids(outputs);
    }

    @Override
    public GTMSRecipeBuilder outputFluids(FluidIngredient... outputs) {
        return (GTMSRecipeBuilder) super.outputFluids(outputs);
    }

    @Override
    public GTMSRecipeBuilder outputFluidsRanged(IntProviderFluidIngredient provider) {
        return (GTMSRecipeBuilder) super.outputFluidsRanged(provider);
    }

    @Override
    protected GTMSRecipeBuilder outputFluidsRanged(FluidIngredient output, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.outputFluidsRanged(output, intProvider);
    }

    @Override
    public GTMSRecipeBuilder outputFluidsRanged(FluidStack output, IntProvider intProvider) {
        return (GTMSRecipeBuilder) super.outputFluidsRanged(output, intProvider);
    }

    @Override
    public GTMSRecipeBuilder addData(String key, Tag data) {
        return (GTMSRecipeBuilder) super.addData(key, data);
    }

    @Override
    public GTMSRecipeBuilder addData(String key, int data) {
        return (GTMSRecipeBuilder) super.addData(key, data);
    }

    @Override
    public GTMSRecipeBuilder addData(String key, long data) {
        return (GTMSRecipeBuilder) super.addData(key, data);
    }

    @Override
    public GTMSRecipeBuilder addData(String key, String data) {
        return (GTMSRecipeBuilder) super.addData(key, data);
    }

    @Override
    public GTMSRecipeBuilder addData(String key, float data) {
        return (GTMSRecipeBuilder) super.addData(key, data);
    }

    @Override
    public GTMSRecipeBuilder addData(String key, boolean data) {
        return (GTMSRecipeBuilder) super.addData(key, data);
    }

    @Override
    public GTMSRecipeBuilder blastFurnaceTemp(int blastTemp) {
        return (GTMSRecipeBuilder) super.blastFurnaceTemp(blastTemp);
    }

    @Override
    public GTMSRecipeBuilder explosivesAmount(int explosivesAmount) {
        return (GTMSRecipeBuilder) super.explosivesAmount(explosivesAmount);
    }

    @Override
    public GTMSRecipeBuilder explosivesType(ItemStack explosivesType) {
        return (GTMSRecipeBuilder) super.explosivesType(explosivesType);
    }

    @Override
    public GTMSRecipeBuilder solderMultiplier(int multiplier) {
        return (GTMSRecipeBuilder) super.solderMultiplier(multiplier);
    }

    @Override
    public GTMSRecipeBuilder disableDistilleryRecipes(boolean flag) {
        return (GTMSRecipeBuilder) super.disableDistilleryRecipes(flag);
    }

    @Override
    public GTMSRecipeBuilder fusionStartEU(long eu) {
        return (GTMSRecipeBuilder) super.fusionStartEU(eu);
    }

    @Override
    public GTMSRecipeBuilder researchScan(boolean isScan) {
        return (GTMSRecipeBuilder) super.researchScan(isScan);
    }

    @Override
    public GTMSRecipeBuilder durationIsTotalCWU(boolean durationIsTotalCWU) {
        return (GTMSRecipeBuilder) super.durationIsTotalCWU(durationIsTotalCWU);
    }

    @Override
    public GTMSRecipeBuilder hideDuration(boolean hideDuration) {
        return (GTMSRecipeBuilder) super.hideDuration(hideDuration);
    }

    @Override
    public GTMSRecipeBuilder cleanroom(CleanroomType cleanroomType) {
        return (GTMSRecipeBuilder) super.cleanroom(cleanroomType);
    }

    @Override
    public GTMSRecipeBuilder dimension(ResourceLocation dimension, boolean reverse) {
        return (GTMSRecipeBuilder) super.dimension(dimension, reverse);
    }

    @Override
    public GTMSRecipeBuilder dimension(ResourceLocation dimension) {
        return (GTMSRecipeBuilder) super.dimension(dimension);
    }

    @Override
    public GTMSRecipeBuilder dimension(ResourceKey<Level> dimension, boolean reverse) {
        return (GTMSRecipeBuilder) super.dimension(dimension, reverse);
    }

    @Override
    public GTMSRecipeBuilder dimension(ResourceKey<Level> dimension) {
        return (GTMSRecipeBuilder) super.dimension(dimension);
    }

    @Override
    public GTMSRecipeBuilder biome(ResourceLocation biome, boolean reverse) {
        return (GTMSRecipeBuilder) super.biome(biome, reverse);
    }

    @Override
    public GTMSRecipeBuilder biome(ResourceLocation biome) {
        return (GTMSRecipeBuilder) super.biome(biome);
    }

    @Override
    public GTMSRecipeBuilder biome(ResourceKey<Biome> biome, boolean reverse) {
        return (GTMSRecipeBuilder) super.biome(biome, reverse);
    }

    @Override
    public GTMSRecipeBuilder biome(ResourceKey<Biome> biome) {
        return (GTMSRecipeBuilder) super.biome(biome);
    }

    @Override
    public GTMSRecipeBuilder biomeTag(TagKey<Biome> biome, boolean reverse) {
        return (GTMSRecipeBuilder) super.biomeTag(biome, reverse);
    }

    @Override
    public GTMSRecipeBuilder biomeTag(TagKey<Biome> biome) {
        return (GTMSRecipeBuilder) super.biomeTag(biome);
    }

    @Override
    public GTMSRecipeBuilder rain(float level, boolean reverse) {
        return (GTMSRecipeBuilder) super.rain(level, reverse);
    }

    @Override
    public GTMSRecipeBuilder rain(float level) {
        return (GTMSRecipeBuilder) super.rain(level);
    }

    @Override
    public GTMSRecipeBuilder thunder(float level, boolean reverse) {
        return (GTMSRecipeBuilder) super.thunder(level, reverse);
    }

    @Override
    public GTMSRecipeBuilder thunder(float level) {
        return (GTMSRecipeBuilder) super.thunder(level);
    }

    @Override
    public GTMSRecipeBuilder posY(int min, int max, boolean reverse) {
        return (GTMSRecipeBuilder) super.posY(min, max, reverse);
    }

    @Override
    public GTMSRecipeBuilder posY(int min, int max) {
        return (GTMSRecipeBuilder) super.posY(min, max);
    }

    @Override
    public GTMSRecipeBuilder environmentalHazard(MedicalCondition condition, boolean reverse) {
        return (GTMSRecipeBuilder) super.environmentalHazard(condition, reverse);
    }

    @Override
    public GTMSRecipeBuilder environmentalHazard(MedicalCondition condition) {
        return (GTMSRecipeBuilder) super.environmentalHazard(condition);
    }

    @Override
    public GTMSRecipeBuilder adjacentFluids(Collection<HolderSet<Fluid>> fluids) {
        return (GTMSRecipeBuilder) super.adjacentFluids(fluids);
    }

    @Override
    public GTMSRecipeBuilder adjacentFluids(Collection<HolderSet<Fluid>> fluids, boolean isReverse) {
        return (GTMSRecipeBuilder) super.adjacentFluids(fluids, isReverse);
    }

    @Override
    public GTMSRecipeBuilder adjacentBlocks(Block... blocks) {
        return (GTMSRecipeBuilder) super.adjacentBlocks(blocks);
    }

    @Override
    public GTMSRecipeBuilder adjacentBlocks(boolean isReverse, Block... blocks) {
        return (GTMSRecipeBuilder) super.adjacentBlocks(isReverse, blocks);
    }

    @Override
    public GTMSRecipeBuilder adjacentBlocks(Collection<HolderSet<Block>> blocks) {
        return (GTMSRecipeBuilder) super.adjacentBlocks(blocks);
    }

    @Override
    public GTMSRecipeBuilder adjacentBlocks(Collection<HolderSet<Block>> blocks, boolean isReverse) {
        return (GTMSRecipeBuilder) super.adjacentBlocks(blocks, isReverse);
    }

    @Override
    public GTMSRecipeBuilder daytime(boolean isNight) {
        return (GTMSRecipeBuilder) super.daytime(isNight);
    }

    @Override
    public GTMSRecipeBuilder daytime() {
        return (GTMSRecipeBuilder) super.daytime();
    }

    @Override
    public GTMSRecipeBuilder heraclesQuest(String questId, boolean isReverse) {
        return (GTMSRecipeBuilder) super.heraclesQuest(questId, isReverse);
    }

    @Override
    public GTMSRecipeBuilder heraclesQuest(String questId) {
        return (GTMSRecipeBuilder) super.heraclesQuest(questId);
    }

    @Override
    public GTMSRecipeBuilder gameStage(String stageName) {
        return (GTMSRecipeBuilder) super.gameStage(stageName);
    }

    @Override
    public GTMSRecipeBuilder gameStage(String stageName, boolean isReverse) {
        return (GTMSRecipeBuilder) super.gameStage(stageName, isReverse);
    }

    @Override
    public GTMSRecipeBuilder ftbQuest(String questId, boolean isReverse) {
        return (GTMSRecipeBuilder) super.ftbQuest(questId, isReverse);
    }

    @Override
    public GTMSRecipeBuilder ftbQuest(String questId) {
        return (GTMSRecipeBuilder) super.ftbQuest(questId);
    }

    @Override
    public GTMSRecipeBuilder researchWithoutRecipe(@NotNull String researchId) {
        return (GTMSRecipeBuilder) super.researchWithoutRecipe(researchId);
    }

    @Override
    public GTMSRecipeBuilder researchWithoutRecipe(@NotNull String researchId, @NotNull ItemStack dataStack) {
        return (GTMSRecipeBuilder) super.researchWithoutRecipe(researchId, dataStack);
    }

    @Override
    public GTMSRecipeBuilder scannerResearch(UnaryOperator<ResearchRecipeBuilder.ScannerRecipeBuilder> research) {
        return (GTMSRecipeBuilder) super.scannerResearch(research);
    }

    @Override
    public GTMSRecipeBuilder scannerResearch(@NotNull ItemStack researchStack) {
        return (GTMSRecipeBuilder) super.scannerResearch(researchStack);
    }

    @Override
    public GTMSRecipeBuilder stationResearch(UnaryOperator<ResearchRecipeBuilder.StationRecipeBuilder> research) {
        return (GTMSRecipeBuilder) super.stationResearch(research);
    }

    @Override
    public GTMSRecipeBuilder category(@NotNull GTRecipeCategory category) {
        return (GTMSRecipeBuilder) super.category(category);
    }

    @Override
    public GTMSRecipeBuilder addMaterialInfo(boolean item) {
        return (GTMSRecipeBuilder) super.addMaterialInfo(item);
    }

    @Override
    public GTMSRecipeBuilder addMaterialInfo(boolean item, boolean fluid) {
        return (GTMSRecipeBuilder) super.addMaterialInfo(item, fluid);
    }

    @Override
    public GTMSRecipeBuilder removePreviousMaterialInfo() {
        return (GTMSRecipeBuilder) super.removePreviousMaterialInfo();
    }

    @Override
    public GTMSRecipeBuilder setTempItemMaterialStacks(List<MaterialStack> stacks) {
        return (GTMSRecipeBuilder) super.setTempItemMaterialStacks(stacks);
    }

    @Override
    public GTMSRecipeBuilder setTempFluidMaterialStacks(List<MaterialStack> stacks) {
        return (GTMSRecipeBuilder) super.setTempFluidMaterialStacks(stacks);
    }

    @Override
    public GTMSRecipeBuilder setTempItemStacks(List<ItemStack> stacks) {
        return (GTMSRecipeBuilder) super.setTempItemStacks(stacks);
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
    public @NotNull GTMSRecipeBuilder chance(int chance) {
        return (GTMSRecipeBuilder) super.chance(chance);
    }

    @Override
    public @NotNull GTMSRecipeBuilder maxChance(int maxChance) {
        return (GTMSRecipeBuilder) super.maxChance(maxChance);
    }

    @Override
    public @NotNull GTMSRecipeBuilder onSave(@Nullable BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onSave) {
        return (GTMSRecipeBuilder) super.onSave(onSave);
    }

    @Override
    public Collection<ResearchRecipeEntry> researchRecipeEntries() {
        return super.researchRecipeEntries();
    }
}

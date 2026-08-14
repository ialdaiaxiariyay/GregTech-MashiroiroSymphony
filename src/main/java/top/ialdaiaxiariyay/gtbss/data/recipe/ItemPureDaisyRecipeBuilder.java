package top.ialdaiaxiariyay.gtbss.data.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtbss.common.data.VanillaRecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class ItemPureDaisyRecipeBuilder {

    private ResourceLocation id;
    private final List<Ingredient> inputs = new ArrayList<>();
    private final List<Integer> inputCounts = new ArrayList<>();
    private @Nullable BlockState outputState;
    private @Nullable ItemStack outputItem;
    private int time;

    private ItemPureDaisyRecipeBuilder() {}

    @Contract(" -> new")
    public static @NotNull ItemPureDaisyRecipeBuilder builder() {
        return new ItemPureDaisyRecipeBuilder();
    }

    public ItemPureDaisyRecipeBuilder id(ResourceLocation id) {
        this.id = id;
        return this;
    }

    public ItemPureDaisyRecipeBuilder input(Ingredient ingredient) {
        return input(ingredient, 1);
    }

    public ItemPureDaisyRecipeBuilder input(Ingredient ingredient, int count) {
        this.inputs.add(ingredient);
        this.inputCounts.add(Math.max(1, count));
        return this;
    }

    public ItemPureDaisyRecipeBuilder input(TagKey<Item> tag) {
        return input(Ingredient.of(tag), 1);
    }

    public ItemPureDaisyRecipeBuilder input(TagKey<Item> tag, int count) {
        return input(Ingredient.of(tag), count);
    }

    public ItemPureDaisyRecipeBuilder input(ItemLike item) {
        return input(Ingredient.of(item), 1);
    }

    public ItemPureDaisyRecipeBuilder input(ItemLike item, int count) {
        return input(Ingredient.of(item), count);
    }

    public ItemPureDaisyRecipeBuilder input(@NotNull ItemStack stack) {
        if (stack.hasTag()) {
            return input(StrictNBTIngredient.of(stack), stack.getCount());
        }
        return input(Ingredient.of(stack), stack.getCount());
    }

    public ItemPureDaisyRecipeBuilder output(BlockState state) {
        this.outputState = state;
        this.outputItem = null;
        return this;
    }

    public ItemPureDaisyRecipeBuilder output(@NotNull Block block) {
        return output(block.defaultBlockState());
    }

    public ItemPureDaisyRecipeBuilder output(@NotNull ItemStack stack) {
        this.outputItem = stack.copy();
        this.outputState = null;
        return this;
    }

    public ItemPureDaisyRecipeBuilder output(ItemLike item) {
        return output(new ItemStack(item));
    }

    public ItemPureDaisyRecipeBuilder output(ItemLike item, int count) {
        return output(new ItemStack(item, count));
    }

    public ItemPureDaisyRecipeBuilder time(int second) {
        this.time = second;
        return this;
    }

    private @NotNull ResourceLocation defaultId() {
        if (outputState != null && !outputState.isAir()) {
            return BuiltInRegistries.BLOCK.getKey(outputState.getBlock());
        } else if (outputItem != null && !outputItem.isEmpty()) {
            return BuiltInRegistries.ITEM.getKey(outputItem.getItem());
        }
        throw new IllegalStateException("No valid output to generate default ID");
    }

    private void toJson(JsonObject json) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException(id + ": inputs cannot be empty");
        }

        JsonArray inputArray = new JsonArray();
        for (int i = 0; i < inputs.size(); i++) {
            JsonObject inputObj = new JsonObject();
            inputObj.add("ingredient", inputs.get(i).toJson());
            inputObj.addProperty("count", inputCounts.get(i));
            inputArray.add(inputObj);
        }
        json.add("inputs", inputArray);

        JsonObject outputJson = new JsonObject();
        if (outputState != null && !outputState.isAir()) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(outputState.getBlock());
            outputJson.addProperty("Name", blockId.toString());
            if (!outputState.getProperties().isEmpty()) {
                JsonObject propertiesJson = new JsonObject();
                for (Property<?> prop : outputState.getProperties()) {
                    Comparable<?> value = outputState.getValue(prop);
                    propertiesJson.addProperty(prop.getName(), value.toString());
                }
                outputJson.add("Properties", propertiesJson);
            }
        } else if (outputItem != null && !outputItem.isEmpty()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(outputItem.getItem());
            outputJson.addProperty("item", itemId.toString());
            if (outputItem.getCount() > 1) {
                outputJson.addProperty("count", outputItem.getCount());
            }
        } else {
            throw new IllegalArgumentException(id + ": output cannot be empty");
        }
        json.add("output", outputJson);

        json.addProperty("time", time);
    }

    public void save(@NotNull Consumer<FinishedRecipe> consumer) {
        ResourceLocation recipeId = (id == null ? defaultId() : id)
                .withPrefix("item_pure_daisy/");

        RecipeSerializer<?> serializer = VanillaRecipeType.ITEM_PURE_DAISY_SERIALIZER.get();

        consumer.accept(new FinishedRecipe() {

            @Override
            public void serializeRecipeData(@NotNull JsonObject json) {
                toJson(json);
            }

            @Override
            public @NotNull ResourceLocation getId() {
                return recipeId;
            }

            @Override
            public @NotNull RecipeSerializer<?> getType() {
                return serializer;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }
}

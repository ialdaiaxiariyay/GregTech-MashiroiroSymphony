package top.ialdaiaxiariyay.gtbss.api.recipe.type;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.google.gson.JsonObject;
import lombok.Getter;
import top.ialdaiaxiariyay.gtbss.common.data.VanillaRecipeType;
import vazkii.botania.common.crafting.StateIngredientHelper;

import java.util.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemPureDaisyRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    @Getter
    private final List<Ingredient> inputs;
    @Getter
    private final List<Integer> inputCounts;
    @Getter
    @Nullable
    private final BlockState outputState;
    @Getter
    @Nullable
    private final ItemStack outputItem;
    @Getter
    private final int outputCount;
    @Getter
    private final int time;
    @Getter
    @Nullable
    private final String group;

    public ItemPureDaisyRecipe(ResourceLocation id, List<Ingredient> inputs, List<Integer> inputCounts,
                               BlockState outputState, int time, @Nullable String group) {
        this(id, inputs, inputCounts, outputState, null, 1, time, group);
    }

    public ItemPureDaisyRecipe(ResourceLocation id, List<Ingredient> inputs, List<Integer> inputCounts,
                               ItemStack outputItem, int outputCount, int time, @Nullable String group) {
        this(id, inputs, inputCounts, null, outputItem, outputCount, time, group);
    }

    private ItemPureDaisyRecipe(ResourceLocation id, List<Ingredient> inputs, List<Integer> inputCounts,
                                @Nullable BlockState outputState,
                                @Nullable ItemStack outputItem,
                                int outputCount,
                                int time,
                                @Nullable String group) {
        this.id = id;

        if (inputs.isEmpty() || inputs.size() != inputCounts.size()) {
            throw new IllegalArgumentException("Inputs and inputCounts must be non-empty and same size");
        }

        this.inputs = List.copyOf(inputs);
        this.inputCounts = List.copyOf(inputCounts);
        this.outputState = outputState;
        this.outputItem = outputItem != null ? outputItem.copy() : null;
        this.outputCount = Math.max(1, outputCount);
        this.time = time;
        this.group = group;
    }

    public boolean test(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        for (int i = 0; i < inputs.size(); i++) {
            if (inputs.get(i).test(stack) && stack.getCount() >= inputCounts.get(i)) {
                return true;
            }
        }
        return false;
    }

    public void set(Level level, BlockPos pos, ItemEntity entity) {
        if (level.isClientSide) return;

        ItemStack inputStack = entity.getItem();
        int actualCount = inputStack.getCount();

        int matchedIndex = -1;
        for (int i = 0; i < inputs.size(); i++) {
            if (inputs.get(i).test(inputStack)) {
                matchedIndex = i;
                break;
            }
        }
        if (matchedIndex == -1) return;

        int requiredCount = inputCounts.get(matchedIndex);
        int times = actualCount / requiredCount;
        if (times <= 0) return;

        if (outputState != null && !outputState.isAir()) {
            List<BlockPos> available = new ArrayList<>();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos checkPos = pos.offset(dx, 0, dz);
                    if (available.contains(checkPos)) continue;
                    BlockState state = level.getBlockState(checkPos);
                    if (state.isAir() || state.canBeReplaced()) {
                        available.add(checkPos);
                    }
                }
            }

            int placed = Math.min(times, available.size());
            if (placed == 0) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                ItemStack refund = inputStack.copy();
                ItemEntity refundEntity = new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, refund);
                level.addFreshEntity(refundEntity);
                return;
            }

            int consumed = placed * requiredCount;
            int remainder = actualCount - consumed;

            entity.remove(Entity.RemovalReason.DISCARDED);

            if (remainder > 0) {
                ItemStack remainderStack = inputStack.copy();
                remainderStack.setCount(remainder);
                ItemEntity remainderEntity = new ItemEntity(level,
                        pos.getX() + 0.5 + (Math.random() - 0.5) * 0.3,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5 + (Math.random() - 0.5) * 0.3,
                        remainderStack);
                level.addFreshEntity(remainderEntity);
            }

            for (int i = 0; i < placed; i++) {
                level.setBlockAndUpdate(available.get(i), outputState);
            }
            return;
        }

        if (outputItem != null && !outputItem.isEmpty()) {
            int consume = times * requiredCount;
            int produce = times * outputCount;
            int remainder = actualCount - consume;

            entity.remove(Entity.RemovalReason.DISCARDED);

            if (remainder > 0) {
                ItemStack remainderStack = inputStack.copy();
                remainderStack.setCount(remainder);
                ItemEntity remainderEntity = new ItemEntity(level,
                        pos.getX() + 0.5 + (Math.random() - 0.5) * 0.3,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5 + (Math.random() - 0.5) * 0.3,
                        remainderStack);
                level.addFreshEntity(remainderEntity);
            }

            ItemStack result = outputItem.copy();
            int totalOutput = result.getCount() * produce;
            int maxStackSize = result.getMaxStackSize();
            while (totalOutput > 0) {
                int batchSize = Math.min(totalOutput, maxStackSize);
                ItemStack batch = result.copy();
                batch.setCount(batchSize);
                ItemEntity resultEntity = new ItemEntity(level,
                        pos.getX() + 0.5 + (Math.random() - 0.5) * 0.3,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5 + (Math.random() - 0.5) * 0.3,
                        batch);
                resultEntity.setDeltaMovement(
                        (Math.random() - 0.5) * 0.1,
                        0.1 + Math.random() * 0.1,
                        (Math.random() - 0.5) * 0.1);
                level.addFreshEntity(resultEntity);
                totalOutput -= batchSize;
            }
        } else {
            throw new IllegalStateException("Recipe " + id + " has no valid output!");
        }
    }

    public boolean outputsBlock() {
        return outputState != null && !outputState.isAir();
    }

    public boolean outputsItem() {
        return outputItem != null && !outputItem.isEmpty();
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return VanillaRecipeType.ITEM_PURE_DAISY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return VanillaRecipeType.ITEM_PURE_DAISY_TYPE.get();
    }

    @SuppressWarnings("deprecation")
    public static class Serializer implements RecipeSerializer<ItemPureDaisyRecipe> {

        @Override
        public ItemPureDaisyRecipe fromJson(ResourceLocation id, JsonObject json) {
            List<Ingredient> inputs = new ArrayList<>();
            List<Integer> inputCounts = new ArrayList<>();

            if (json.has("inputs") && json.get("inputs").isJsonArray()) {
                var inputArray = json.getAsJsonArray("inputs");
                for (var elem : inputArray) {
                    var obj = elem.getAsJsonObject();
                    inputs.add(Ingredient.fromJson(obj.get("ingredient")));
                    inputCounts.add(obj.has("count") ? obj.get("count").getAsInt() : 1);
                }
            } else if (json.has("input")) {
                inputs.add(Ingredient.fromJson(json.get("input")));
                inputCounts.add(json.has("inputCount") ? json.get("inputCount").getAsInt() : 1);
            } else {
                throw new IllegalArgumentException("Recipe must have 'input' or 'inputs' field");
            }

            int time = json.has("time") ? json.get("time").getAsInt() : 150;
            int outputCount = json.has("outputCount") ? json.get("outputCount").getAsInt() : 1;
            String group = json.has("group") ? json.get("group").getAsString() : null;
            JsonObject outputJson = json.get("output").getAsJsonObject();

            if (outputJson.has("item")) {
                ItemStack stack = new ItemStack(
                        BuiltInRegistries.ITEM
                                .get(ResourceLocation.tryParse(outputJson.get("item").getAsString())),
                        outputJson.has("count") ? outputJson.get("count").getAsInt() : 1);
                return new ItemPureDaisyRecipe(id, inputs, inputCounts, stack, outputCount, time, group);
            } else {
                BlockState state = StateIngredientHelper.readBlockState(outputJson);
                return new ItemPureDaisyRecipe(id, inputs, inputCounts, state, time, group);
            }
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ItemPureDaisyRecipe recipe) {
            buf.writeVarInt(recipe.inputs.size());
            for (int i = 0; i < recipe.inputs.size(); i++) {
                recipe.inputs.get(i).toNetwork(buf);
                buf.writeVarInt(recipe.inputCounts.get(i));
            }
            buf.writeBoolean(recipe.outputsBlock());
            if (recipe.outputsBlock()) {
                buf.writeVarInt(Block.getId(recipe.outputState));
            } else if (recipe.outputsItem()) {
                buf.writeItem(recipe.outputItem != null ? recipe.outputItem : ItemStack.EMPTY);
            }
            buf.writeVarInt(recipe.outputCount);
            buf.writeVarInt(recipe.time);
            buf.writeUtf(recipe.group != null ? recipe.group : "");
        }

        @Override
        public ItemPureDaisyRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<Ingredient> inputs = new ArrayList<>(size);
            List<Integer> inputCounts = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                inputs.add(Ingredient.fromNetwork(buf));
                inputCounts.add(buf.readVarInt());
            }
            boolean isBlock = buf.readBoolean();
            int outputCount = buf.readVarInt();
            int time = buf.readVarInt();
            String group = buf.readUtf();
            if (group.isEmpty()) group = null;

            if (isBlock) {
                BlockState state = Block.stateById(buf.readVarInt());
                return new ItemPureDaisyRecipe(id, inputs, inputCounts, state, time, group);
            } else {
                ItemStack stack = buf.readItem();
                return new ItemPureDaisyRecipe(id, inputs, inputCounts, stack, outputCount, time, group);
            }
        }
    }
}

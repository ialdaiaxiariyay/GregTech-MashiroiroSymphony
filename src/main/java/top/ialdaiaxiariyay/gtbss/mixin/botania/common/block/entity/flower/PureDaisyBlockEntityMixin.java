package top.ialdaiaxiariyay.gtbss.mixin.botania.common.block.entity.flower;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ialdaiaxiariyay.gtbss.api.recipe.type.ItemPureDaisyRecipe;
import top.ialdaiaxiariyay.gtbss.common.data.VanillaRecipeType;
import vazkii.botania.common.block.flower.PureDaisyBlockEntity;

import java.util.*;

@Mixin(PureDaisyBlockEntity.class)
public abstract class PureDaisyBlockEntityMixin {

    @Unique
    private final Map<BlockPos, Integer> gtbss$processingItems = new HashMap<>();
    @Unique
    private int gtbss$scanCooldown = 0;

    @Inject(method = "tickFlower", at = @At("HEAD"), remap = false)
    private void tickFlower(CallbackInfo ci) {
        PureDaisyBlockEntity self = (PureDaisyBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) return;

        if (level.isClientSide) {
            BlockPos center = self.getEffectivePos();
            for (Map.Entry<BlockPos, Integer> entry : gtbss$processingItems.entrySet()) {
                if (entry.getValue() > 0) {
                    BlockPos relPos = entry.getKey();
                    BlockPos worldPos = center.offset(relPos.getX(), relPos.getY(), relPos.getZ());
                    double x = worldPos.getX() + 0.5 + (Math.random() - 0.5) * 0.8;
                    double y = worldPos.getY() + 0.5 + (Math.random() - 0.5) * 0.8;
                    double z = worldPos.getZ() + 0.5 + (Math.random() - 0.5) * 0.8;
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                            x, y, z, 0, 0, 0);
                }
            }
            return;
        }

        if (--gtbss$scanCooldown > 0) return;
        gtbss$scanCooldown = 5;

        BlockPos center = self.getEffectivePos();

        Iterator<Map.Entry<BlockPos, Integer>> iter = gtbss$processingItems.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iter.next();
            BlockPos relPos = entry.getKey();
            BlockPos worldPos = center.offset(relPos.getX(), relPos.getY(), relPos.getZ());
            int remain = entry.getValue() - 1;

            ItemEntity entity = gtbss$findItemEntityAt(level, worldPos);
            if (entity == null) {
                iter.remove();
                self.setChanged();
                self.sync();
                level.sendBlockUpdated(self.getBlockPos(), self.getBlockState(), self.getBlockState(), 3);
                continue;
            }

            if (remain <= 0) {
                ItemPureDaisyRecipe recipe = gtbss$findRecipe(entity);
                if (recipe != null && recipe.test(entity)) {
                    recipe.set(level, worldPos, entity);
                    level.blockEvent(self.getBlockPos(), self.getBlockState().getBlock(), 0, 0);
                }
                iter.remove();
            } else {
                entry.setValue(remain);
            }
            self.setChanged();
            self.sync();
            level.sendBlockUpdated(self.getBlockPos(), self.getBlockState(), self.getBlockState(), 3);
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos relPos = new BlockPos(dx, 0, dz);
                if (gtbss$processingItems.containsKey(relPos)) continue;

                BlockPos worldPos = center.offset(dx, 0, dz);
                ItemEntity entity = gtbss$findItemEntityAt(level, worldPos);
                if (entity != null) {
                    ItemPureDaisyRecipe recipe = gtbss$findRecipe(entity);
                    if (recipe != null && recipe.test(entity)) {
                        gtbss$processingItems.put(relPos, recipe.getTime());
                        self.setChanged();
                        self.sync();
                        level.sendBlockUpdated(self.getBlockPos(), self.getBlockState(), self.getBlockState(), 3);
                    }
                }
            }
        }
    }

    @Unique
    private @Nullable ItemEntity gtbss$findItemEntityAt(@NotNull Level level, BlockPos pos) {
        AABB aabb = new AABB(pos).inflate(0.1);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, aabb);
        return items.isEmpty() ? null : items.get(0);
    }

    @Unique
    private @Nullable ItemPureDaisyRecipe gtbss$findRecipe(@NotNull ItemEntity entity) {
        var recipes = entity.level().getRecipeManager()
                .getAllRecipesFor(VanillaRecipeType.ITEM_PURE_DAISY_TYPE.get());
        for (Recipe<?> recipe : recipes) {
            if (recipe instanceof ItemPureDaisyRecipe ipr && ipr.test(entity)) {
                return ipr;
            }
        }
        return null;
    }

    @Unique
    private void gtbss$writeItemData(CompoundTag cmp) {
        CompoundTag data = new CompoundTag();
        for (Map.Entry<BlockPos, Integer> entry : gtbss$processingItems.entrySet()) {
            data.putInt(Long.toString(entry.getKey().asLong()), entry.getValue());
        }
        cmp.put("BotaniaItemProcessing", data);
    }

    @Unique
    private void gtbss$readItemData(@NotNull CompoundTag cmp) {
        gtbss$processingItems.clear();
        if (cmp.contains("BotaniaItemProcessing", Tag.TAG_COMPOUND)) {
            CompoundTag data = cmp.getCompound("BotaniaItemProcessing");
            for (String key : data.getAllKeys()) {
                BlockPos pos = BlockPos.of(Long.parseLong(key));
                int time = data.getInt(key);
                gtbss$processingItems.put(pos, time);
            }
        }
    }

    @Inject(method = "writeToPacketNBT", at = @At("TAIL"), remap = false)
    private void writeToPacketNBT(CompoundTag cmp, CallbackInfo ci) {
        gtbss$writeItemData(cmp);
    }

    @Inject(method = "readFromPacketNBT", at = @At("TAIL"), remap = false)
    private void readFromPacketNBT(CompoundTag cmp, CallbackInfo ci) {
        gtbss$readItemData(cmp);
    }
}

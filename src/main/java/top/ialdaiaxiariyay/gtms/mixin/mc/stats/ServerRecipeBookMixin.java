package top.ialdaiaxiariyay.gtms.mixin.mc.stats;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collection;
import java.util.List;

@Mixin(ServerRecipeBook.class)
public abstract class ServerRecipeBookMixin extends RecipeBook {

    /**
     * @author GTMS
     * @reason Prevents any recipe from being added to the server-side book – returns 0.
     */
    @Overwrite
    public int addRecipes(Collection<Recipe<?>> recipes, ServerPlayer player) {
        return 0;
    }

    /**
     * @author GTMS
     * @reason Prevents any recipe from being removed – returns 0.
     */
    @Overwrite
    public int removeRecipes(Collection<Recipe<?>> recipes, ServerPlayer player) {
        return 0;
    }

    /**
     * @author GTMS
     * @reason Suppresses sending of recipe update packets to the client – no network traffic.
     */
    @Overwrite
    private void sendRecipes(ClientboundRecipePacket.State state, ServerPlayer player, List<ResourceLocation> recipes) {
    }

    /**
     * @author GTMS
     * @reason Writes an empty NBT compound – no recipe data is ever persisted.
     */
    @Overwrite
    public CompoundTag toNbt() {
        return new CompoundTag();
    }

    /**
     * @author GTMS
     * @reason Ignores any saved recipe data – no recipes are ever loaded.
     */
    @Overwrite
    public void fromNbt(CompoundTag tag, RecipeManager recipeManager) {
    }

    /**
     * @author GTMS
     * @reason Prevents the initial recipe book synchronization packet from being sent.
     */
    @Overwrite
    public void sendInitialRecipeBook(ServerPlayer player) {
    }
}
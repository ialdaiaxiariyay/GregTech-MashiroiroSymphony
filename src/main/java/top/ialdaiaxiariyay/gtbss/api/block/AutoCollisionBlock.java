package top.ialdaiaxiariyay.gtbss.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.client.renderer.block.AutoCollisionShapes;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
public abstract class AutoCollisionBlock extends Block {

    private final ResourceLocation modelLocation;

    public AutoCollisionBlock(Properties properties, ResourceLocation modelLocation) {
        super(properties);
        this.modelLocation = modelLocation;
        AutoCollisionShapes.precompute(modelLocation);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                                        CollisionContext context) {
        return AutoCollisionShapes.getShape(modelLocation);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                                 @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                                   @NotNull BlockPos pos) {
        return getShape(state, level, pos, CollisionContext.empty());
    }
}

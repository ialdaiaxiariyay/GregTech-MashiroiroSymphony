package top.ialdaiaxiariyay.gtms.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DivergentDreamRodBlock extends AutoCollisionBlock {

    public DivergentDreamRodBlock(Properties properties, ResourceLocation modelLocation) {
        super(properties, modelLocation);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return 15;
    }

    @Override
    public void animateTick(BlockState state, Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        Direction direction = Direction.UP;
        double d0 = (double) pos.getX() + 0.55D - (double) (random.nextFloat() * 0.1F);
        double d1 = (double) pos.getY() + 0.55D - (double) (random.nextFloat() * 0.1F);
        double d2 = (double) pos.getZ() + 0.55D - (double) (random.nextFloat() * 0.1F);
        double d3 = 0.4F - (random.nextFloat() + random.nextFloat()) * 0.4F;
        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.END_ROD,
                    d0 + (double) direction.getStepX() * d3,
                    d1 + (double) direction.getStepY() * d3,
                    d2 + (double) direction.getStepZ() * d3,
                    random.nextGaussian() * 0.005D,
                    random.nextGaussian() * 0.005D,
                    random.nextGaussian() * 0.005D);
        }
    }
}

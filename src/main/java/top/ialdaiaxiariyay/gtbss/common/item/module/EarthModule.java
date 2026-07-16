package top.ialdaiaxiariyay.gtbss.common.item.module;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import top.ialdaiaxiariyay.gtbss.api.item.MagicModuleItem;
import top.ialdaiaxiariyay.gtbss.utils.GTBSSDamageUtils;

public class EarthModule extends MagicModuleItem {

    public EarthModule(Properties properties) {
        super(properties);
    }

    @Override
    public void cast(Level level, Player player, ItemStack wand, float chargeTime) {
        if (level.isClientSide) return;
        double radius = 5 + chargeTime * 3;
        float totalDamage = 12 + chargeTime * 10;
        Vec3 center = player.position();

        GTBSSDamageUtils.splitDamage(level, center, radius, totalDamage,
                e -> e instanceof LivingEntity && e != player && !e.isAlliedTo(player) && e.isAlive());

        if (level instanceof ServerLevel server) {
            int numRays = 16;
            double maxRadius = radius * 0.85;
            double minRadius = 0.5;
            int layers = 4;

            boolean mixMaterials = chargeTime > 0.5f;

            for (int i = 0; i < numRays; i++) {
                double angle = 2 * Math.PI * i / numRays + player.getYRot() * Math.PI / 180;
                for (int layer = 0; layer < layers; layer++) {
                    double r = minRadius + (maxRadius - minRadius) * (layer + 1) / layers;
                    double x = center.x + Math.cos(angle) * r;
                    double z = center.z + Math.sin(angle) * r;
                    double y = center.y;

                    int height = 3 + (int) (layer * 1.5);
                    for (int j = 0; j < height; j++) {
                        net.minecraft.world.level.block.state.BlockState state;
                        if (mixMaterials && level.random.nextFloat() < 0.2f) {
                            state = level.random.nextBoolean() ?
                                    Blocks.DEEPSLATE.defaultBlockState() :
                                    Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                        } else {
                            state = Blocks.STONE.defaultBlockState();
                        }

                        double offsetX = (level.random.nextDouble() - 0.5) * 0.4;
                        double offsetZ = (level.random.nextDouble() - 0.5) * 0.4;
                        double offsetY = (level.random.nextDouble() - 0.5) * 0.2;
                        server.sendParticles(
                                new BlockParticleOption(ParticleTypes.BLOCK, state),
                                x + offsetX,
                                y + j * 0.35 + offsetY,
                                z + offsetZ,
                                1, 0, 0, 0, 0.1);
                    }
                }
            }

            for (int i = 0; i < 20; i++) {
                double angle = 2 * Math.PI * i / 20;
                double r = 1.0 + level.random.nextDouble() * 1.5;
                double x = center.x + Math.cos(angle) * r;
                double z = center.z + Math.sin(angle) * r;
                server.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLESTONE.defaultBlockState()),
                        x, center.y + 0.2, z,
                        0, 0.1, 0.1, 0.1, 0);
            }
        }
    }

    @Override
    public float getChargeTimeModifier() {
        return 0.25f;
    }

    @Override
    public int getManaCost() {
        return 2000;
    }
}

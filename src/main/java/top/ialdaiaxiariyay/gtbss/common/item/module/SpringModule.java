package top.ialdaiaxiariyay.gtbss.common.item.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.phys.Vec3;

import top.ialdaiaxiariyay.gtbss.api.item.MagicModuleItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpringModule extends MagicModuleItem {

    private final Random random = new Random();

    public SpringModule(Properties properties) {
        super(properties);
    }

    @Override
    public void cast(Level level, Player player, ItemStack wand, float chargeTime) {
        if (level.isClientSide) return;
        double radius = 4 + chargeTime * 4;
        BlockPos center = player.blockPosition();
        List<BlockPos> plants = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-(int) radius, -2, -(int) radius),
                center.offset((int) radius, 2, (int) radius))) {
            if (level.getBlockState(pos).getBlock() instanceof BonemealableBlock) {
                plants.add(pos.immutable());
            }
        }

        if (!plants.isEmpty()) {
            int count = 3 + random.nextInt(4); // 3~6
            count = Math.min(count, plants.size());
            for (int i = 0; i < count; i++) {
                int idx = random.nextInt(plants.size());
                BlockPos pos = plants.remove(idx);
                if (level.getBlockState(pos).getBlock() instanceof BonemealableBlock bone) {
                    bone.performBonemeal((ServerLevel) level, level.random, pos, level.getBlockState(pos));
                }
            }

            if (level instanceof ServerLevel server) {
                Vec3 centerVec = player.position();
                for (int i = 0; i < 30; i++) {
                    double x = centerVec.x + (random.nextDouble() - 0.5) * radius * 2;
                    double z = centerVec.z + (random.nextDouble() - 0.5) * radius * 2;
                    double y = centerVec.y + 0.5 + random.nextDouble() * 2;
                    server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            x, y, z, 0, 0.2, 0.2, 0.2, 0.1);
                }
            }
        } else {
            float heal = 2 + chargeTime * 2;
            player.heal(heal);
            if (level instanceof ServerLevel server) {
                Vec3 pos = player.position();
                server.sendParticles(ParticleTypes.HEART,
                        pos.x, pos.y + 1, pos.z, 5, 0.5, 0.5, 0.5, 0);
            }
        }
    }

    @Override
    public float getChargeTimeModifier() {
        return -0.15f;
    }

    @Override
    public int getManaCost() {
        return 900;
    }
}

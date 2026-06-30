package top.ialdaiaxiariyay.gtms.common.item.module;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.api.item.MagicModuleItem;

public class WinterModule extends MagicModuleItem {

    public WinterModule(Properties properties) {
        super(properties);
    }

    @Override
    public void cast(@NotNull Level level, Player player, ItemStack wand, float chargeTime) {
        if (level.isClientSide) return;
        double radius = 4 + chargeTime * 3;
        int duration = (int) (3 + chargeTime * 4) * 20;
        float damage = 1 + chargeTime * 2;
        Vec3 center = player.position();

        ServerLevel server = (ServerLevel) level;

        for (int i = 0; i < 80; i++) {
            double angle = 2 * Math.PI * i / 80;
            double r = 2 + i * 0.1;
            double x = center.x + Math.cos(angle) * r;
            double z = center.z + Math.sin(angle) * r;
            double y = center.y + 0.5 + Math.sin(angle * 2) * 0.3;
            server.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 0, 0.1, 0.1, 0.1, 0);
            if (i % 2 == 0) {
                server.sendParticles(ParticleTypes.END_ROD, x, y, z, 0, 0.1, 0.1, 0.1, 0);
            }
            if (i % 5 == 0) {
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                        x, y - 0.5, z, 1, 0.2, 0.2, 0.2, 0);
            }
        }

        level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center.x - radius, center.y - radius, center.z - radius,
                        center.x + radius, center.y + radius, center.z + radius))
                .forEach(e -> {
                    if (e != player && e.isAlive()) {
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 2, false, true));
                        e.hurt(level.damageSources().magic(), damage);

                        Vec3 pos = e.position();
                        for (int i = 0; i < 16; i++) {
                            double angle = 2 * Math.PI * i / 16;
                            double r = 0.8;
                            double x = pos.x + Math.cos(angle) * r;
                            double z = pos.z + Math.sin(angle) * r;
                            server.sendParticles(ParticleTypes.END_ROD,
                                    x, pos.y + 0.5 + Math.sin(angle * 2) * 0.3, z,
                                    0, 0.05, 0.05, 0.05, 0);
                        }

                        for (int i = 0; i < 8; i++) {
                            double x = pos.x + (level.random.nextDouble() - 0.5) * 1.2;
                            double z = pos.z + (level.random.nextDouble() - 0.5) * 1.2;
                            server.sendParticles(
                                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
                                    x, pos.y + 0.1 + level.random.nextDouble() * 0.5, z,
                                    1, 0.1, 0.1, 0.1, 0);
                        }

                        for (int i = 0; i < 5; i++) {
                            server.sendParticles(ParticleTypes.SNOWFLAKE,
                                    pos.x + (level.random.nextDouble() - 0.5) * 1.5,
                                    pos.y + 0.5 + (level.random.nextDouble() - 0.5) * 1.5,
                                    pos.z + (level.random.nextDouble() - 0.5) * 1.5,
                                    1, 0.1, 0.1, 0.1, 0.05);
                        }

                        for (int i = 0; i < 12; i++) {
                            double angle = 2 * Math.PI * i / 12;
                            double r = 1.2;
                            double x = pos.x + Math.cos(angle) * r;
                            double z = pos.z + Math.sin(angle) * r;
                            server.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                                    x, pos.y + 0.1, z,
                                    0, 0.05, 0.05, 0.05, 0);
                        }
                    }
                });

        for (int i = 0; i < 30; i++) {
            double x = center.x + (level.random.nextDouble() - 0.5) * radius * 2;
            double z = center.z + (level.random.nextDouble() - 0.5) * radius * 2;
            double y = center.y + level.random.nextDouble() * 2;
            server.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 1, 0, 0, 0, 0.1);
        }
    }

    @Override
    public float getChargeTimeModifier() {
        return 0.10f;
    }
}

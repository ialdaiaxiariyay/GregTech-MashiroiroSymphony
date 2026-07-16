package top.ialdaiaxiariyay.gtbss.common.item.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.api.item.MagicModuleItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FireModule extends MagicModuleItem {

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    public FireModule(Properties properties) {
        super(properties);
    }

    @Override
    public void cast(Level level, Player player, ItemStack wand, float chargeTime) {
        if (level.isClientSide) return;

        Vec3 look = player.getLookAngle();
        float damage = 4 + chargeTime * 8;
        double explosionRadius = 2 + chargeTime * 2;
        int fireDurationSeconds = 5;
        double speedMultiplier = 1.0 + chargeTime * 0.3;
        Vec3 velocity = look.scale(speedMultiplier);

        LargeFireball fireball = new LargeFireball(level, player, velocity.x, velocity.y, velocity.z, 0) {

            @Override
            protected void onHit(@NotNull HitResult result) {
                Vec3 hitPos = this.position();
                ServerLevel server = (ServerLevel) level;

                AABB aabb = new AABB(
                        hitPos.x - explosionRadius, hitPos.y - explosionRadius, hitPos.z - explosionRadius,
                        hitPos.x + explosionRadius, hitPos.y + explosionRadius, hitPos.z + explosionRadius);
                level.getEntitiesOfClass(LivingEntity.class, aabb, e -> e != player && e.isAlive())
                        .forEach(e -> e.hurt(level.damageSources().magic(), damage));

                List<BlockPos> firePositions = new ArrayList<>();
                int fireCount = (int) (10 + explosionRadius * 2);
                for (int i = 0; i < fireCount; i++) {
                    int dx = level.random.nextInt((int) (explosionRadius * 2 + 1)) - (int) explosionRadius;
                    int dz = level.random.nextInt((int) (explosionRadius * 2 + 1)) - (int) explosionRadius;
                    BlockPos pos = new BlockPos(
                            (int) hitPos.x + dx,
                            (int) hitPos.y,
                            (int) hitPos.z + dz);
                    if (level.getBlockState(pos.below()).isSolid() && level.isEmptyBlock(pos)) {
                        level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
                        firePositions.add(pos.immutable());
                    }
                }

                SCHEDULER.schedule(() -> server.getServer().execute(() -> {
                    for (BlockPos pos : firePositions) {
                        if (level.getBlockState(pos).getBlock() instanceof BaseFireBlock) {
                            level.removeBlock(pos, false);
                        }
                    }
                }), fireDurationSeconds, TimeUnit.SECONDS);

                for (int i = 0; i < 40; i++) {
                    server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                            hitPos.x + (level.random.nextDouble() - 0.5) * explosionRadius * 2,
                            hitPos.y + (level.random.nextDouble() - 0.5) * explosionRadius * 2,
                            hitPos.z + (level.random.nextDouble() - 0.5) * explosionRadius * 2,
                            1, 0, 0, 0, 0);
                }
                this.discard();
            }
        };

        fireball.setPos(player.getX() + look.x * 2, player.getY() + 1.5, player.getZ() + look.z * 2);
        level.addFreshEntity(fireball);

        if (level instanceof ServerLevel server) {
            Vec3 pos = fireball.position();
            for (int i = 0; i < 20; i++) {
                server.sendParticles(ParticleTypes.FLAME,
                        pos.x + (level.random.nextDouble() - 0.5) * 0.5,
                        pos.y + (level.random.nextDouble() - 0.5) * 0.5,
                        pos.z + (level.random.nextDouble() - 0.5) * 0.5,
                        1, 0, 0, 0, 0.05);
            }
        }
    }

    @Override
    public float getChargeTimeModifier() {
        return 0.20f;
    }

    @Override
    public int getManaCost() {
        return 1920;
    }
}

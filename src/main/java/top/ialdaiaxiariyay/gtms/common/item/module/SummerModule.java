package top.ialdaiaxiariyay.gtms.common.item.module;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.api.item.MagicModuleItem;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;

public class SummerModule extends MagicModuleItem {

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    public SummerModule(Properties properties) {
        super(properties);
    }

    @Override
    public void cast(@NotNull Level level, Player player, ItemStack wand, float chargeTime) {
        if (level.isClientSide) return;

        float totalDamage = 8f + 12f * chargeTime;
        double radius = 6.0;
        Vec3 center = player.position();

        Predicate<LivingEntity> filter = e -> e.isAlive() && !e.isInvulnerable() && e != player;

        AABB box = new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> filter.test(e) && e.distanceToSqr(center) <= radius * radius);

        if (targets.isEmpty()) return;

        int duration = (int) (5 + 5 * chargeTime);
        for (LivingEntity e : targets) {
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration * 20, 1));
            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration * 20, 0));
        }

        float each = totalDamage / targets.size();
        for (LivingEntity e : targets) {
            e.hurt(level.damageSources().magic(), each);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FIREWORK,
                    center.x, center.y + 0.5, center.z,
                    60, radius * 0.9, radius * 0.9, radius * 0.9, 0.1);
            for (LivingEntity e : targets) {
                Vec3 pos = e.position();
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        pos.x, pos.y + 0.5, pos.z,
                        8, 0.3, 0.3, 0.3, 0.0);
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    @Override
    public float getChargeTimeModifier() {
        return 0.30f;
    }
}

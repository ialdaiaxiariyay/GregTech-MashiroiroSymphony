package top.ialdaiaxiariyay.gtbss.common.item.module;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.api.item.MagicModuleItem;
import top.ialdaiaxiariyay.gtbss.utils.GTBSSDamageUtil;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class WindModule extends MagicModuleItem {

    public WindModule(Properties properties) {
        super(properties);
    }

    @Override
    public void cast(@NotNull Level level, Player player, ItemStack wand, float chargeTime) {
        if (level.isClientSide) return;

        int maxBounces = 3 + (int) (chargeTime * 3);
        float baseDamage = 5f + 7f * chargeTime;
        float decayRate = 0.2f - 0.1f * chargeTime;
        double range = 12.0;
        Vec3 origin = player.position();

        Predicate<LivingEntity> filter = e -> e.isAlive() && !e.isInvulnerable() && e != player;

        LivingEntity first = GTBSSDamageUtil.findNearest(level, origin, range, filter, Collections.emptySet());
        if (first != null) {
            Vec3 knockDir = origin.subtract(first.position()).normalize();
            first.knockback(0.5 + chargeTime, knockDir.x, knockDir.z);
        }

        List<LivingEntity> hitList = GTBSSDamageUtil.bounceDamageWithList(level, origin, first,
                baseDamage, maxBounces, decayRate, range, filter,
                (entity, damage) -> {
                    if (level instanceof ServerLevel serverLevel) {
                        Vec3 end = entity.position();
                        for (int i = 0; i < 10; i++) {
                            double t = i / 10.0;
                            Vec3 pos = origin.lerp(end, t);
                            serverLevel.sendParticles(ParticleTypes.CLOUD,
                                    pos.x, pos.y + 0.5, pos.z,
                                    1, 0, 0, 0, 0);
                        }
                        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                                entity.getX(), entity.getY() + 0.5, entity.getZ(),
                                1, 0, 0, 0, 0);
                    }
                    level.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                            SoundSource.PLAYERS, 0.8f, 1.0f);
                });

        if (hitList.isEmpty()) {
            level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
                    SoundSource.PLAYERS, 0.5f, 0.8f);
        }
    }

    @Override
    public float getChargeTimeModifier() {
        return -0.25f;
    }

    @Override
    public int getManaCost() {
        return 1810;
    }
}

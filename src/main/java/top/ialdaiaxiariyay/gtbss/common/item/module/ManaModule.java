package top.ialdaiaxiariyay.gtbss.common.item.module;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.api.item.MagicModuleItem;
import top.ialdaiaxiariyay.gtbss.utils.GTBSSDamageUtils;

import java.util.function.Predicate;

public class ManaModule extends MagicModuleItem {

    public ManaModule(Properties properties) {
        super(properties);
    }

    @Override
    public void cast(@NotNull Level level, Player player, ItemStack wand, float chargeTime) {
        if (level.isClientSide) return;

        float totalDamage = 12f + 18f * chargeTime;          // 12 ~ 30
        double radius = 8.0;
        Vec3 center = player.position();

        Predicate<LivingEntity> filter = e -> e.isAlive() && !e.isInvulnerable() && e != player &&
                e.getType().getCategory() == MobCategory.MONSTER;

        int hitCount = GTBSSDamageUtils.splitDamage(level, center, radius, totalDamage, filter, null);

        if (hitCount > 0) {
            int absorptionAmount = (int) (totalDamage * 0.3f);
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 10, absorptionAmount / 4));

            if (level instanceof ServerLevel serverLevel) {
                int ringCount = 20;
                for (int i = 0; i < ringCount; i++) {
                    double angle = 2 * Math.PI * i / ringCount;
                    double x = center.x + radius * 0.8 * Math.cos(angle);
                    double z = center.z + radius * 0.8 * Math.sin(angle);
                    serverLevel.sendParticles(ParticleTypes.INSTANT_EFFECT,
                            x, center.y + 0.5, z,
                            1, 0, 0, 0, 0);
                }
                serverLevel.sendParticles(ParticleTypes.WITCH,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        40, 0.6, 0.6, 0.6, 0.1);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_AMBIENT,
                    SoundSource.PLAYERS, 1.0f, 0.8f);
        }
    }

    @Override
    public float getChargeTimeModifier() {
        return 0.5f;
    }

    @Override
    public int getManaCost() {
        return 2100;
    }
}

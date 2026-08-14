package top.ialdaiaxiariyay.gtbss.common.item.module;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.item.MagicModuleItem;
import top.ialdaiaxiariyay.gtbss.utils.GTBSSDamageUtil;
import top.ialdaiaxiariyay.gtbss.utils.GTBSSUtil;

import java.util.function.Predicate;

public class AutumnModule extends MagicModuleItem {

    public AutumnModule(Properties properties) {
        super(properties);
    }

    @Override
    public void cast(@NotNull Level level, Player player, ItemStack wand, float chargeTime) {
        if (level.isClientSide) return;

        float totalDamage = 10f + 20f * chargeTime;          // 10 ~ 30
        double radius = 8.0;
        Vec3 center = player.position();

        Predicate<LivingEntity> filter = e -> e.isAlive() && !e.isInvulnerable() && e != player &&
                e.getType().getCategory() == MobCategory.MONSTER;

        int hitCount = GTBSSDamageUtil.splitDamage(level, center, radius, totalDamage, filter, null);

        if (hitCount > 0) {

            float healAmount = totalDamage * 0.5f;
            player.heal(healAmount);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.INSTANT_EFFECT,
                        center.x, center.y + 0.5, center.z,
                        40, radius * 0.8, radius * 0.8, radius * 0.8, 0.0);
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        20, 0.5, 0.5, 0.5, 0.0);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    @Override
    public float getChargeTimeModifier() {
        return 0.10f;
    }

    @Override
    public int getManaCost() {
        return 2100;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        if (level.isClientSide) {
            GTBSSUtil.openMarkdownViewScreenGui(GTBSS.id("docs/readme.md"));
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }
}

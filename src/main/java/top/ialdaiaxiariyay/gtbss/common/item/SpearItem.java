package top.ialdaiaxiariyay.gtbss.common.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.common.entity.SpearEntity;

import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SpearItem extends TridentItem {

    private static final float STRAIN_INCREMENT = 0.2F;
    private static final float STRAIN_DECREMENT_PER_TICK = 0.0005F;
    private static final double IMPULSE_FORCE = 3.0;
    public static final String STRAIN_KEY = "strain";

    private final Supplier<EntityType<SpearEntity>> spearEntityTypeSupplier;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public SpearItem(Properties properties, Supplier<EntityType<SpearEntity>> entityTypeSupplier) {
        super(properties);
        this.spearEntityTypeSupplier = entityTypeSupplier;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 9.0F,
                AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.9F,
                AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player) {
            CompoundTag tag = stack.getOrCreateTag();
            float strain = tag.getFloat(STRAIN_KEY);
            if (strain > 0) {
                strain = Math.max(0, strain - STRAIN_DECREMENT_PER_TICK);
                tag.putFloat(STRAIN_KEY, strain);
            }
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player)) return;

        int useTime = this.getUseDuration(stack) - timeLeft;
        if (useTime < 10) return;

        CompoundTag tag = stack.getOrCreateTag();
        float strain = tag.getFloat(STRAIN_KEY);
        strain = Math.min(1.0F, strain + STRAIN_INCREMENT);
        tag.putFloat(STRAIN_KEY, strain);

        if (!level.isClientSide) {
            Vec3 look = player.getLookAngle();
            Vec3 throwDir = look.scale(-1.0);

            EntityType<SpearEntity> type = spearEntityTypeSupplier.get();
            SpearEntity spear = new SpearEntity(type, level, player, stack, throwDir);
            spear.setStrain(strain);
            float power = 2.5F;
            spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power, 1.0F);
            if (player.getAbilities().instabuild) {
                spear.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(spear);
            level.playSound(null, spear, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

            Vec3 impulse = throwDir.scale(IMPULSE_FORCE);
            player.setDeltaMovement(player.getDeltaMovement().add(impulse));
            player.hurtMarked = true;
            player.fallDistance = 0;
            player.getPersistentData().putLong("SpearImpulseTime", level.getGameTime());

            if (!player.getAbilities().instabuild) {
                player.getInventory().removeItem(stack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (EnchantmentHelper.getRiptide(stack) > 0 && !player.isInWaterOrRain()) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers :
                super.getDefaultAttributeModifiers(equipmentSlot);
    }
}

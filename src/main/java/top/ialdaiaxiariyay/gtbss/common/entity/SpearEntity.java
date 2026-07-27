package top.ialdaiaxiariyay.gtbss.common.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSEnchantments;
import top.ialdaiaxiariyay.gtbss.common.item.SpearItem;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("deprecation")
public class SpearEntity extends AbstractArrow {

    private static final EntityDataAccessor<Boolean> DATA_RETURNING = SynchedEntityData.defineId(SpearEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_STRAIN = SynchedEntityData.defineId(SpearEntity.class,
            EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_RETURN_TARGET_HAND = SynchedEntityData
            .defineId(SpearEntity.class, EntityDataSerializers.INT);

    private static final float RETURN_SPEED_BASE = 10.0F;
    private static final float REACH_DISTANCE = 1.5F;
    private static final float AUTO_RETURN_DISTANCE = 100.0F;
    private Vec3 throwDirection;
    private final Set<Entity> hitEntities = new HashSet<>();
    private ItemStack spearItem = ItemStack.EMPTY;
    public int clientSideReturnTridentTickCount;

    public SpearEntity(EntityType<? extends SpearEntity> type, Level level) {
        super(type, level);
        this.setPierceLevel((byte) 0);
    }

    public SpearEntity(EntityType<? extends SpearEntity> type, Level level, LivingEntity shooter, ItemStack stack,
                       Vec3 throwDir) {
        super(type, shooter, level);
        CompoundTag tag = stack.getOrCreateTag();
        float strain = tag.getFloat(SpearItem.STRAIN_KEY);
        this.entityData.set(DATA_STRAIN, strain);
        this.entityData.set(DATA_RETURNING, false);
        this.entityData.set(DATA_RETURN_TARGET_HAND, 0);
        this.setBaseDamage(10.0);
        int pierce = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, stack);
        if (pierce > 0) this.setPierceLevel((byte) Math.min(pierce, 127));
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, stack) > 0)
            this.setSecondsOnFire(100);
        if (shooter instanceof Player player && player.getAbilities().instabuild)
            this.pickup = Pickup.CREATIVE_ONLY;
        else
            this.pickup = Pickup.ALLOWED;
        this.setOwner(shooter);
        this.throwDirection = throwDir.normalize();
        this.spearItem = stack.copy();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_RETURNING, false);
        this.entityData.define(DATA_STRAIN, 0.0F);
        this.entityData.define(DATA_RETURN_TARGET_HAND, 0);
    }

    public boolean isInGround() {
        return this.inGround;
    }

    public void setStrain(float strain) {
        this.entityData.set(DATA_STRAIN, Math.max(0, Math.min(1.0F, strain)));
    }

    public float getStrain() {
        return this.entityData.get(DATA_STRAIN);
    }

    public boolean isReturning() {
        return this.entityData.get(DATA_RETURNING);
    }

    public void startReturning(int handIndex) {
        if (this.isReturning()) return;
        this.entityData.set(DATA_RETURNING, true);
        this.entityData.set(DATA_RETURN_TARGET_HAND, handIndex);
        this.inGround = false;
        this.setNoPhysics(true);
        this.setDeltaMovement(Vec3.ZERO);
        this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
    }

    @Override
    public void tick() {
        if (this.isRemoved()) return;

        if (this.level().isClientSide) {
            if (this.isReturning()) ++this.clientSideReturnTridentTickCount;
            super.tick();
            return;
        }

        Entity owner = this.getOwner();

        if (this.isReturning()) {
            this.setNoPhysics(true);
            if (owner == null || !owner.isAlive()) {
                if (!this.level().isClientSide) {
                    this.spawnAtLocation(this.getPickupItem());
                }
                this.discard();
                return;
            }
            Vec3 targetPos = owner.getEyePosition();
            Vec3 delta = targetPos.subtract(this.position());
            double dist = delta.length();

            if (dist < REACH_DISTANCE) {
                this.returnToPlayer(owner);
                return;
            }

            float strain = this.getStrain();
            float speedMul = Math.max(0.3F, 1.0F - strain * 0.7F);
            double speed = RETURN_SPEED_BASE * speedMul;
            Vec3 targetMotion = delta.normalize().scale(speed);
            Vec3 current = this.getDeltaMovement();
            Vec3 newMotion = current.lerp(targetMotion, 0.2);
            this.setDeltaMovement(newMotion);

            float yRot = (float) (Math.atan2(newMotion.x, newMotion.z) * 180.0 / Math.PI);
            float xRot = (float) (Math.atan2(newMotion.y, newMotion.horizontalDistance()) * 180.0 / Math.PI);
            this.setYRot(yRot);
            this.setXRot(xRot);
            this.setPos(this.getX() + newMotion.x, this.getY() + newMotion.y, this.getZ() + newMotion.z);

            return;
        }

        super.tick();

        if (owner instanceof Player player) {
            float distToPlayer = player.distanceTo(this);
            if (distToPlayer > AUTO_RETURN_DISTANCE) {
                this.startReturning(0);
                this.setNoPhysics(true);
            }
        }
    }

    private void returnToPlayer(Entity owner) {
        if (this.isRemoved()) return;
        if (!(owner instanceof Player player)) {
            this.discard();
            return;
        }

        if (!player.isAlive()) {
            if (!this.level().isClientSide) {
                this.spawnAtLocation(this.getPickupItem());
            }
            this.discard();
            return;
        }

        this.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 1.0F);

        Vec3 horizontal = player.getLookAngle().scale(-1.0);
        horizontal = new Vec3(horizontal.x, 0, horizontal.z).normalize();
        double horizontalForce = 5.0;
        double verticalForce = 2.0;
        Vec3 impulse = horizontal.scale(horizontalForce).add(new Vec3(0, verticalForce, 0));
        player.push(impulse.x, impulse.y, impulse.z);
        player.hurtMarked = true;

        double verticalSpeed = player.getDeltaMovement().y;
        long estimatedTicks = verticalSpeed > 0 ? (long) (2 * verticalSpeed / 0.08) + 30 : 40;
        estimatedTicks = Math.max(estimatedTicks, 80);

        int featherFalling = EnchantmentHelper.getItemEnchantmentLevel(
                GTBSSEnchantments.SPEAR_FALL_PROTECTION.get(),
                this.spearItem);
        CompoundTag data = player.getPersistentData();
        data.remove("SpearImpulseTime");
        data.remove("SpearEstimatedFallTime");
        data.remove("SpearFeatherFalling");
        data.putLong("SpearImpulseTime", player.level().getGameTime());
        data.putLong("SpearEstimatedFallTime", estimatedTicks);
        if (featherFalling > 0) {
            data.putInt("SpearFeatherFalling", featherFalling);
        }

        float strain = this.getStrain();
        if (strain > 0.5F) player.setSecondsOnFire(2);

        ItemStack stack = this.getPickupItem();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putFloat(SpearItem.STRAIN_KEY, 0f);
        tag.putBoolean("recovered", true);

        int handIndex = this.entityData.get(DATA_RETURN_TARGET_HAND);
        InteractionHand hand = (handIndex == 0) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack current = player.getItemInHand(hand);
        if (current.isEmpty()) {
            player.setItemInHand(hand, stack);
        } else {
            InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND :
                    InteractionHand.MAIN_HAND;
            if (player.getItemInHand(other).isEmpty()) {
                player.setItemInHand(other, stack);
            } else {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            }
        }
        this.discard();
    }

    @Override
    public void playerTouch(Player player) {
        if (this.isReturning()) return;
        super.playerTouch(player);
    }

    // ===== 碰撞处理 =====
    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.isReturning()) return;

        Vec3 motion = this.getDeltaMovement();
        Vec3 normal = Vec3.atLowerCornerOf(result.getDirection().getNormal());
        double angle = Math.acos(motion.normalize().dot(normal));
        if (angle < Math.PI / 6.0) {
            Vec3 reflect = motion.subtract(normal.scale(2 * motion.dot(normal)));
            this.setDeltaMovement(reflect);
            this.playSound(SoundEvents.CHAIN_HIT, 0.5F, 1.0F);
            return;
        }

        this.inGround = true;
        this.setNoPhysics(false);
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(result.getLocation().subtract(Vec3.atLowerCornerOf(result.getDirection().getNormal()).scale(0.4)));
        this.playSound(SoundEvents.TRIDENT_HIT_GROUND, 1.0F, 1.0F);
        this.setPierceLevel((byte) 0);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.isReturning()) return;

        Entity target = result.getEntity();

        if (this.hitEntities.contains(target)) {
            return;
        }
        this.hitEntities.add(target);

        float strain = this.getStrain();

        if (strain > 0.5F && !target.fireImmune()) {
            target.setSecondsOnFire(5);
        }

        float damage = (float) this.getBaseDamage();
        if (target instanceof LivingEntity living) {
            damage += EnchantmentHelper.getDamageBonus(this.getPickupItem(), living.getMobType());
        }

        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().trident(this, owner == null ? this : owner);

        if (target.hurt(source, damage)) {
            if (target instanceof LivingEntity livingTarget) {
                if (owner instanceof LivingEntity livingOwner) {
                    EnchantmentHelper.doPostHurtEffects(livingTarget, livingOwner);
                    EnchantmentHelper.doPostDamageEffects(livingOwner, livingTarget);
                }
                this.doPostHurtEffects(livingTarget);
            }
            this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
            this.setYRot(this.getYRot() + 180.0F);
            this.yRotO += 180.0F;
        }

        this.setPierceLevel((byte) 0);

        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() > 0.001) {
            Vec3 dir = motion.normalize();
            this.setPos(this.getX() - dir.x * 0.5, this.getY() - dir.y * 0.5, this.getZ() - dir.z * 0.5);
        }
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return this.spearItem.copy();
    }

    @Override
    protected @NotNull SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("strain", this.getStrain());
        compound.putBoolean("returning", this.isReturning());
        compound.putInt("returnTargetHand", this.entityData.get(DATA_RETURN_TARGET_HAND));
        if (this.throwDirection != null) {
            compound.putDouble("ThrowDirX", this.throwDirection.x);
            compound.putDouble("ThrowDirY", this.throwDirection.y);
            compound.putDouble("ThrowDirZ", this.throwDirection.z);
        }
        compound.put("SpearItem", this.spearItem.save(new CompoundTag()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(DATA_STRAIN, compound.getFloat("strain"));
        this.entityData.set(DATA_RETURNING, compound.getBoolean("returning"));
        this.entityData.set(DATA_RETURN_TARGET_HAND, compound.getInt("returnTargetHand"));
        if (compound.contains("ThrowDirX")) {
            this.throwDirection = new Vec3(
                    compound.getDouble("ThrowDirX"),
                    compound.getDouble("ThrowDirY"),
                    compound.getDouble("ThrowDirZ"));
        }
        if (compound.contains("SpearItem")) {
            this.spearItem = ItemStack.of(compound.getCompound("SpearItem"));
        }
    }
}

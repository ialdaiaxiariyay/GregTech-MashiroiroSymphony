package top.ialdaiaxiariyay.gtbss.utils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class GTBSSDamageUtils {

    /**
     * Splits total damage equally among all entities within the spherical range.
     */
    public static int splitDamage(@NotNull Level level, Vec3 center, double radius,
                                  float totalDamage, Predicate<LivingEntity> filter) {
        return splitOperation(level, center, radius, totalDamage, filter,
                (e, amount) -> e.hurt(level.damageSources().magic(), amount));
    }

    /**
     * Splits total healing equally among all entities within the spherical range.
     */
    public static int splitHeal(@NotNull Level level, Vec3 center, double radius,
                                float totalHeal, Predicate<LivingEntity> filter) {
        return splitOperation(level, center, radius, totalHeal, filter,
                LivingEntity::heal);
    }

    private static int splitOperation(@NotNull Level level, Vec3 center, double radius,
                                      float totalAmount, Predicate<LivingEntity> filter,
                                      BiConsumer<LivingEntity, Float> action) {
        if (level.isClientSide) return 0;
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center.x - radius, center.y - radius, center.z - radius,
                        center.x + radius, center.y + radius, center.z + radius),
                e -> {
                    if (!filter.test(e)) return false;
                    return e.distanceToSqr(center) <= radius * radius;
                });
        if (targets.isEmpty()) return 0;
        float each = totalAmount / targets.size();
        for (LivingEntity e : targets) {
            action.accept(e, each);
        }
        return targets.size();
    }

    /**
     * Chain bounces: starting from the initial target, sequentially finds the nearest unhit entity.
     * The search uses spherical range (distance from current target).
     *
     * @param level       the world
     * @param origin      the starting point (used only when firstTarget is null)
     * @param firstTarget the first target (can be null, then automatically finds the nearest)
     * @param baseDamage  base damage
     * @param maxBounces  maximum number of bounces (including the first hit)
     * @param decayRate   decay rate per bounce (0~1)
     * @param range       search radius (spherical)
     * @param filter      target filter (should ensure entity is alive and vulnerable)
     * @return the actual number of bounces performed
     */
    public static int bounceDamage(@NotNull Level level, Vec3 origin, @Nullable LivingEntity firstTarget,
                                   float baseDamage, int maxBounces, float decayRate,
                                   double range, Predicate<LivingEntity> filter) {
        if (level.isClientSide) return 0;
        if (maxBounces <= 0 || baseDamage <= 0 || range <= 0) return 0;

        Set<LivingEntity> hit = new HashSet<>();
        LivingEntity current = firstTarget;

        if (current == null) {
            current = findNearest(level, origin, range, filter, hit);
            if (current == null) return 0;
        } else {
            if (!filter.test(current)) {
                current = findNearest(level, origin, range, filter, hit);
                if (current == null) return 0;
            }
        }

        float damage = baseDamage;
        int count = 0;
        while (current != null) {
            if (!filter.test(current) || hit.contains(current)) {
                break;
            }
            if (damage > 0) {
                current.hurt(level.damageSources().magic(), damage);
            }
            hit.add(current);
            count++;

            if (count >= maxBounces || damage * (1 - decayRate) <= 0) {
                break;
            }

            Vec3 center = current.position();
            current = findNearest(level, center, range, filter, hit);
            damage *= (1 - decayRate);
        }
        return count;
    }

    /**
     * Finds the nearest living entity within spherical range that satisfies the filter and is not in the hit set.
     * Returns null if none found.
     */
    @Nullable
    public static LivingEntity findNearest(@NotNull Level level, @NotNull Vec3 center, double radius,
                                           Predicate<LivingEntity> filter, Set<LivingEntity> hit) {
        AABB box = new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);
        double radiusSq = radius * radius;
        return level.getEntitiesOfClass(LivingEntity.class, box,
                e -> {
                    if (hit.contains(e)) return false;
                    if (!filter.test(e)) return false;
                    return e.distanceToSqr(center) <= radiusSq;
                })
                .stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(center)))
                .orElse(null);
    }

    public static int bounceDamage(Level level, Vec3 origin, LivingEntity firstTarget,
                                   float baseDamage, int maxBounces, float decayRate, double range) {
        return bounceDamage(level, origin, firstTarget, baseDamage, maxBounces, decayRate, range,
                e -> e.isAlive() && !e.isInvulnerable());
    }

    /**
     * Splits total damage equally among all entities within spherical range, with extra action per target.
     */
    public static int splitDamage(@NotNull Level level, Vec3 center, double radius,
                                  float totalDamage, Predicate<LivingEntity> filter,
                                  @Nullable BiConsumer<LivingEntity, Float> extraAction) {
        return splitOperation(level, center, radius, totalDamage, filter,
                (e, amount) -> {
                    e.hurt(level.damageSources().magic(), amount);
                    if (extraAction != null) extraAction.accept(e, amount);
                });
    }

    /**
     * Splits total healing equally among all entities within spherical range, with extra action per target.
     */
    public static int splitHeal(@NotNull Level level, Vec3 center, double radius,
                                float totalHeal, Predicate<LivingEntity> filter,
                                @Nullable BiConsumer<LivingEntity, Float> extraAction) {
        return splitOperation(level, center, radius, totalHeal, filter,
                (e, amount) -> {
                    e.heal(amount);
                    if (extraAction != null) extraAction.accept(e, amount);
                });
    }

    /**
     * Chain bounces with extra action on each hit target (e.g., play effects, apply knockback).
     * Returns the list of hit entities for further processing.
     */
    public static @NotNull List<LivingEntity> bounceDamageWithList(@NotNull Level level, Vec3 origin,
                                                                   @Nullable LivingEntity firstTarget,
                                                                   float baseDamage, int maxBounces, float decayRate,
                                                                   double range, Predicate<LivingEntity> filter,
                                                                   @Nullable BiConsumer<LivingEntity, Float> extraAction) {
        if (level.isClientSide) return Collections.emptyList();
        if (maxBounces <= 0 || baseDamage <= 0 || range <= 0) return Collections.emptyList();

        Set<LivingEntity> hit = new HashSet<>();
        List<LivingEntity> hitList = new ArrayList<>();
        LivingEntity current = firstTarget;

        if (current == null) {
            current = findNearest(level, origin, range, filter, hit);
            if (current == null) return Collections.emptyList();
        } else {
            if (!filter.test(current)) {
                current = findNearest(level, origin, range, filter, hit);
                if (current == null) return Collections.emptyList();
            }
        }

        float damage = baseDamage;
        while (current != null) {
            if (!filter.test(current) || hit.contains(current)) {
                break;
            }
            if (damage > 0) {
                current.hurt(level.damageSources().magic(), damage);
                if (extraAction != null) extraAction.accept(current, damage);
            }
            hit.add(current);
            hitList.add(current);

            if (hitList.size() >= maxBounces || damage * (1 - decayRate) <= 0) {
                break;
            }

            Vec3 center = current.position();
            current = findNearest(level, center, range, filter, hit);
            damage *= (1 - decayRate);
        }
        return hitList;
    }
}

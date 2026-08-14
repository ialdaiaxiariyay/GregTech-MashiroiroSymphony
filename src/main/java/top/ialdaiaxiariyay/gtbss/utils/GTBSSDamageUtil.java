package top.ialdaiaxiariyay.gtbss.utils;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class GTBSSDamageUtil {

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

    /**
     * Splits total damage equally with an extra action per target.
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
     * Splits total healing equally with an extra action per target.
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

    private static int splitOperation(@NotNull Level level, Vec3 center, double radius,
                                      float totalAmount, Predicate<LivingEntity> filter,
                                      BiConsumer<LivingEntity, Float> action) {
        if (level.isClientSide) return 0;
        if (totalAmount <= 0 || radius <= 0) return 0;

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center.x - radius, center.y - radius, center.z - radius,
                        center.x + radius, center.y + radius, center.z + radius),
                e -> {
                    if (!e.isAlive() || e.isInvulnerable()) return false;
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
     * Chain bounces with default magic damage source.
     *
     * @see #bounceDamage(Level, Vec3, LivingEntity, float, int, float, double, Predicate, DamageSource)
     */
    public static int bounceDamage(@NotNull Level level, Vec3 origin, @Nullable LivingEntity firstTarget,
                                   float baseDamage, int maxBounces, float decayRate,
                                   double range, Predicate<LivingEntity> filter) {
        return bounceDamage(level, origin, firstTarget, baseDamage, maxBounces, decayRate,
                range, filter, level.damageSources().magic());
    }

    /**
     * Chain bounces with a custom damage source.
     * If {@code firstTarget} is {@code null}, the nearest valid target from {@code origin} is used.
     * If {@code firstTarget} is non-null but invalid (does not pass filters or is already hit),
     * the method returns 0 immediately (no automatic fallback).
     *
     * @param level       the world
     * @param origin      starting point (used only when firstTarget is null)
     * @param firstTarget first target (maybe null for auto‑search)
     * @param baseDamage  base damage
     * @param maxBounces  maximum number of bounces (including the first hit)
     * @param decayRate   decay rate per bounce (0~1)
     * @param range       search radius (spherical)
     * @param filter      additional target filter (will be combined with mandatory alive/invulnerable checks)
     * @param source      the damage source to apply
     * @return the actual number of bounces performed
     */
    public static int bounceDamage(@NotNull Level level, Vec3 origin, @Nullable LivingEntity firstTarget,
                                   float baseDamage, int maxBounces, float decayRate,
                                   double range, Predicate<LivingEntity> filter,
                                   DamageSource source) {
        if (level.isClientSide) return 0;
        if (baseDamage <= 0 || maxBounces <= 0 || range <= 0) return 0;
        if (decayRate < 0 || decayRate > 1) return 0;

        List<LivingEntity> result = bounceCore(level, origin, firstTarget, baseDamage,
                maxBounces, decayRate, range, filter, source,
                null);
        return result.size();
    }

    /**
     * Chain bounces with extra action per hit, returns the list of hit entities.
     * Default magic damage source.
     *
     * @see #bounceDamageWithList(Level, Vec3, LivingEntity, float, int, float, double, Predicate, BiConsumer,
     *      DamageSource)
     */
    public static @NotNull List<LivingEntity> bounceDamageWithList(@NotNull Level level, Vec3 origin,
                                                                   @Nullable LivingEntity firstTarget,
                                                                   float baseDamage, int maxBounces, float decayRate,
                                                                   double range, Predicate<LivingEntity> filter,
                                                                   @Nullable BiConsumer<LivingEntity, Float> extraAction) {
        return bounceDamageWithList(level, origin, firstTarget, baseDamage, maxBounces, decayRate,
                range, filter, extraAction, level.damageSources().magic());
    }

    /**
     * Chain bounces with custom damage source and extra action, returns the list of hit entities.
     * The same fallback rules as
     * {@link #bounceDamage(Level, Vec3, LivingEntity, float, int, float, double, Predicate, DamageSource)} apply.
     */
    public static @NotNull List<LivingEntity> bounceDamageWithList(@NotNull Level level, Vec3 origin,
                                                                   @Nullable LivingEntity firstTarget,
                                                                   float baseDamage, int maxBounces, float decayRate,
                                                                   double range, Predicate<LivingEntity> filter,
                                                                   @Nullable BiConsumer<LivingEntity, Float> extraAction,
                                                                   DamageSource source) {
        if (level.isClientSide) return Collections.emptyList();
        if (baseDamage <= 0 || maxBounces <= 0 || range <= 0) return Collections.emptyList();
        if (decayRate < 0 || decayRate > 1) return Collections.emptyList();

        return bounceCore(level, origin, firstTarget, baseDamage,
                maxBounces, decayRate, range, filter, source,
                extraAction);
    }

    /**
     * Core bounce implementation.
     *
     * @param extraAction optional action to perform after each hit (e.g. effects, knockback)
     * @return list of all entities hit (in order of bounce), empty if none
     */
    @NotNull
    private static List<LivingEntity> bounceCore(@NotNull Level level, Vec3 origin,
                                                 @Nullable LivingEntity firstTarget,
                                                 float baseDamage, int maxBounces, float decayRate,
                                                 double range, Predicate<LivingEntity> userFilter,
                                                 DamageSource source,
                                                 @Nullable BiConsumer<LivingEntity, Float> extraAction) {
        Predicate<LivingEntity> fullFilter = e -> e.isAlive() && !e.isInvulnerable() && userFilter.test(e);

        Set<LivingEntity> hitSet = new HashSet<>();
        List<LivingEntity> hitList = new ArrayList<>();

        LivingEntity current;
        if (firstTarget == null) {
            current = findNearest(level, origin, range, fullFilter, hitSet);
            if (current == null) return Collections.emptyList();
        } else {
            if (!fullFilter.test(firstTarget)) {
                return Collections.emptyList();
            }
            current = firstTarget;
        }

        float damage = baseDamage;
        while (current != null) {
            if (damage <= 0) break;
            if (!fullFilter.test(current) || hitSet.contains(current)) {
                break;
            }

            current.hurt(source, damage);
            if (extraAction != null) extraAction.accept(current, damage);

            hitSet.add(current);
            hitList.add(current);

            if (hitList.size() >= maxBounces) break;

            damage *= (1 - decayRate);
            if (damage <= 0) break;

            Vec3 center = current.position();
            current = findNearest(level, center, range, fullFilter, hitSet);
        }
        return hitList;
    }

    /**
     * Finds the nearest living entity within spherical range that satisfies the filter and is not in the hit set.
     * Returns null if none found.
     * This implementation performs a single pass over the entity list for efficiency.
     */
    @Nullable
    public static LivingEntity findNearest(@NotNull Level level, @NotNull Vec3 center, double radius,
                                           Predicate<LivingEntity> filter, Set<LivingEntity> hit) {
        AABB box = new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);
        double radiusSq = radius * radius;

        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> {
                    if (hit.contains(e)) return false;
                    if (!filter.test(e)) return false;
                    return e.distanceToSqr(center) <= radiusSq;
                });

        if (candidates.isEmpty()) return null;

        LivingEntity nearest = candidates.get(0);
        double minDistSq = nearest.distanceToSqr(center);
        for (int i = 1; i < candidates.size(); i++) {
            LivingEntity e = candidates.get(i);
            double d = e.distanceToSqr(center);
            if (d < minDistSq) {
                minDistSq = d;
                nearest = e;
            }
        }
        return nearest;
    }

    /**
     * Simplified bounce with default filter (alive and not invulnerable).
     */
    public static int bounceDamage(Level level, Vec3 origin, LivingEntity firstTarget,
                                   float baseDamage, int maxBounces, float decayRate, double range) {
        return bounceDamage(level, origin, firstTarget, baseDamage, maxBounces, decayRate, range,
                e -> true, level.damageSources().magic());
    }

    /**
     * Simplified bounce with default filter and custom damage source.
     */
    public static int bounceDamage(Level level, Vec3 origin, LivingEntity firstTarget,
                                   float baseDamage, int maxBounces, float decayRate,
                                   double range, DamageSource source) {
        return bounceDamage(level, origin, firstTarget, baseDamage, maxBounces, decayRate, range,
                e -> true, source);
    }
}

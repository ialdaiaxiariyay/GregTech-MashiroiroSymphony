package top.ialdaiaxiariyay.gtbss.common;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.player.PlayerSanity;
import top.ialdaiaxiariyay.gtbss.api.player.SanityProvider;
import top.ialdaiaxiariyay.gtbss.api.wireless.WirelessContainer;
import top.ialdaiaxiariyay.gtbss.api.wireless.WirelessData;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSDimension;

import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(modid = GTBSS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventListener {

    public static final Capability<PlayerSanity> SANITY = CapabilityManager.get(new CapabilityToken<>() {});

    @Mod.EventBusSubscriber(modid = GTBSS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void registerCapabilities(@NotNull RegisterCapabilitiesEvent event) {
            event.register(PlayerSanity.class);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.getAbilities().flying) {
            if (player.xxa == 0.0F && player.zza == 0.0F) {
                Vec3 velocity = player.getDeltaMovement();
                player.setDeltaMovement(0.0, velocity.y, 0.0);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = GTBSS.MOD_ID)
    public static class AttachHandler {

        @SubscribeEvent
        public static void attachToPlayer(@NotNull AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                event.addCapability(GTBSS.id("sanity"), new SanityProvider());
            }
        }
    }

    @SubscribeEvent
    public static void serverSetup(LevelEvent.@NotNull Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
            if (overworld == null) return;
            WirelessData.INSTANCE = WirelessData.getOrCreate(overworld);
            WirelessContainer.server = level.getServer();
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        event.getEntity().getPersistentData().remove("SpearMarked");

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!player.level().dimension().location().equals(GTBSS.id(GTBSSDimension.TheDarkroom))) {
            return;
        }
        event.setCanceled(true);
        player.setHealth(player.getMaxHealth());
        FoodData food = player.getFoodData();
        food.setFoodLevel(20);
        food.setSaturation(5.0f);
        player.setAirSupply(player.getMaxAirSupply());
        player.clearFire();
        player.fallDistance = 0.0f;

        BlockPos spawnPos = player.getRespawnPosition();
        ResourceKey<Level> spawnDim = player.getRespawnDimension();

        if (spawnPos == null) {
            spawnPos = player.server.overworld().getSharedSpawnPos();
            spawnDim = Level.OVERWORLD;
        }

        player.teleportTo(
                Objects.requireNonNull(player.server.getLevel(spawnDim)),
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                player.getYRot(),
                player.getXRot());
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!player.level().dimension().location().equals(GTBSS.id(GTBSSDimension.TheDarkroom))) {
            return;
        }
        event.getDrops().clear();
    }

    @SubscribeEvent
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!player.level().dimension().location().equals(GTBSS.id(GTBSSDimension.TheDarkroom))) {
            return;
        }
        event.setDroppedExperience(0);
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CompoundTag data = player.getPersistentData();
        if (!data.contains("SpearImpulseTime") || !data.contains("SpearEstimatedFallTime")) return;

        long impulseTime = data.getLong("SpearImpulseTime");
        long estimatedTicks = data.getLong("SpearEstimatedFallTime");
        long currentTime = player.level().getGameTime();

        int level = data.getInt("SpearFeatherFalling");
        data.remove("SpearImpulseTime");
        data.remove("SpearEstimatedFallTime");
        data.remove("SpearFeatherFalling");

        if (currentTime - impulseTime <= estimatedTicks && level > 0) {
            if (level >= 5) {
                event.setCanceled(true);
            } else {
                float reduction = Math.min(0.8f, level * 0.3f);
                event.setDamageMultiplier(1.0f - reduction);
            }
        }
    }
}

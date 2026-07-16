package top.ialdaiaxiariyay.gtbss.common;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.player.PlayerSanity;
import top.ialdaiaxiariyay.gtbss.api.player.SanityProvider;
import top.ialdaiaxiariyay.gtbss.api.wireless.WirelessContainer;
import top.ialdaiaxiariyay.gtbss.api.wireless.WirelessData;

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
}

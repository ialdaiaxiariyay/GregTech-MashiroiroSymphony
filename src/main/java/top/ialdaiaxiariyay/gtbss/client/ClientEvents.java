package top.ialdaiaxiariyay.gtbss.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.client.renderer.SpearRenderer;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSDimension;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSEntityTypes;
import top.ialdaiaxiariyay.gtbss.common.entity.SpearEntity;
import top.ialdaiaxiariyay.gtbss.network.NetworkHandler;
import top.ialdaiaxiariyay.gtbss.network.packet.SpearRecallPacket;

import static top.ialdaiaxiariyay.gtbss.common.ForgeEventListener.SANITY;

public class ClientEvents {

    public static final ResourceLocation CUSTOM_DIMENSION = GTBSS.id(GTBSSDimension.TheDarkroom);

    private static final double MAX_SEARCH_DIST = 100.0;
    private static final float DOT_THRESHOLD = 0.95F;
    private static final int INDICATOR_SIZE = 32;
    private static final ResourceLocation SPEAR_INDICATOR = GTBSS.id("textures/gui/spear_indicator.png");
    public static final String KEY_CATEGORY = "key.category.gtbss";
    public static final String KEY_RECALL_SPEAR = "key.gtbss.recall_spear";
    public static KeyMapping RECALL_SPEAR_KEY = new KeyMapping(
            KEY_RECALL_SPEAR,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            KEY_CATEGORY);

    public static boolean isPlayerInCustomDimension(@NotNull Player player) {
        return player.level().dimension().location().equals(CUSTOM_DIMENSION);
    }

    @Mod.EventBusSubscriber(modid = GTBSS.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {

        @SuppressWarnings("InstantiationOfUtilityClass")
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                while (RECALL_SPEAR_KEY.consumeClick()) {
                    NetworkHandler.sendToServer(new SpearRecallPacket());
                }
            }
        }

        @SubscribeEvent
        public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;
            if (!isPlayerInCustomDimension(player)) return;

            player.getCapability(SANITY).ifPresent(sanity -> {
                int value = sanity.getSanity();
                GuiGraphics graphics = event.getGuiGraphics();
                int screenWidth = mc.getWindow().getGuiScaledWidth();
                int screenHeight = mc.getWindow().getGuiScaledHeight();
                int barWidth = 100;
                int barHeight = 5;
                int margin = 10;
                int barX = screenWidth - barWidth - margin;
                int barY = screenHeight - barHeight - margin;
                graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xAA444444);
                int filledWidth = (int) (barWidth * value / 100.0f);
                graphics.fill(barX, barY, barX + filledWidth, barY + barHeight, 0xFF44AA44);
                int textY = barY - mc.font.lineHeight - 2;
                Component sanityText = Component.translatable("gui.gtbss.sanity", value);
                graphics.drawString(mc.font, sanityText, barX, textY, 0xFFFFFF);
            });
        }

        @SubscribeEvent
        public static void onRenderSpearIndicator(RenderGuiOverlayEvent.Post event) {
            if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) return;

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;

            SpearEntity targetSpear = null;
            for (SpearEntity spear : player.level().getEntitiesOfClass(
                    SpearEntity.class,
                    player.getBoundingBox().inflate(MAX_SEARCH_DIST))) {
                if (!spear.isInGround() || spear.isReturning()) continue;
                if (spear.getOwner() != player) continue;

                float dist = player.distanceTo(spear);
                if (!player.isCrouching() && dist <= 8.0F) continue;

                Vec3 eye = player.getEyePosition();
                Vec3 toSpear = spear.position().subtract(eye).normalize();
                if (toSpear.dot(player.getLookAngle()) < DOT_THRESHOLD) continue;

                targetSpear = spear;
                break;
            }

            if (targetSpear == null) return;

            GuiGraphics gui = event.getGuiGraphics();
            int centerX = mc.getWindow().getGuiScaledWidth() / 2;
            int centerY = mc.getWindow().getGuiScaledHeight() / 2;

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            gui.blit(SPEAR_INDICATOR,
                    centerX - INDICATOR_SIZE / 2,
                    centerY - INDICATOR_SIZE / 2,
                    0, 0, INDICATOR_SIZE, INDICATOR_SIZE, INDICATOR_SIZE, INDICATOR_SIZE);
            RenderSystem.disableBlend();
        }
    }

    @Mod.EventBusSubscriber(modid = GTBSS.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(GTBSSEntityTypes.SPEAR.get(), SpearRenderer::new);
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(RECALL_SPEAR_KEY);
        }
    }
}

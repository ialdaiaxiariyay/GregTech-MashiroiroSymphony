package top.ialdaiaxiariyay.gtms.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.common.data.GTMSDimension;

import static top.ialdaiaxiariyay.gtms.common.ForgeEventListener.SANITY;

@Mod.EventBusSubscriber(modid = GTMS.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    public static final ResourceLocation CUSTOM_DIMENSION = GTMS.id(GTMSDimension.TheDarkroom);

    public static boolean isPlayerInCustomDimension(@NotNull Player player) {
        return player.level().dimension().location().equals(CUSTOM_DIMENSION);
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
            Component sanityText = Component.translatable("gui.gtms.sanity", value);
            graphics.drawString(mc.font, sanityText, barX, textY, 0xFFFFFF);
        });
    }
}

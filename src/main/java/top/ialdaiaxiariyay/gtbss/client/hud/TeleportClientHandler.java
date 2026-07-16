package top.ialdaiaxiariyay.gtbss.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.mojang.blaze3d.systems.RenderSystem;

@OnlyIn(Dist.CLIENT)
public class TeleportClientHandler {

    private static int animationTicks = 0;
    private static final int MAX_TICKS = 20 * 2;

    private static final ResourceLocation WATER_TEXTURE = ResourceLocation.parse("textures/block/water_still.png");

    public static void startAnimation() {
        animationTicks = MAX_TICKS;
        MinecraftForge.EVENT_BUS.register(new TeleportClientHandler());
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        if (animationTicks <= 0) {
            MinecraftForge.EVENT_BUS.unregister(this);
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        float progress = (float) animationTicks / MAX_TICKS;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int alpha = (int) (200 * progress);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha / 255.0F);

        float scaleUV = 0.5f + (1 - progress) * 1.5f;
        float offsetX = (MAX_TICKS - animationTicks) * 0.02f;
        float offsetY = (MAX_TICKS - animationTicks) * 0.01f;

        graphics.pose().pushPose();
        graphics.blit(WATER_TEXTURE, 0, 0, screenWidth, screenHeight,
                offsetX, offsetY, (int) (screenWidth * scaleUV), (int) (screenHeight * scaleUV),
                (int) (screenWidth * scaleUV), (int) (screenHeight * scaleUV));

        graphics.pose().popPose();

        ResourceLocation SPLASH = ResourceLocation.parse("textures/particle/splash_0.png");
        float splashScale = 0.8f + 0.6f * (1 - progress);
        int splashAlpha = (int) (200 * progress);
        int splashSize = (int) (80 * splashScale);
        int x = centerX - splashSize / 2;
        int y = centerY - splashSize / 2;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, splashAlpha / 255.0F);
        graphics.blit(SPLASH, x, y, splashSize, splashSize, 0, 0, 1, 1, 1, 1);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        animationTicks--;
    }
}

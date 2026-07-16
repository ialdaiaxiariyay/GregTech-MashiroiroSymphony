package top.ialdaiaxiariyay.gtbss.mixin.mc.client.gui.screens;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ModListScreen;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.gui.ButtonEntry;
import top.ialdaiaxiariyay.gtbss.api.gui.CustomButton;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private static final ResourceLocation BG_MORNING = GTBSS.id("textures/gui/title/background_morning.png");
    @Unique
    private static final ResourceLocation BG_DAY = GTBSS.id("textures/gui/title/background_day.png");
    @Unique
    private static final ResourceLocation BG_EVENING = GTBSS.id("textures/gui/title/background_evening.png");
    @Unique
    private static final ResourceLocation BG_NIGHT = GTBSS.id("textures/gui/title/background_night.png");

    @Unique
    private static final long TRANSITION_DURATION = 2500;

    @Unique
    private ResourceLocation gtbss$currentBg = null;
    @Unique
    private ResourceLocation gtbss$targetBg = null;
    @Unique
    private long gtbss$transitionStart = 0;
    @Unique
    private boolean gtbss$isTransitioning = false;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private ResourceLocation gtbss$getBackgroundForHour(int hour) {
        if (hour >= 5 && hour < 9) return BG_MORNING;
        else if (hour >= 9 && hour < 17) return BG_DAY;
        else if (hour >= 17 && hour < 20) return BG_EVENING;
        else return BG_NIGHT;
    }

    @Unique
    private void gtbss$drawBackground(GuiGraphics guiGraphics, ResourceLocation texture, float alpha) {
        if (texture == null) return;

        int[] size = new int[] { guiGraphics.guiWidth(), guiGraphics.guiHeight() };
        int imgWidth = size[0];
        int imgHeight = size[1];

        float screenRatio = (float) this.width / this.height;
        float imgRatio = (float) imgWidth / imgHeight;

        int drawWidth, drawHeight;
        int offsetX, offsetY;

        if (screenRatio > imgRatio) {
            drawHeight = this.height;
            drawWidth = (int) (this.height * imgRatio);
            offsetX = (this.width - drawWidth) / 2;
            offsetY = 0;
        } else {
            drawWidth = this.width;
            drawHeight = (int) (this.width / imgRatio);
            offsetX = 0;
            offsetY = (this.height - drawHeight) / 2;
        }

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        guiGraphics.blit(texture,
                offsetX, offsetY, drawWidth, drawHeight,
                0, 0, imgWidth, imgHeight,
                imgWidth, imgHeight);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // 重置
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.clearWidgets();

        int buttonWidth = 200;
        int buttonHeight = 20;
        int buttonSpacing = 2;

        List<ButtonEntry> buttons = new ArrayList<>();
        if (this.minecraft != null) {
            buttons.add(new ButtonEntry("menu.singleplayer", btn -> minecraft.setScreen(new SelectWorldScreen(this))));
            buttons.add(
                    new ButtonEntry("menu.multiplayer", btn -> minecraft.setScreen(new JoinMultiplayerScreen(this))));
            buttons.add(new ButtonEntry("fml.menu.mods", btn -> minecraft.setScreen(new ModListScreen(this))));
            buttons.add(new ButtonEntry("narrator.button.language", btn -> minecraft
                    .setScreen(new LanguageSelectScreen(this, minecraft.options, minecraft.getLanguageManager()))));
            buttons.add(new ButtonEntry("menu.options",
                    btn -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))));
            buttons.add(new ButtonEntry("menu.quit", btn -> minecraft.stop()));
        }

        int startX = this.width / 2 - buttonWidth / 2;
        int y = this.height / 4 + 48;

        for (ButtonEntry entry : buttons) {
            this.addRenderableWidget(new CustomButton(
                    startX, y, buttonWidth, buttonHeight,
                    Component.translatable(entry.translationKey),
                    entry.onPress));
            y += buttonHeight + buttonSpacing;
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF000000);

        int hour = LocalTime.now().getHour();
        ResourceLocation newBg = gtbss$getBackgroundForHour(hour);

        if (gtbss$currentBg == null) {
            gtbss$currentBg = newBg;
            gtbss$targetBg = newBg;
            gtbss$isTransitioning = false;
        }

        if (!newBg.equals(gtbss$currentBg) && !newBg.equals(gtbss$targetBg)) {
            gtbss$targetBg = newBg;
            gtbss$transitionStart = System.currentTimeMillis();
            gtbss$isTransitioning = true;
        }

        float progress = 0f;
        if (gtbss$isTransitioning) {
            progress = (System.currentTimeMillis() - gtbss$transitionStart) / (float) TRANSITION_DURATION;
            if (progress >= 1f) {
                progress = 1f;
                gtbss$currentBg = gtbss$targetBg;
                gtbss$isTransitioning = false;
            }
        }

        if (gtbss$isTransitioning) {
            gtbss$drawBackground(guiGraphics, gtbss$currentBg, 1f - progress);
            gtbss$drawBackground(guiGraphics, gtbss$targetBg, progress);
        } else {
            gtbss$drawBackground(guiGraphics, gtbss$currentBg, 1f);
        }
    }

    @Redirect(method = "render",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/PanoramaRenderer;render(FF)V"))
    private void redirectPanorama(PanoramaRenderer panorama, float partialTick, float fade) {}

    @Redirect(method = "render",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/components/LogoRenderer;renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IF)V"))
    private void redirectLogo(LogoRenderer logo, GuiGraphics guiGraphics, int width, float alpha) {}

    @Redirect(method = "render",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/components/SplashRenderer;render(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/client/gui/Font;I)V"))
    private void redirectSplash(SplashRenderer splash, GuiGraphics guiGraphics, int width, Font font, int color) {}
}

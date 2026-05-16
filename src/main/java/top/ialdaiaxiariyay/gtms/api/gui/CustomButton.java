package top.ialdaiaxiariyay.gtms.api.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.common.data.GTMSSoundEvent;
import top.ialdaiaxiariyay.gtms.mixin.mc.MinecraftAccessor;
import top.ialdaiaxiariyay.gtms.mixin.mc.gui.FontManagerAccessor;

@OnlyIn(Dist.CLIENT)
public class CustomButton extends Button {

    private boolean wasHovered = false;

    private static final int COLOR_NORMAL = 0xFFFFFF;
    private static final int COLOR_HOVERED = 0x527DB5;
    private static final int COLOR_BORDER = 0xAA000000;

    private final Font font;

    public CustomButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        FontManager fontManager = ((MinecraftAccessor) Minecraft.getInstance()).getFontManager();
        FontSet customFontSet = ((FontManagerAccessor) fontManager).getFontSets()
                .get(GTMS.id("jiangcheng_rounded_font_400w"));
        this.font = new Font(id -> customFontSet, false);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Component message = this.getMessage();
        int color = this.isHovered() ? COLOR_HOVERED : COLOR_NORMAL;
        int x = this.getX() + (this.width - font.width(message)) / 2;
        int y = this.getY() + (this.height - font.lineHeight) / 2;

        // 前景文字
        guiGraphics.drawString(font, message, x, y, color, false);

        // 悬停音效
        boolean isHovered = this.isHovered();
        if (isHovered != wasHovered) {
            if (isHovered) {
                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(GTMSSoundEvent.UI_BUTTON_HOVER.getMainEvent(), 1.0F));
            }
            wasHovered = isHovered;
        }
    }

    @Override
    public void playDownSound(@NotNull SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(GTMSSoundEvent.UI_BUTTON_CLICK_1.getMainEvent(), 1.0F));
    }
}

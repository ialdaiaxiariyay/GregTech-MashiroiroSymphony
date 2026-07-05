package top.ialdaiaxiariyay.gtms.api.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CustomButton extends Button {

    private static final int COLOR_NORMAL = 0xFFFFFF;
    private static final int COLOR_HOVERED = 0x527DB5;

    private final Font font;

    public CustomButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Component message = this.getMessage();
        int color = this.isHovered() ? COLOR_HOVERED : COLOR_NORMAL;
        int x = this.getX() + (this.width - font.width(message)) / 2;
        int y = this.getY() + (this.height - font.lineHeight) / 2;
        guiGraphics.drawString(font, message, x, y, color, false);
    }
}

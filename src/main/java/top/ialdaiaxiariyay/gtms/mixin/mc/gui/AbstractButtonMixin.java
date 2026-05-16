package top.ialdaiaxiariyay.gtms.mixin.mc.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ialdaiaxiariyay.gtms.common.data.GTMSSoundEvent;

@Mixin(AbstractButton.class)
public abstract class AbstractButtonMixin extends AbstractWidget {

    public AbstractButtonMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Unique
    private boolean gtms$wasHovered = false;

    @Inject(method = "renderWidget", at = @At("TAIL"))
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        boolean isHovered = this.isHovered();
        if (isHovered != gtms$wasHovered) {
            if (isHovered) {
                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(GTMSSoundEvent.UI_BUTTON_HOVER.getMainEvent(), 1.0F));
            }
            gtms$wasHovered = isHovered;
        }
    }

    @Override
    public void playDownSound(@NotNull SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(GTMSSoundEvent.UI_BUTTON_CLICK_1.getMainEvent(), 1.0F));
    }
}

package top.ialdaiaxiariyay.gtbss.mixin.forge.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.TitleScreenModUpdateIndicator;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreenModUpdateIndicator.class)
public abstract class TitleScreenModUpdateIndicatorMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, @NotNull CallbackInfo ci) {
        ci.cancel();
    }
}

package top.ialdaiaxiariyay.gtbss.mixin.forge.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.forge.snapshots.ForgeSnapshotsModClient;
import net.minecraftforge.versions.forge.ForgeVersion;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(ForgeHooksClient.class)
public abstract class ForgeHooksClientMixin {

    @Inject(method = "renderMainMenu", at = @At("HEAD"), cancellable = true, remap = false)
    private static void renderMainMenu(TitleScreen gui, GuiGraphics guiGraphics, Font font, int width, int height,
                                       int alpha, @NotNull CallbackInfo ci) {
        VersionChecker.Status status = ForgeVersion.getStatus();
        ForgeSnapshotsModClient.renderMainMenuWarning(status, gui, guiGraphics, font, width, height, alpha);
        ci.cancel();
    }
}

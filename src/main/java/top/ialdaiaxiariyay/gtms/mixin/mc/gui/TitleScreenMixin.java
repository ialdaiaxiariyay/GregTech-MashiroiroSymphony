package top.ialdaiaxiariyay.gtms.mixin.mc.gui;

import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ModListScreen;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ialdaiaxiariyay.gtms.api.gui.ButtonEntry;
import top.ialdaiaxiariyay.gtms.api.gui.CustomButton;

import java.util.ArrayList;
import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.clearWidgets();

        int buttonWidth = 200;
        int buttonHeight = 20;
        int buttonSpacing = 2;

        // 定义按钮列表（顺序、文本、点击事件）
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

        int totalHeight = buttons.size() * buttonHeight + (buttons.size() - 1) * buttonSpacing;
        int startX = this.width / 2 - buttonWidth / 2;
        int y = this.height / 2 - totalHeight / 2;

        for (ButtonEntry entry : buttons) {
            this.addRenderableWidget(new CustomButton(
                    startX, y, buttonWidth, buttonHeight,
                    Component.translatable(entry.translationKey),
                    entry.onPress));
            y += buttonHeight + buttonSpacing;
        }
    }
}

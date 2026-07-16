package top.ialdaiaxiariyay.gtbss.common.data;

import com.gregtechceu.gtceu.api.sound.SoundEntry;

import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.registrate.GTBSSRegistrate;

public class GTBSSSoundEvent {

    public static void init() {}

    public static final SoundEntry UI_BUTTON_HOVER = GTBSSRegistrate.REGISTRATION
            .sound(GTBSS.id("mouse_hover_1"))
            .build();

    public static final SoundEntry UI_BUTTON_CLICK_1 = GTBSSRegistrate.REGISTRATION
            .sound(GTBSS.id("mouse_click_1"))
            .build();

    public static final SoundEntry MENU_MUSIC = GTBSSRegistrate.REGISTRATION
            .sound(GTBSS.id("menu_music"))
            .build();
}

package top.ialdaiaxiariyay.gtms.common.data;

import com.gregtechceu.gtceu.api.sound.SoundEntry;

import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate;

public class GTMSSoundEvent {

    public static void init() {}

    public static final SoundEntry UI_BUTTON_HOVER = GTMSRegistrate.REGISTRATE
            .sound(GTMS.id("mouse_hover_1"))
            .build();

    public static final SoundEntry UI_BUTTON_CLICK_1 = GTMSRegistrate.REGISTRATE
            .sound(GTMS.id("mouse_click_1"))
            .build();

    public static final SoundEntry UI_BUTTON_CLICK_2 = GTMSRegistrate.REGISTRATE.sound(GTMS.id("mouse_click_2"))
            .build();
}

package top.ialdaiaxiariyay.gtms.data.datagen.lang.initlang;

import top.ialdaiaxiariyay.gtms.data.datagen.lang.UnifiedLanguageProvider;

public class TipsLang {

    public static void init(UnifiedLanguageProvider provider) {
        provider.add("gtms.gui.wireless_steam","Wireless Total Steam Volume","无线蒸汽总量");
        provider.add("gtms.machine.steam.wireless_steam_in_hatch.tooltip","Steam accessible from wireless network)","能从无线网络中获取蒸汽");
        provider.add("gtms.machine.steam.wireless_steam_out_hatch.tooltip","Transmit steam into wireless network","将蒸汽输入到无线网络中");
    }
}

package top.ialdaiaxiariyay.gtbss.data.datagen.lang.initlang;

import com.gregtechceu.gtceu.api.GTValues;

import top.ialdaiaxiariyay.gtbss.data.datagen.lang.UnifiedLanguageProvider;

public class BlockLang {

    public static void init(UnifiedLanguageProvider provider) {
        EnergyHatch(provider);
        DynamoHatch(provider);
        LaserSourceHatch(provider);
        LaserTargetHatch(provider);
        provider.add("block.gtbss.me_dual_output_part", "ME Output Dual Part", "ME输出总成");
        provider.add("block.gtbss.wireless_steam_input_hatch", "Wireless Steam Input Hatch", "无线蒸汽输入仓");
        provider.add("block.gtbss.wireless_steam_output_hatch", "Wireless Steam Output Hatch", "无线蒸汽输出仓");
        provider.add("block.gtbss.wireless_resource_monitor", "Wireless Resource Monitor", "无线资源监视器");
        provider.add("block.gtbss.spun_time_anchor", "Spun Time Anchor", "纺时之仪");
        provider.add("block.gtbss.divergent_dream_rod", "Candle of Alien Dreams", "异梦烛");
        provider.add("block.gtbss.time_sand", "Time Sand", "时之沙");
    }

    public static void EnergyHatch(UnifiedLanguageProvider provider) {
        Object[][] ampereConfigs = {
                { "", "", "" },
                { "_4a", "4A ", "4安" },
                { "_16a", "16A ", "16安" }
        };
        int count = Math.min(GTValues.VN.length, GTValues.VNF.length);
        for (int i = 0; i < count; i++) {
            String vn = GTValues.VN[i];
            String vnf = GTValues.VNF[i];
            for (Object[] cfg : ampereConfigs) {
                String keySuffix = (String) cfg[0];
                String ampereEn = (String) cfg[1];
                String ampereZh = (String) cfg[2];
                String key = "block.gtbss." + vn.toLowerCase() + "_wireless_energy_input_hatch" + keySuffix;
                String english = vnf + " " + ampereEn + "Wireless Energy Hatch";
                String chinese = ampereZh + vnf + "§r无线能源仓";
                provider.add(key, english, chinese);
            }
        }
    }

    public static void DynamoHatch(UnifiedLanguageProvider provider) {
        Object[][] ampereConfigs = {
                { "", "", "" },
                { "_4a", "4A ", "4安" },
                { "_16a", "16A ", "16安" }
        };
        int count = Math.min(GTValues.VN.length, GTValues.VNF.length);
        for (int i = 0; i < count; i++) {
            String vn = GTValues.VN[i];
            String vnf = GTValues.VNF[i];
            for (Object[] cfg : ampereConfigs) {
                String keySuffix = (String) cfg[0];
                String ampereEn = (String) cfg[1];
                String ampereZh = (String) cfg[2];
                String key = "block.gtbss." + vn.toLowerCase() + "_wireless_energy_output_hatch" + keySuffix;
                String english = vnf + " " + ampereEn + "Wireless Dynamo Hatch";
                String chinese = ampereZh + vnf + "§r无线动力仓";
                provider.add(key, english, chinese);
            }
        }
    }

    public static void LaserSourceHatch(UnifiedLanguageProvider provider) {
        int[] amperages = { 256, 1024, 4096 };
        int count = Math.min(GTValues.VN.length, GTValues.VNF.length);
        for (int i = 0; i < count; i++) {
            String vnLower = GTValues.VN[i].toLowerCase();
            String vnf = GTValues.VNF[i];
            for (int amperage : amperages) {
                String key = "block.gtbss." + vnLower + "_" + amperage + "a_wireless_laser_source_hatch";
                String formattedAmpere = String.format("%,d", amperage);
                String english = vnf + " " + formattedAmpere + "§eA§r Wireless Laser Source Hatch";
                String chinese = amperage + "§e安§r" + vnf + "无线激光源仓";
                provider.add(key, english, chinese);
            }
        }
    }

    public static void LaserTargetHatch(UnifiedLanguageProvider provider) {
        int[] amperages = { 256, 1024, 4096 };
        int count = Math.min(GTValues.VN.length, GTValues.VNF.length);
        for (int i = 0; i < count; i++) {
            String vnLower = GTValues.VN[i].toLowerCase();
            String vnf = GTValues.VNF[i];
            for (int amperage : amperages) {
                String key = "block.gtbss." + vnLower + "_" + amperage + "a_wireless_laser_target_hatch";
                String formattedAmpere = String.format("%,d", amperage);
                String english = vnf + " " + formattedAmpere + "§eA§r Wireless Laser Target Hatch";
                String chinese = amperage + "§e安§r" + vnf + "无线激光靶仓";
                provider.add(key, english, chinese);
            }
        }
    }
}

package top.ialdaiaxiariyay.gtms.data.datagen.lang.initlang;

import com.gregtechceu.gtceu.api.GTValues;

import top.ialdaiaxiariyay.gtms.data.datagen.lang.UnifiedLanguageProvider;

public class BlockLang {

    public static void init(UnifiedLanguageProvider provider) {
        EnergyHatch(provider);
        DynamoHatch(provider);
        provider.add("block.gtms.wireless_steam_input_hatch", "Wireless Steam Input Hatch", "无线蒸汽输入仓");
        provider.add("block.gtms.wireless_steam_output_hatch", "Wireless Steam Output Hatch", "无线蒸汽输出仓");
        provider.add("block.gtms.wireless_resource_monitor", "Wireless Resource Monitor", "无线资源监视器");
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
                String key = "block.gtms." + vn.toLowerCase() + "_wireless_energy_input_hatch" + keySuffix;
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
                String key = "block.gtms." + vn.toLowerCase() + "_wireless_energy_output_hatch" + keySuffix;
                String english = vnf + " " + ampereEn + "Wireless Dynamo Hatch";
                String chinese = ampereZh + vnf + "§r无线动力仓";
                provider.add(key, english, chinese);
            }
        }
    }
}

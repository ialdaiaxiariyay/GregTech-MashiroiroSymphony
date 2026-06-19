package top.ialdaiaxiariyay.gtms.data.datagen.lang.initlang;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.api.GTMSValues;
import top.ialdaiaxiariyay.gtms.data.datagen.lang.UnifiedLanguageProvider;

public class Lang {

    public static void init(@NotNull UnifiedLanguageProvider provider) {
        registerManaTiers(provider);
        registerOreVein(provider);
        provider.add("itemGroup.gtms.block", "Mashiroiro Symphony | Blocks", "纯白交响曲 | 方块");
        provider.add("itemGroup.gtms.machine", "Mashiroiro Symphony | Machine", "纯白交响曲 | 机器");
        provider.add("gui.gtms.sanity", "Sanity: %d", "理智: %d");
        provider.add("config.jade.plugin_gtms.mana_container", "[Mashiroiro Symphony] Mana Container",
                "[Mashiroiro Symphony] 魔力槽");
        provider.add("recipe.capabilitg.mana.name", "Mana", "魔力");
        provider.add("gtms.jade.mana_stored", "%d / %d Mana", "%d / %d Mana");
        provider.add("gtms.recipe.mana_per_tick", "Mana: %s mana/t", "魔力: %s 魔力/t");
        provider.add("gtms.recipe.total_mana", "Total Mana: %s mana", "总魔力: %s 魔力");
        provider.add("gtms.jade.mana_consumption", "Mana Consumption: %s", "魔力消耗：%s /t");
        provider.add("gtms.jade.mana_production", "Mana Production: %s", "魔力产出：%s /t");
        provider.add("gtms.jade.mana_unit", "Total consumption: %s", "总消耗: %s");
        provider.add("gtms.recipe.mana_tier", "Mana Tier: %s", "魔力等级: %s");
    }

    private static void registerOreVein(@NotNull UnifiedLanguageProvider provider) {
        provider.add("gtceu.jei.ore_vein.time_sand_vein_deepslate","Time Sand Ore Vein","时之沙矿脉");
    }

    private static void registerManaTiers(@NotNull UnifiedLanguageProvider provider) {
        for (int i = 0; i < GTMSValues.MN.length; i++) {
            String key = "gtms.lang.mana_tier." + GTMSValues.MN[i].toLowerCase();
            provider.add(key, GTMSValues.MNF_EN[i], GTMSValues.MNF_CN[i]);
        }
    }
}

package top.ialdaiaxiariyay.gtbss.data.datagen.lang.initlang;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.api.GTBSSValues;
import top.ialdaiaxiariyay.gtbss.data.datagen.lang.UnifiedLanguageProvider;

public class Lang {

    public static void init(@NotNull UnifiedLanguageProvider provider) {
        registerManaTiers(provider);
        registerOreVein(provider);
        provider.add("itemGroup.gtbss.block", "Blue Symphony | Block", "蓝天交响曲 | 方块");
        provider.add("itemGroup.gtbss.machine", "Blue Symphony | Machine", "蓝天交响曲 | 机器");
        provider.add("itemGroup.gtbss.item", "Blue Symphony | Item", "蓝天交响曲 | 物品");
        provider.add("gui.gtbss.sanity", "Sanity: %d", "理智: %d");
        provider.add("config.jade.plugin_gtbss.mana_container", "[Blue Symphony] Mana Container",
                "[Blue Symphony] 魔力槽");
        provider.add("recipe.capability.mana.name", "Mana", "魔力");
        provider.add("gtbss.jade.mana_stored", "%d / %d Mana", "%d / %d Mana");
        provider.add("gtbss.recipe.mana_per_tick", "Mana: %s mana/t", "魔力: %s 魔力/t");
        provider.add("gtbss.recipe.total_mana", "Total Mana: %s mana", "总魔力: %s 魔力");
        provider.add("gtbss.jade.mana_consumption", "Mana Consumption: %s", "魔力消耗：%s /t");
        provider.add("gtbss.jade.mana_production", "Mana Production: %s", "魔力产出：%s /t");
        provider.add("gtbss.jade.mana_unit", "Total consumption: %s", "总消耗: %s");
        provider.add("gtbss.recipe.mana_tier", "Mana Tier: %s", "魔力等级: %s");
    }

    private static void registerOreVein(@NotNull UnifiedLanguageProvider provider) {
        provider.add("gtceu.jei.ore_vein.time_sand_vein_deepslate", "Time Sand Ore Vein", "时之沙矿脉");
    }

    private static void registerManaTiers(@NotNull UnifiedLanguageProvider provider) {
        for (int i = 0; i < GTBSSValues.MN.length; i++) {
            String key = "gtbss.lang.mana_tier." + GTBSSValues.MN[i].toLowerCase();
            provider.add(key, GTBSSValues.MNF_EN[i], GTBSSValues.MNF_CN[i]);
        }
    }
}

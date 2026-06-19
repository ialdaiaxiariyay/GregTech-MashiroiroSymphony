package top.ialdaiaxiariyay.gtms.data.datagen.lang.initlang;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.data.datagen.lang.UnifiedLanguageProvider;

public class TipsLang {

    public static void init(@NotNull UnifiedLanguageProvider provider) {
        provider.add("gtms.machine.steam.wireless_steam_in_hatch.tooltip", "Steam accessible from wireless network",
                "能从无线网络中获取蒸汽");
        provider.add("gtms.machine.steam.wireless_steam_out_hatch.tooltip", "Transmit steam into wireless network",
                "将蒸汽输入到无线网络中");
        provider.add("gtms.machine.wireless_resource_monitor.tooltip", "Owner: %s", "所有者：%s");
        provider.add("gtms.gui.wireless_steam", "Wireless Total Steam Volume", "无线蒸汽总量");
        provider.add("gtms.machine.wireless_resource_monitor.title", "%s Monitor - Team: %s", "%s 监视器 - 团队: %s");
        provider.add("gtms.machine.wireless_resource_monitor.no_team", "No team found", "未找到团队");
        provider.add("gtms.machine.wireless_resource_monitor.no_data_for_type", "No data available for resource: %s",
                "没有资源 %s 的数据");
        provider.add("gtms.machine.wireless_resource_monitor.storage", "Storage: %s", "储量: %s");
        provider.add("gtms.machine.wireless_resource_monitor.net_rate", "Net rate: %s/t", "净速率: %s/t");
        provider.add("gtms.machine.wireless_resource_monitor.last_minute", "Last minute: %s/t", "最近1分钟: %s/t");
        provider.add("gtms.machine.wireless_resource_monitor.last_hour", "Last hour: %s/t", "最近1小时: %s/t");
        provider.add("gtms.machine.wireless_resource_monitor.last_day", "Last day: %s/t", "最近24小时: %s/t");
        provider.add("gtms.machine.wireless_resource_monitor.show", "Show: ", "显示: ");
        provider.add("gtms.machine.wireless_resource_monitor.all", "All Teams", "全部团队");
        provider.add("gtms.machine.wireless_resource_monitor.team_only", "Only Our Team", "仅本队");
        provider.add("gtms.machine.wireless_resource_monitor.recent_transfers", "Recent transfers:", "最近传输记录:");
        provider.add("gtms.machine.wireless_resource_monitor.tooltip.summary",
                "Monitors wireless resources (EU, Steam, Mana) stored and transferred within your team.",
                "监视团队内无线网络中的资源存储与传输（能量、蒸汽、魔力）。");
        provider.add("gtms.machine.wireless_resource_monitor.tooltip.usage",
                "Click the resource type name (EU/STEAM/MANA) to cycle through different resource types.",
                "点击资源类型名称（EU/STEAM/MANA）可在不同资源类型间循环切换。");
        provider.add("gtms.machine.wireless_resource_monitor.teleport_success", "Teleported to %s, %s, %s in %s",
                "传送至 %s, %s, %s 于 %s");
        provider.add("gtms.machine.spun_time_anchor.tips.0", "Anchoring Dreams and Reality", "锚定梦境与现实");
        provider.add("gtms.recipe_modifier.insufficient_mana", "Insufficient Mana", "魔力不足");
        provider.add("gtms.universal.tooltip.mana_in", "§aMax Mana IN: §f%d (%s§f)", "§a最大输入魔力：§f%d（%s§f）");
        provider.add("gtms.universal.tooltip.mana_out", "§aMax Mana OUT: §f%d (%s§f)", "§a最大输出魔力：§f%d（%s§f）");
        provider.add("gtms.universal.tooltip.mana_storage_capacity", "§cMana Capacity: §r%d Mana", "§c魔力缓存：§r%d Mana");
    }
}

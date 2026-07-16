package top.ialdaiaxiariyay.gtbss.data.datagen.lang.initlang;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.data.datagen.lang.UnifiedLanguageProvider;

public class TipsLang {

    public static void init(@NotNull UnifiedLanguageProvider provider) {
        provider.add("gtbss.machine.steam.wireless_steam_in_hatch.tooltip", "Steam accessible from wireless network",
                "能从无线网络中获取蒸汽");
        provider.add("gtbss.machine.steam.wireless_steam_out_hatch.tooltip", "Transmit steam into wireless network",
                "将蒸汽输入到无线网络中");
        provider.add("gtbss.machine.wireless_resource_monitor.tooltip", "Owner: %s", "所有者：%s");
        provider.add("gtbss.gui.wireless_steam", "Wireless Total Steam Volume: %s", "无线蒸汽总量: %s");
        provider.add("gtbss.machine.wireless_resource_monitor.transfers", "No recent transfers", "无传输记录");
        provider.add("gtbss.machine.wireless_resource_monitor.teleport", "Click to teleport", "点击传送");
        provider.add("gtbss.machine.wireless_resource_monitor.switch", "Switch: ", "切换: ");
        provider.add("gtbss.machine.wireless_resource_monitor.title", "%s Monitor - Team: %s", "%s 监视器 - 团队: %s");
        provider.add("gtbss.machine.wireless_resource_monitor.no_team", "No team found", "未找到团队");
        provider.add("gtbss.machine.wireless_resource_monitor.no_data_for_type", "No data available for resource: %s",
                "没有资源 %s 的数据");
        provider.add("gtbss.machine.wireless_resource_monitor.storage", "Storage: %s", "储量: %s");
        provider.add("gtbss.machine.wireless_resource_monitor.net_rate", "Net rate: %s/t", "净速率: %s/t");
        provider.add("gtbss.machine.wireless_resource_monitor.last_minute", "Last minute: %s/t", "最近1分钟: %s/t");
        provider.add("gtbss.machine.wireless_resource_monitor.last_hour", "Last hour: %s/t", "最近1小时: %s/t");
        provider.add("gtbss.machine.wireless_resource_monitor.last_day", "Last day: %s/t", "最近24小时: %s/t");
        provider.add("gtbss.machine.wireless_resource_monitor.show", "Show: ", "显示: ");
        provider.add("gtbss.machine.wireless_resource_monitor.all", "All Teams", "全部团队");
        provider.add("gtbss.machine.wireless_resource_monitor.team_only", "Only Our Team", "仅本队");
        provider.add("gtbss.machine.wireless_resource_monitor.recent_transfers", "Recent transfers:", "最近传输记录:");
        provider.add("gtbss.machine.wireless_resource_monitor.tooltip.summary",
                "Monitors wireless resources (EU, Steam, Mana) stored and transferred within your team.",
                "监视团队内无线网络中的资源存储与传输（能量、蒸汽、魔力）。");
        provider.add("gtbss.machine.wireless_resource_monitor.tooltip.usage",
                "Click the resource type name (EU/STEAM/MANA) to cycle through different resource types.",
                "点击资源类型名称（EU/STEAM/MANA）可在不同资源类型间循环切换。");
        provider.add("gtbss.machine.wireless_resource_monitor.teleport_success", "Teleported to %s, %s, %s in %s",
                "传送至 %s, %s, %s 于 %s");
        provider.add("gtbss.machine.spun_time_anchor.tips.0", "Anchoring Dreams and Reality", "锚定梦境与现实");
        provider.add("gtbss.recipe_modifier.insufficient_mana", "Insufficient Mana", "魔力不足");
        provider.add("gtbss.universal.tooltip.mana_in", "§aMax Mana IN: §f%d (%s§f)", "§a最大输入魔力：§f%d（%s§f）");
        provider.add("gtbss.universal.tooltip.mana_out", "§aMax Mana OUT: §f%d (%s§f)", "§a最大输出魔力：§f%d（%s§f）");
        provider.add("gtbss.universal.tooltip.mana_storage_capacity", "§cMana Capacity: §r%d Mana", "§c魔力缓存：§r%d Mana");
        provider.add("gtbss.tooltip.wand.combo", "Active Combination: %s", "已激活组合");
        provider.add("gtbss.tooltip.wand.no_modules", "No modules installed", "未安装模块");
        provider.add("gtbss.tooltip.wand.installed", "Installed:", "已安装:");
        provider.add("gtbss.tooltip.wand.max_slots", "Max %d modules", "最多 %d 个模块");
        provider.add("gtbss.tooltip.wand.controls", "Shift+Right-click to remove, offhand to install",
                "潜行+右键拆卸，副手物品安装");
        provider.add("gtbss.tooltip.wand.combination", "Combination is empty", "组合为空");
        provider.add("gtbss.tooltip.wand.charge_time", "Charge time: %s", "蓄力时间: %s");
        provider.add("gtbss.tooltip.magic_module.manacost", "Mana cost: %s", "魔力消耗: %s");
        provider.add("gtbss.tooltip.wand.total_mana_cost", "Total mana cost: %s", "总魔力消耗: %s");
        provider.add("gtbss.tooltip.magic_module.charge_time_modifier", "Charge Time Modifier: %s", "蓄力时间修正: %s");
        provider.add("gtbss.tooltip.magic_module.skill", "Module skill:", "模块技能:");
        provider.add("gtbss.tooltip.spring_module.0",
                "Blossom of Life:\nWithin a radius of 4 + charge time * 4 blocks around the player:\nRandomly advances 3–6 plants (crops/saplings/grass) to their next stage;\nIf no plants are present, restores 2 + charge time * 2 health to the caster.",
                "生命绽放：\n以玩家为中心，半径 4 + 蓄力时间×4 格内：\n随机催熟 3~6 个植物（作物/树苗/草）至下一阶段；\n若无植物，则恢复自身 2 + 蓄力时间×2 生命值。");
        provider.add("gtbss.tooltip.earth_module.0",
                "Earth Pulse (Shared Damage – evenly split):\nDeals a total of 12 + charge time * 10 magic damage to all enemies within a radius of 5 + charge time * 3 blocks, split equally among all targets (more enemies → less damage per target).\nVisual: generates rocky particles around the area (no actual block placement).",
                "地脉震荡（均摊伤害）：\n对半径 5 + 蓄力时间×3 格内所有敌人造成总计 12 + 蓄力时间×10 魔法伤害，伤害在所有目标间均分（敌人越多单体伤害越低）。\n特效：周围产生大量岩石粒子（不实际放置方块）。");
        provider.add("gtbss.tooltip.winter_module.0",
                "Ice Imprisonment (Independent Damage):\nAll creatures within a radius of 4 + charge time * 3 blocks receive Slowness III for 3 + charge time * 4 seconds,\nand each target independently takes 1 + charge time * 2 frost damage (not split).",
                "寒冰禁锢（独立伤害）：\n半径 4 + 蓄力时间×3 格内所有生物获得缓慢 III，持续 3 + 蓄力时间×4 秒，\n并且每个目标独立受到 1 + 蓄力时间×2 冰冻伤害（伤害不均分）。");
        provider.add("gtbss.tooltip.wind_module.0",
                "Wind Chain (Bouncing Damage):\nFires a wind projectile that bounces up to 3 + charge time * 3 times, dealing 5 + charge time * 7 damage per hit,\nwith damage decaying by (20% - 10%*charge time) each bounce. The first target is knocked back with force 0.5 + charge time.\nEach bounce seeks the nearest un-hit enemy within range.",
                "风之连锁（弹射伤害）：\n发射一枚风弹，最多弹射 3 + 蓄力时间×3 次，每次造成 5 + 蓄力时间×7 伤害，\n每次弹射伤害衰减 20% - 10%×蓄力时间。首个目标被击退，强度 0.5 + 蓄力时间。\n每次弹射自动寻找范围内最近的未受伤敌人。");
        provider.add("gtbss.tooltip.fire_module.0",
                "Flame Burst (Independent Area Damage):\nLaunches a fireball (speed and explosion radius scale with charge time), which explodes on impact in a 2 + charge time * 2 block radius.\nEvery enemy within the explosion takes 4 + charge time * 4 fire damage individually (damage is NOT shared).\nAlso sets fire to nearby blocks for a short duration.",
                "烈焰冲击（独立范围伤害）：\n发射一枚火球（速度与爆炸范围受蓄力时间影响），命中后产生 2 + 蓄力时间×2 格爆炸。\n爆炸范围内每个敌人独立受到 4 + 蓄力时间×4 火焰伤害（伤害不均分）。\n同时点燃附近方块一段时间。");
        provider.add("gtbss.tooltip.mana_module.0",
                "Mana Burst (Shared Damage):\nDeals a total of 12 + charge time * 18 magic damage to all enemies within a radius of 8 blocks, split equally among all targets.\nAdditionally, grants the caster an Absorption shield equal to 30% of the total damage dealt, lasting 10 seconds.",
                "魔力爆发（均摊伤害）：\n对半径 8 格内所有敌人造成总计 12 + 蓄力时间×18 魔法伤害，伤害在所有目标间均分。\n同时为施法者提供相当于总伤害 30% 的伤害吸收护盾，持续 10 秒。");
        provider.add("gtbss.tooltip.autumn_module.0",
                "Harvest of Life (Shared Damage):\nDeals a total of 10 + charge time * 20 magic damage to all enemies within a radius of 8 blocks, split equally among all targets.\nHeals the caster for 50% of the total damage dealt (before splitting).",
                "生命收割（均摊伤害）：\n对半径 8 格内所有敌人造成总计 10 + 蓄力时间×20 魔法伤害，伤害在所有目标间均分。\n恢复施法者总伤害（均分前）50% 的生命值。");
        provider.add("gtbss.tooltip.water_module.0",
                "Healing Spring (Shared Healing):\nCreates a water spring with a diameter of 3 + charge time * 2 blocks beneath the player, lasting 4 + charge time * 3 seconds,\nhealing a total of 2 + charge time * 2 health per second, split evenly among all friendly units (including the player) within the spring.",
                "治愈之泉（均摊治疗）：\n在玩家脚下生成 3 + 蓄力时间×2 格直径的水泉，持续 4 + 蓄力时间×3 秒，\n每秒总治疗量为 2 + 蓄力时间×2，均分给泉水中所有友方单位（含玩家）。");
        provider.add("gtbss.tooltip.summer_module.0",
                "Scorching Heat (Shared Damage):\nDeals a total of 8 + charge time * 12 magic damage to all enemies within a radius of 6 blocks, split equally among all targets.\nAlso applies Slowness II and Weakness I to each enemy for 5 + charge time * 5 seconds.",
                "灼热高温（均摊伤害）：\n对半径 6 格内所有敌人造成总计 8 + 蓄力时间×12 魔法伤害，伤害在所有目标间均分。\n同时对每个敌人施加缓慢 II 和虚弱 I，持续 5 + 蓄力时间×5 秒。");
        provider.add("gtbss.tooltip.wand.combo.tooltip", "Combo skill:", "组合技能:");
        provider.add("gtbss.machine.me_dual_output_part.tooltip.0","Unified Output for Multiblocks: Supports direct transfer of both items and fluids into an ME network without intermediate buffers or pipes.","多方块统一输出：支持物品和流体直接输入ME网络，无需中间缓存或管道。");
        provider.add("gtbss.machine.me_dual_output_part.tooltip.1","Dual Infinite Buffers: While disconnected from the ME network, the internal item buffer and fluid buffer each have unlimited capacity, allowing arbitrary amounts of both item and fluid types to be stored until the network reconnects.","双无限缓存：在未连接ME网络时，内部的物品缓存和流体缓存各自拥有无限容量，可任意存储两种类型的资源，直到网络恢复。");
    }
}

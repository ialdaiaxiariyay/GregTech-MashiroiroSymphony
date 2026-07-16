package top.ialdaiaxiariyay.gtbss.common.data;

import net.minecraft.network.chat.Component;

import com.tterrag.registrate.util.entry.ItemEntry;
import top.ialdaiaxiariyay.gtbss.api.registrate.GTBSSRegistrate;
import top.ialdaiaxiariyay.gtbss.common.item.MagicModularWandItem;
import top.ialdaiaxiariyay.gtbss.common.item.module.*;

import static top.ialdaiaxiariyay.gtbss.utils.GTBSSItemUtils.*;

public class GTBSSItems {

    public static void init() {}

    static {
        GTBSSRegistrate.REGISTRATION.creativeModeTab(() -> GTBSSCreativeModeTab.ITEM);
    }

    public static final ItemEntry<MagicModularWandItem> BASICS_WAND = magicModularWand(
            properties -> new MagicModularWandItem(properties, 3, 100000), "basics_wand");

    public static final ItemEntry<SpringModule> SPRING_MODULE = magicModular(SpringModule::new, "spring_module",
            Component.translatable("gtbss.tooltip.spring_module.0"));
    public static final ItemEntry<EarthModule> EARTH_MODULE = magicModular(EarthModule::new, "earth_module",
            Component.translatable("gtbss.tooltip.earth_module.0"));
    public static final ItemEntry<WinterModule> WINTER_MODULE = magicModular(WinterModule::new, "winter_module",
            Component.translatable("gtbss.tooltip.winter_module.0"));
    public static final ItemEntry<WindModule> WIND_MODULE = magicModular(WindModule::new, "wind_module",
            Component.translatable("gtbss.tooltip.wind_module.0"));
    public static final ItemEntry<FireModule> FIRE_MODULE = magicModular(FireModule::new, "fire_module",
            Component.translatable("gtbss.tooltip.fire_module.0"));
    public static final ItemEntry<ManaModule> MANA_MODULE = magicModular(ManaModule::new, "mana_module",
            Component.translatable("gtbss.tooltip.mana_module.0"));
    public static final ItemEntry<AutumnModule> AUTUMN_MODULE = magicModular(AutumnModule::new, "autumn_module",
            Component.translatable("gtbss.tooltip.autumn_module.0"));
    public static final ItemEntry<WaterModule> WATER_MODULE = magicModular(WaterModule::new, "water_module",
            Component.translatable("gtbss.tooltip.water_module.0"));
    public static final ItemEntry<SummerModule> SUMMER_MODULE = magicModular(SummerModule::new, "summer_module",
            Component.translatable("gtbss.tooltip.summer_module.0"));
}

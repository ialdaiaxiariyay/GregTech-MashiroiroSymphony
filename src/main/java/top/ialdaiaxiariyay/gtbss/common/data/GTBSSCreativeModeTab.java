package top.ialdaiaxiariyay.gtbss.common.data;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import net.minecraft.world.item.CreativeModeTab;

import com.tterrag.registrate.util.entry.RegistryEntry;
import top.ialdaiaxiariyay.gtbss.GTBSS;

import static top.ialdaiaxiariyay.gtbss.api.registrate.GTBSSRegistrate.REGISTRATION;

public class GTBSSCreativeModeTab {

    public static void init() {}

    public static RegistryEntry<CreativeModeTab> MACHINE = REGISTRATION.defaultCreativeTab("machine",
            builder -> builder
                    .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("machine", REGISTRATION))
                    .icon(GTBSSMachines.WIRELESS_STEAM_INPUT_HATCH::asStack)
                    .title(REGISTRATION.addLang("itemGroup", GTBSS.id("machine"),
                            GTBSS.NAME + " Machine"))
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> BLOCK = REGISTRATION.defaultCreativeTab("block",
            builder -> builder
                    .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("block", REGISTRATION))
                    .icon(GTBSSBlocks.DIVERGENT_DREAM_ROD::asStack)
                    .title(REGISTRATION.addLang("itemGroup", GTBSS.id("block"),
                            GTBSS.NAME + " Blocks"))
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> ITEM = REGISTRATION.defaultCreativeTab("item",
            builder -> builder
                    .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("item", REGISTRATION))
                    .icon(GTBSSItems.BASICS_WAND::asStack)
                    .title(REGISTRATION.addLang("itemGroup", GTBSS.id("item"),
                            GTBSS.NAME + " Items"))
                    .build())
            .register();
}

package top.ialdaiaxiariyay.gtms.common.data;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import net.minecraft.world.item.CreativeModeTab;

import com.tterrag.registrate.util.entry.RegistryEntry;
import top.ialdaiaxiariyay.gtms.GTMS;

import static top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate.REGISTRATE;

public class GTMSCreativeModeTab {

    public static void init() {}

    public static RegistryEntry<CreativeModeTab> MACHINE = REGISTRATE.defaultCreativeTab("machine",
            builder -> builder
                    .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("machine", REGISTRATE))
                    .icon(GTMSMachines.WIRELESS_STEAM_INPUT_HATCH::asStack)
                    .title(REGISTRATE.addLang("itemGroup", GTMS.id("machine"),
                            GTMS.NAME + " Machine"))
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> BLOCK = REGISTRATE.defaultCreativeTab("block",
            builder -> builder
                    .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("block", REGISTRATE))
                    .icon(GTMSBlocks.DIVERGENT_DREAM_ROD::asStack)
                    .title(REGISTRATE.addLang("itemGroup", GTMS.id("block"),
                            GTMS.NAME + " Blocks"))
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> ITEM = REGISTRATE.defaultCreativeTab("item",
                    builder -> builder
                            .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("item", REGISTRATE))
                            .icon(GTMSBlocks.DIVERGENT_DREAM_ROD::asStack)
                            .title(REGISTRATE.addLang("itemGroup", GTMS.id("item"),
                                    GTMS.NAME + " Items"))
                            .build())
            .register();
}

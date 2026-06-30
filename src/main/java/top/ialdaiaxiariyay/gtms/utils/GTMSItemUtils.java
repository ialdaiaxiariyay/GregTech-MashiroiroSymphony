package top.ialdaiaxiariyay.gtms.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.item.MagicModuleItem;
import top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate;
import top.ialdaiaxiariyay.gtms.common.item.MagicModularWandItem;

import java.util.Arrays;

public class GTMSItemUtils {

    public static <T extends MagicModuleItem> ItemEntry<T> magicModular(NonNullFunction<Item.Properties, T> factory,
                                                                        String name, Component... components) {
        return GTMSRegistrate.REGISTRATE
                .item(name, factory)
                .defaultModel()
                .properties(properties -> properties.stacksTo(1))
                .onRegister(t -> t.setTooltips(Arrays.asList(components)))
                .register();
    }

    public static <
            T extends MagicModularWandItem> @NotNull ItemEntry<T> magicModularWand(NonNullFunction<Item.Properties, T> factory,
                                                                                   String name) {
        return GTMSRegistrate.REGISTRATE
                .item(name, factory)
                .model((ctx, prov) -> {
                    prov.getBuilder(ctx.getName())
                            .parent(prov.getExistingFile(GTMS.id("item/wand")))
                            .texture("layer0", prov.modLoc("item/" + ctx.getName()));
                })
                .register();
    }
}

package top.ialdaiaxiariyay.gtbss.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.item.MagicModuleItem;
import top.ialdaiaxiariyay.gtbss.api.registrate.GTBSSRegistrate;
import top.ialdaiaxiariyay.gtbss.common.item.MagicModularWandItem;

import java.util.Arrays;

public class GTBSSItemUtils {

    public static <T extends MagicModuleItem> ItemEntry<T> magicModular(NonNullFunction<Item.Properties, T> factory,
                                                                        String name, Component... components) {
        return GTBSSRegistrate.REGISTRATION
                .item(name, factory)
                .defaultModel()
                .properties(properties -> properties.stacksTo(1))
                .onRegister(t -> t.setTooltips(Arrays.asList(components)))
                .register();
    }

    public static <
            T extends MagicModularWandItem> @NotNull ItemEntry<T> magicModularWand(NonNullFunction<Item.Properties, T> factory,
                                                                                   String name) {
        return GTBSSRegistrate.REGISTRATION
                .item(name, factory)
                .model((ctx, prov) -> {
                    prov.getBuilder(ctx.getName())
                            .parent(prov.getExistingFile(GTBSS.id("item/wand")))
                            .texture("layer0", prov.modLoc("item/" + ctx.getName()));
                })
                .register();
    }
}

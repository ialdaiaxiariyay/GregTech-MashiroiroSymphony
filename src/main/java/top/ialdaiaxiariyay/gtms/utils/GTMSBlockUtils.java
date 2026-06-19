package top.ialdaiaxiariyay.gtms.utils;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.tterrag.registrate.util.entry.BlockEntry;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate;

import java.util.function.Function;

public class GTMSBlockUtils {

    public static <T extends Block> @NotNull BlockEntry<T> createNonModelsBlock(String name,
                                                                                @NotNull Function<BlockBehaviour.Properties, T> factory) {
        return GTMSRegistrate.REGISTRATE.block(name, factory::apply)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().getExistingFile(
                        GTMS.id("block/" + ctx.getName()))))
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .item(BlockItem::new)
                .build()
                .register();
    }

    public static <T extends Block> @NotNull BlockEntry<T> createBlock(String name,
                                                                       @NotNull Function<BlockBehaviour.Properties, T> factory) {
        return GTMSRegistrate.REGISTRATE.block(name, factory::apply)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().cubeAll(ctx.getName(), GTMS.id("block/" + ctx.getName()))))
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .item(BlockItem::new)
                .build()
                .register();
    }
}

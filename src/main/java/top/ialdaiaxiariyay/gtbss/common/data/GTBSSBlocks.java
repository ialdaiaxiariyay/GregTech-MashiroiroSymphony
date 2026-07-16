package top.ialdaiaxiariyay.gtbss.common.data;

import com.gregtechceu.gtceu.api.data.tag.TagUtil;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SandBlock;

import com.tterrag.registrate.util.entry.BlockEntry;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.registrate.GTBSSRegistrate;
import top.ialdaiaxiariyay.gtbss.common.block.DivergentDreamRodBlock;

import static top.ialdaiaxiariyay.gtbss.utils.GTBSSBlockUtils.*;

public class GTBSSBlocks {

    public static void init() {}

    static {
        GTBSSRegistrate.REGISTRATION.creativeModeTab(() -> GTBSSCreativeModeTab.BLOCK);
    }

    public static final BlockEntry<DivergentDreamRodBlock> DIVERGENT_DREAM_ROD = createNonModelsBlock(
            "divergent_dream_rod",
            props -> new DivergentDreamRodBlock(props, GTBSS.id("divergent_dream_rod")));

    public static final BlockEntry<SandBlock> TIME_SAND = GTBSSRegistrate.REGISTRATION
            .block("time_sand", properties -> new SandBlock(0x4A90D9, properties))
            .initialProperties(() -> Blocks.SAND)
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                    prov.models().cubeAll(ctx.getName(), GTBSS.id("block/" + ctx.getName()))))
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .tag(TagUtil.createBlockTag("ores"))
            .item(BlockItem::new)
            .build()
            .register();
}

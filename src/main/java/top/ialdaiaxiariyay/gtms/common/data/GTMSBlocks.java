package top.ialdaiaxiariyay.gtms.common.data;

import com.gregtechceu.gtceu.api.data.tag.TagUtil;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SandBlock;

import com.tterrag.registrate.util.entry.BlockEntry;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate;
import top.ialdaiaxiariyay.gtms.common.block.DivergentDreamRodBlock;

import static top.ialdaiaxiariyay.gtms.utils.GTMSBlockUtils.*;

public class GTMSBlocks {

    public static void init() {}

    static {
        GTMSRegistrate.REGISTRATE.creativeModeTab(() -> GTMSCreativeModeTab.BLOCK);
    }

    public static final BlockEntry<DivergentDreamRodBlock> DIVERGENT_DREAM_ROD = createNonModelsBlock(
            "divergent_dream_rod",
            props -> new DivergentDreamRodBlock(props, GTMS.id("divergent_dream_rod")));

    public static final BlockEntry<SandBlock> TIME_SAND = GTMSRegistrate.REGISTRATE
            .block("time_sand", properties -> new SandBlock(0x4A90D9, properties))
            .initialProperties(() -> Blocks.SAND)
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                    prov.models().cubeAll(ctx.getName(), GTMS.id("block/" + ctx.getName()))))
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .tag(TagUtil.createBlockTag("ores"))
            .item(BlockItem::new)
            .build()
            .register();
}

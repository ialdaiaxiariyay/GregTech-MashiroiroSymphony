package top.ialdaiaxiariyay.gtms.common.data;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import top.ialdaiaxiariyay.gtms.GTMS;

import java.util.Set;

public class GTMSWorldGenLayers {

    public static void init(){}

    public static final TagMatchTest DEEPSLATE_RULE = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

    public static final IWorldGenLayer DARKROOM_DEEPSLATE = new SimpleWorldGenLayer(
            GTMS.id("darkroom_deepslate"),
            () -> new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
            Set.of(GTMS.id(GTMSDimension.TheDarkroom)));
}

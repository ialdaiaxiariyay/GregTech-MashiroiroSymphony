package top.ialdaiaxiariyay.gtbss.common.data;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import top.ialdaiaxiariyay.gtbss.GTBSS;

import java.util.Set;

public class GTBSSWorldGenLayers {

    public static void init() {}

    public static final TagMatchTest DEEPSLATE_RULE = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

    public static final IWorldGenLayer DARKROOM_DEEPSLATE = new SimpleWorldGenLayer(
            GTBSS.id("darkroom_deepslate"),
            () -> new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
            Set.of(GTBSS.id(GTBSSDimension.TheDarkroom)));
}

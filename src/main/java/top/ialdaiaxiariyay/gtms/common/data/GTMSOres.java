package top.ialdaiaxiariyay.gtms.common.data;

import com.gregtechceu.gtceu.api.data.worldgen.*;
import com.gregtechceu.gtceu.common.data.GTOres;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import top.ialdaiaxiariyay.gtms.GTMS;

import java.util.Set;

@MethodsReturnNonnullByDefault
public class GTMSOres {

    public static final TagMatchTest DEEPSLATE_RULE = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

    public static final IWorldGenLayer DARKROOM_DEEPSLATE = new SimpleWorldGenLayer(
            "darkroom_deepslate",
            () -> new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
            Set.of(GTMS.id(GTMSDimension.TheDarkroom)));

    public static final GTOreDefinition TIME_SAND_VEIN_DEEPSLATE = create("time_sand_vein_deepslate", vein -> vein
            .clusterSize(UniformInt.of(8, 16))
            .density(0.55f)
            .weight(30)
            .layer(DARKROOM_DEEPSLATE)
            .heightRangeUniform(-64, 0)
            .dimensions(Set.of(ResourceKey.create(Registries.DIMENSION, GTMS.id(GTMSDimension.TheDarkroom))))
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(DEEPSLATE_RULE)
                            .layer(l -> l.weight(3).state(GTMSBlocks.TIME_SAND.get().defaultBlockState()).size(2, 3))
                            .layer(l -> l.weight(2).state(GTMSBlocks.TIME_SAND.get().defaultBlockState()).size(1, 2))
                            .build())));

    private static GTOreDefinition create(String name, java.util.function.Consumer<GTOreDefinition> config) {
        return GTOres.create(GTMS.id(name), config);
    }

    public static void init() {}
}

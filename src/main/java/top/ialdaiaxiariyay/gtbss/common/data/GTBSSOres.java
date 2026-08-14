package top.ialdaiaxiariyay.gtbss.common.data;

import com.gregtechceu.gtceu.api.data.worldgen.*;
import com.gregtechceu.gtceu.common.data.GTOres;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;

import top.ialdaiaxiariyay.gtbss.GTBSS;

import java.util.Set;

import static top.ialdaiaxiariyay.gtbss.common.data.GTBSSWorldGenLayers.*;

@MethodsReturnNonnullByDefault
public class GTBSSOres {

    public static final GTOreDefinition TIME_SAND_VEIN_DEEPSLATE = create("time_sand_vein_deepslate", vein -> vein
            .clusterSize(UniformInt.of(8, 16))
            .density(0.55f)
            .weight(30)
            .layer(DARKROOM_DEEPSLATE)
            .heightRangeUniform(-64, 0)
            .dimensions(Set.of(ResourceKey.create(Registries.DIMENSION, GTBSS.id(GTBSSDimension.TheDarkroom))))
            .layeredVeinGenerator(generator -> generator
                    .withLayerPattern(() -> GTLayerPattern.builder(DEEPSLATE_RULE)
                            .layer(l -> l.weight(3).state(GTBSSBlocks.TIME_SAND.get().defaultBlockState()).size(2, 3))
                            .layer(l -> l.weight(2).state(GTBSSBlocks.TIME_SAND.get().defaultBlockState()).size(1, 2))
                            .build())));

    private static GTOreDefinition create(String name, java.util.function.Consumer<GTOreDefinition> config) {
        return GTOres.create(GTBSS.id(name), config);
    }

    public static void init() {}
}

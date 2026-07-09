package top.ialdaiaxiariyay.gtms.common.data.machines;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.pattern.PatternBuilderLoader;
import top.ialdaiaxiariyay.gtms.common.data.GTMSBlocks;
import top.ialdaiaxiariyay.gtms.common.machine.multiblock.noenergy.SpunTimeAnchorMachine;

import static com.gregtechceu.gtceu.api.multiblock.Predicates.blocks;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.controller;
import static top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate.REGISTRATE;

public class GTMSMultiblockMachinesA {

    public static void init() {}

    public static final MultiblockMachineDefinition SPUN_TiME_ANCHOR = REGISTRATE
            .multiblock("spun_time_anchor", SpunTimeAnchorMachine::new)
            .alwaysTryModifyRecipe(true)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GTRecipeTypes.DUMMY_RECIPES)
            .tooltips(Component.translatable("gtms.machine.spun_time_anchor.tips.0"))
            .pattern(definition -> PatternBuilderLoader
                    .fromResourceCached(GTMS.id("multiblock/spun_time_anchor.mb"))
                    .where("~", controller(blocks(definition.get())))
                    .where("C", blocks(Blocks.LIGHT_BLUE_STAINED_GLASS))
                    .where("A", blocks(Blocks.SMOOTH_QUARTZ_SLAB))
                    .where("F", blocks(Blocks.QUARTZ_PILLAR))
                    .where("E", blocks(Blocks.AMETHYST_BLOCK))
                    .where("H", blocks(GTMSBlocks.DIVERGENT_DREAM_ROD.get()))
                    .where("B", blocks(Blocks.SMOOTH_QUARTZ))
                    .where("I", blocks(Blocks.QUARTZ_STAIRS))
                    .where("G", blocks(Blocks.SEA_LANTERN))
                    .where("D", blocks(Blocks.QUARTZ_BRICKS))
                    .build())
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .simpleModel(GTMS.id("block/machine/spun_time_anchor"))
            .register();
}

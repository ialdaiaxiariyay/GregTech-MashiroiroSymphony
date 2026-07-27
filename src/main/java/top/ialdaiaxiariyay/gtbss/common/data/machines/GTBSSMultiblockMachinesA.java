package top.ialdaiaxiariyay.gtbss.common.data.machines;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.machine.GTBSSPartAbility;
import top.ialdaiaxiariyay.gtbss.api.machine.multiblock.MultipleRecipeCoilWorkableElectricMultiblockMachine;
import top.ialdaiaxiariyay.gtbss.api.pattern.PatternBuilderLoader;
import top.ialdaiaxiariyay.gtbss.api.recipe.GTBSSRecipeModifiers;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSBlocks;
import top.ialdaiaxiariyay.gtbss.common.machine.multiblock.noenergy.SpunTimeAnchorMachine;

import java.util.Objects;

import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_INVAR_HEATPROOF;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.BATCH_MODE;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.PARALLEL_HATCH;
import static top.ialdaiaxiariyay.gtbss.api.registrate.GTBSSRegistrate.REGISTRATION;

public class GTBSSMultiblockMachinesA {

    public static void init() {}

    public static final MultiblockMachineDefinition SPUN_TiME_ANCHOR = REGISTRATION
            .multiblock("spun_time_anchor", SpunTimeAnchorMachine::new)
            .alwaysTryModifyRecipe(true)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GTRecipeTypes.DUMMY_RECIPES)
            .tooltips(Component.translatable("gtbss.machine.spun_time_anchor.tips.0"))
            .pattern(definition -> PatternBuilderLoader
                    .fromResourceCached(GTBSS.id("multiblock/spun_time_anchor.mb"))
                    .where("~", controller(blocks(definition.get())))
                    .where("C", blocks(Blocks.LIGHT_BLUE_STAINED_GLASS))
                    .where("A", blocks(Blocks.SMOOTH_QUARTZ_SLAB))
                    .where("F", blocks(Blocks.QUARTZ_PILLAR))
                    .where("E", blocks(Blocks.AMETHYST_BLOCK))
                    .where("H", blocks(GTBSSBlocks.DIVERGENT_DREAM_ROD.get()))
                    .where("B", blocks(Blocks.SMOOTH_QUARTZ))
                    .where("I", blocks(Blocks.QUARTZ_STAIRS))
                    .where("G", blocks(Blocks.SEA_LANTERN))
                    .where("D", blocks(Blocks.QUARTZ_BRICKS))
                    .build())
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .simpleModel(GTBSS.id("block/machine/spun_time_anchor"))
            .register();

    public static final MultiblockMachineDefinition ELECTRIC_BLAST_FURNACE = REGISTRATION
            .multiblock("electric_blast_furnace", MultipleRecipeCoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.BLAST_RECIPES)
            .recipeModifiers(GTBSSRecipeModifiers::NoParallelEbfOverclock, BATCH_MODE, PARALLEL_HATCH,
                    GTRecipeModifiers.OC_PERFECT)
            .appearanceBlock(CASING_INVAR_HEATPROOF)
            .pattern(definition -> MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                    .slice("XXX", "CCC", "CCC", "XXX")
                    .slice("XXX", "C#C", "C#C", "XMX")
                    .slice("XSX", "CCC", "CCC", "XXX")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('X', blocks(CASING_INVAR_HEATPROOF.get()).setMinGlobalLimited(9)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, true))
                            .or(abilities(GTBSSPartAbility.MULTIPLE_RECIPE_PARALLEL).setMaxGlobalLimited(1)))
                    .where('M', abilities(PartAbility.MUFFLER))
                    .where('C', heatingCoils())
                    .where('#', air())
                    .build())
            .recoveryItems(
                    () -> new ItemLike[] {
                            Objects.requireNonNull(
                                    GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash)).get() })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_heatproof"),
                    GTCEu.id("block/multiblock/electric_blast_furnace"))
            .tooltips(Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.0"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.1"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.2"))
            .additionalDisplay((controller, components) -> {
                // spotless:off
                if (controller instanceof MultipleRecipeCoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                            Component.translatable(
                                            FormattingUtil.formatNumbers(coilMachine.getCoilType().getCoilTemperature() +
                                                    100L * Math.max(0, coilMachine.getTier() - GTValues.MV)) + "K")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                }
                // spotless:on
            })
            .register();
}

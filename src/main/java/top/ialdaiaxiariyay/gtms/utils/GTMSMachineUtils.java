package top.ialdaiaxiariyay.gtms.utils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import top.ialdaiaxiariyay.bettergtae.utils.NumberUtil;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.GTMSValues;
import top.ialdaiaxiariyay.gtms.api.machine.SimpleTieredManaMachine;
import top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate;
import top.ialdaiaxiariyay.gtms.common.machine.multiblock.part.energy.WirelessLaserHatchPartMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.IN;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;
import static top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTMSMachineUtils {

    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition, ?>, MachineDefinition> builder,
                                                             int... tiers) {
        return registerTieredMachines(REGISTRATE, name, factory, builder, tiers);
    }

    public static MachineDefinition[] registerTieredMachines(GTMSRegistrate registrate,
                                                             String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition, ?>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = registrate
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static MachineDefinition[] registerWirelessLaserHatch(IO io, int amperage, PartAbility ability) {
        return registerWirelessLaserHatch(REGISTRATE, io, amperage, ability);
    }

    public static MachineDefinition[] registerWirelessLaserHatch(GTMSRegistrate registrate, IO io, int amperage,
                                                                 PartAbility ability) {
        String name = io == IN ? "target" : "source";
        return registerTieredMachines(registrate, amperage + "a_wireless_laser_" + name + "_hatch",
                (holder, tier) -> new WirelessLaserHatchPartMachine(holder, tier, io, amperage),
                (tier, builder) -> builder
                        .langValue(VNF[tier] + "§r " + FormattingUtil.formatNumbers(amperage) + "§eA§r Laser " +
                                toEnglishName(name) + " Hatch")
                        .rotationState(RotationState.ALL)
                        .tooltips(Component.translatable("gtceu.machine.laser_hatch." + name + ".tooltip"),
                                Component.translatable("gtceu.machine.laser_hatch.both.tooltip"),
                                Component.translatable("gtceu.universal.tooltip.voltage_" + (io == IN ? "in" : "out"),
                                        FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.amperage_in", amperage),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        FormattingUtil
                                                .formatNumbers(
                                                        EnergyHatchPartMachine.getHatchEnergyCapacity(tier, amperage))),
                                Component.translatable("gtceu.part_sharing.disabled"))
                        .abilities(ability)
                        .modelProperty(IS_FORMED, false)
                        .overlayTieredHullModel(GTCEu.id("block/machine/part/" + "laser_" + name + "_hatch"))
                        .register(),
                HIGH_TIERS);
    }

    public static MachineDefinition[] registerSimpleExtMachines(String name, GTRecipeType recipeType,
                                                                Int2IntFunction tankScalingFunction,
                                                                RecipeModifier overclockModifier,
                                                                boolean hasPollutionDebuff) {
        return registerSimpleExtMachines(GTMSRegistrate.REGISTRATE, name, recipeType, tankScalingFunction,
                overclockModifier,
                hasPollutionDebuff);
    }

    public static MachineDefinition[] registerSimpleExtMachines(GTMSRegistrate registrate, String name,
                                                                GTRecipeType recipeType,
                                                                Int2IntFunction tankScalingFunction,
                                                                RecipeModifier overclockModifier,
                                                                boolean hasPollutionDebuff) {
        return registerSimpleExtMachines(registrate, name, recipeType, tankScalingFunction, overclockModifier,
                hasPollutionDebuff,
                ELECTRIC_TIERS);
    }

    public static MachineDefinition[] registerSimpleExtMachines(String name, GTRecipeType recipeType,
                                                                Int2IntFunction tankScalingFunction,
                                                                RecipeModifier overclockModifier) {
        return registerSimpleExtMachines(GTMSRegistrate.REGISTRATE, name, recipeType, tankScalingFunction,
                overclockModifier);
    }

    public static MachineDefinition[] registerSimpleExtMachines(GTMSRegistrate registrate, String name,
                                                                GTRecipeType recipeType,
                                                                Int2IntFunction tankScalingFunction,
                                                                RecipeModifier overclockModifier) {
        return registerSimpleExtMachines(registrate, name, recipeType, tankScalingFunction, overclockModifier, false);
    }

    public static MachineDefinition[] registerSimpleExtMachines(String name, GTRecipeType recipeType,
                                                                RecipeModifier overclockModifier) {
        return registerSimpleExtMachines(GTMSRegistrate.REGISTRATE, name, recipeType, overclockModifier);
    }

    public static MachineDefinition[] registerSimpleExtMachines(GTMSRegistrate registrate, String name,
                                                                GTRecipeType recipeType,
                                                                RecipeModifier overclockModifier) {
        return registerSimpleExtMachines(registrate, name, recipeType, defaultTankSizeFunction, overclockModifier);
    }

    public static MachineDefinition[] registerSimpleExtMachines(GTMSRegistrate registrate,
                                                                String name,
                                                                GTRecipeType recipeType,
                                                                Int2IntFunction tankScalingFunction,
                                                                RecipeModifier overclockModifier,
                                                                boolean hasPollutionDebuff,
                                                                int... tiers) {
        return registerTieredExtMachines(registrate, name,
                (holder, tier) -> new SimpleTieredManaMachine(holder, tier, tankScalingFunction), (tier, builder) -> {
                    if (hasPollutionDebuff) {
                        builder.recipeModifiers(GTRecipeModifiers.ENVIRONMENT_REQUIREMENT
                                .apply(GTMedicalConditions.CARBON_MONOXIDE_POISONING, 100 * tier),
                                overclockModifier)
                                .conditionalTooltip(defaultEnvironmentRequirement(),
                                        ConfigHolder.INSTANCE.gameplay.environmentalHazards);
                    } else {
                        builder.recipeModifier(overclockModifier);
                    }
                    return builder
                            .editableUI(SimpleTieredManaMachine.EDITABLE_UI_CREATOR.apply(GTMS.id(name), recipeType))
                            .rotationState(RotationState.NON_Y_AXIS)
                            .recipeType(recipeType)
                            .workableTieredHullModel(GTMS.id("block/machines/" + name))
                            .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType,
                                    tankScalingFunction.applyAsInt(tier), true))
                            .register();
                },
                tiers);
    }

    public static MachineDefinition[] registerSimpleManaMachines(String name, GTRecipeType recipeType,
                                                                 RecipeModifier overclockModifier) {
        return registerSimpleManaMachines(GTMSRegistrate.REGISTRATE, name, recipeType, overclockModifier);
    }

    public static MachineDefinition[] registerSimpleManaMachines(GTMSRegistrate registrate, String name,
                                                                 GTRecipeType recipeType,
                                                                 RecipeModifier overclockModifier) {
        return registerSimpleManaMachines(registrate, name, recipeType, defaultTankSizeFunction, overclockModifier,
                ELECTRIC_TIERS);
    }

    public static MachineDefinition[] registerSimpleManaMachines(GTMSRegistrate registrate,
                                                                 String name,
                                                                 GTRecipeType recipeType,
                                                                 Int2IntFunction tankScalingFunction,
                                                                 RecipeModifier overclockModifier,
                                                                 int... tiers) {
        return registerTieredExtMachines(registrate, name,
                (holder, tier) -> new SimpleTieredManaMachine(holder, tier, tankScalingFunction),
                (tier, builder) -> builder
                        .editableUI(SimpleTieredManaMachine.EDITABLE_UI_CREATOR.apply(GTMS.id(name), recipeType))
                        .rotationState(RotationState.NON_Y_AXIS)
                        .recipeType(recipeType)
                        .recipeModifier(overclockModifier)
                        .workableTieredHullModel(GTMS.id("block/machines/" + name))
                        .tooltips(workableTieredMana(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType,
                                tankScalingFunction.applyAsInt(tier), true))
                        .register(),
                tiers);
    }

    public static Component[] workableTieredMana(int tier, long voltage, long energyCapacity, GTRecipeType recipeType,
                                                 long tankCapacity, boolean input) {
        List<Component> tooltipComponents = new ArrayList<>();
        tooltipComponents
                .add(input ?
                        Component.translatable("gtms.universal.tooltip.mana_in",
                                NumberUtil.formatLong(voltage), GTMSValues.MNF[tier]) :
                        Component.translatable("gtms.universal.tooltip.mana_out",
                                NumberUtil.formatLong(voltage), GTMSValues.MNF[tier]));
        tooltipComponents
                .add(Component.translatable("gtms.universal.tooltip.mana_storage_capacity",
                        NumberUtil.formatLong(energyCapacity)));
        if (recipeType.getMaxInputs(FluidRecipeCapability.CAP) > 0 ||
                recipeType.getMaxOutputs(FluidRecipeCapability.CAP) > 0)
            tooltipComponents
                    .add(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                            NumberUtil.formatLong(tankCapacity)));
        return tooltipComponents.toArray(Component[]::new);
    }

    public static MachineDefinition[] registerTieredExtMachines(String name,
                                                                BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                                BiFunction<Integer, MachineBuilder<MachineDefinition, ?>, MachineDefinition> builder,
                                                                int... tiers) {
        return registerTieredExtMachines(GTMSRegistrate.REGISTRATE, name, factory, builder, tiers);
    }

    public static MachineDefinition[] registerTieredExtMachines(GTMSRegistrate registrate,
                                                                String name,
                                                                BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                                BiFunction<Integer, MachineBuilder<MachineDefinition, ?>, MachineDefinition> builder,
                                                                int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = registrate
                    .extMachine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }
}

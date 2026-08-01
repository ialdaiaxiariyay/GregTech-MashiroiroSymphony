package top.ialdaiaxiariyay.gtbss.utils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.LaserHatchPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Setter;
import lombok.experimental.Accessors;
import top.ialdaiaxiariyay.gtbss.api.GTBSSValues;
import top.ialdaiaxiariyay.gtbss.api.machine.SimpleTieredManaMachine;
import top.ialdaiaxiariyay.gtbss.api.recipe.GTBSSRecipeModifiers;
import top.ialdaiaxiariyay.gtbss.api.registrate.GTBSSRegistrate;
import top.ialdaiaxiariyay.gtbss.common.mui.GTBSSSingleblockMachinePanels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.GTValues.VLVT;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.IN;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTBSSMachineUtils {

    public static final int[] Luv2Max_TIERS = GTValues.tiersBetween(LuV, MAX);

    public static <MACHINE extends MetaMachine> MachineDefinition[] registerTieredMachines(String name,
                                                                                           MachineInstanceFactory.Tiered<MACHINE> factory,
                                                                                           BiFunction<Integer, MachineBuilder<MachineDefinition, MACHINE, ?>, MachineDefinition> builder,
                                                                                           int... tiers) {
        return registerTieredMachines(GTBSSRegistrate.REGISTRATION, name, factory, builder, tiers);
    }

    public static <MACHINE extends MetaMachine> MachineDefinition[] registerTieredMachines(GTBSSRegistrate registrate,
                                                                                           String name,
                                                                                           MachineInstanceFactory.Tiered<MACHINE> factory,
                                                                                           BiFunction<Integer, MachineBuilder<MachineDefinition, MACHINE, ?>, MachineDefinition> builder,
                                                                                           int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = registrate
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            info -> factory.buildMachine(info, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static MachineDefinition[] registerWirelessLaserHatch(IO io, int amperage, PartAbility ability) {
        return registerWirelessLaserHatch(GTBSSRegistrate.REGISTRATION, io, amperage, ability);
    }

    public static MachineDefinition[] registerWirelessLaserHatch(GTBSSRegistrate registrate, IO io, int amperage,
                                                                 PartAbility ability) {
        String name = io == IN ? "target" : "source";
        return registerTieredMachines(registrate, amperage + "a_wireless_laser_" + name + "_hatch",
                (holder, tier) -> new LaserHatchPartMachine(holder, io, tier, amperage), (tier, builder) -> builder
                        .langValue(VNF[tier] + "§r " + FormattingUtil.formatNumbers(amperage) + "§eA§r Laser " +
                                FormattingUtil.toEnglishName(name) + " Hatch")
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
                GTValues.tiersBetween(IV, MAX));
    }

    public static MachineDefinition[] registerSimpleManaMachines(String name,
                                                                 GTRecipeType recipeType, boolean Perfect,
                                                                 Component... components) {
        return new SimpleManaMachineBuilder(GTBSSRegistrate.REGISTRATION, name, recipeType, Perfect, components)
                .register();
    }

    public static MachineDefinition[] registerSimpleManaMachines(String name,
                                                                 GTRecipeType recipeType, boolean Perfect) {
        return new SimpleManaMachineBuilder(GTBSSRegistrate.REGISTRATION, name, recipeType, Perfect)
                .register();
    }

    @Accessors(chain = true, fluent = true)
    public static class SimpleManaMachineBuilder {

        private final GTBSSRegistrate registrate;
        @Setter
        private String name;
        @Setter
        private GTRecipeType recipeType;
        @Setter
        private Int2IntFunction tankScalingFunction = defaultTankSizeFunction;
        @Setter
        private PanelFactory panelFactory = null;
        @Setter
        private int[] tiers = ELECTRIC_TIERS;
        @Setter
        private List<Component> componentList;
        @Setter
        private boolean PerfectOverclocked;

        public SimpleManaMachineBuilder(GTBSSRegistrate registrate, String name, GTRecipeType recipeType,
                                        boolean PerfectOverclocked, Component... components) {
            this.registrate = registrate;
            this.name = name;
            this.recipeType = recipeType;
            this.componentList = Arrays.asList(components);
            this.PerfectOverclocked = PerfectOverclocked;
        }

        public MachineDefinition[] register() {
            if (panelFactory == null) {
                panelFactory = GTBSSSingleblockMachinePanels.GENERAL_MACHINE;
            }
            return registerTieredMachines(registrate, name,
                    (holder, tier) -> new SimpleTieredManaMachine(holder, tier, tankScalingFunction),
                    (tier, builder) -> {
                        RecipeModifier recipeModifier = PerfectOverclocked ? GTBSSRecipeModifiers.MANA_OC_PERFECT :
                                GTBSSRecipeModifiers.MANA_OC_NON_PERFECT;
                        builder
                                .langValue("%s %s %s".formatted(VLVH[tier], toEnglishName(name), VLVT[tier]))
                                .rotationState(RotationState.NON_Y_AXIS)
                                .recipeType(recipeType)
                                .recipeModifiers(recipeModifier)
                                .workableTieredHullModel(GTCEu.id("block/machines/" + name))
                                .tooltips(workableManaTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType,
                                        tankScalingFunction.applyAsInt(tier), true))
                                .tooltips(componentList)
                                .ui(panelFactory);
                        return builder.register();
                    },
                    tiers);
        }
    }

    public static Component[] workableManaTiered(int tier, long voltage, long energyCapacity, GTRecipeType recipeType,
                                                 long tankCapacity, boolean input) {
        List<Component> tooltipComponents = new ArrayList<>();
        tooltipComponents
                .add(input ?
                        Component.translatable("gtbss.universal.tooltip.mana_in",
                                FormattingUtil.formatNumbers(voltage), GTBSSValues.MNF[tier]) :
                        Component.translatable("gtbss.universal.tooltip.mana_out",
                                FormattingUtil.formatNumbers(voltage), GTBSSValues.MNF[tier]));
        tooltipComponents
                .add(Component.translatable("gtbss.universal.tooltip.mana_storage_capacity",
                        FormattingUtil.formatNumbers(energyCapacity)));
        if (recipeType.getMaxInputs(FluidRecipeCapability.CAP) > 0 ||
                recipeType.getMaxOutputs(FluidRecipeCapability.CAP) > 0)
            tooltipComponents
                    .add(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                            FormattingUtil.formatNumbers(tankCapacity)));
        return tooltipComponents.toArray(Component[]::new);
    }
}

package top.ialdaiaxiariyay.gtbss.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import top.ialdaiaxiariyay.gtbss.api.registrate.GTBSSRegistrate;
import top.ialdaiaxiariyay.gtbss.common.data.machines.GTBSSMultiblockMachinesA;
import top.ialdaiaxiariyay.gtbss.common.machine.multiblock.part.MEOutputDualPartMachine;
import top.ialdaiaxiariyay.gtbss.common.machine.multiblock.part.energy.WirelessEnergyHatchPartMachine;
import top.ialdaiaxiariyay.gtbss.common.machine.multiblock.part.mana.ManaHatchPartMachine;
import top.ialdaiaxiariyay.gtbss.common.machine.multiblock.part.mana.WirelessManaHatchPartMachine;
import top.ialdaiaxiariyay.gtbss.common.machine.multiblock.part.steam.WirelessSteamHatchPartMachine;
import top.ialdaiaxiariyay.gtbss.common.machine.noenergy.WirelessResourceMonitor;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.IN;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.OUT;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.*;
import static top.ialdaiaxiariyay.gtbss.utils.GTBSSMachineUtils.*;

public class GTBSSMachines {

    public static void init() {
        GTBSSMultiblockMachinesA.init();
    }

    static {
        GTBSSRegistrate.REGISTRATION.creativeModeTab(() -> GTBSSCreativeModeTab.MACHINE);
    }

    public static final MachineDefinition[] ARC_FURNACE = registerSimpleManaMachines("arc_furnace",
            GTBSSRecipeTypes.FURNACE_RECIPES, false);

    public static final MachineDefinition WE_MONITOR = GTBSSRegistrate.REGISTRATION
            .machine("wire1nitor", (holder) -> new WirelessManaHatchPartMachine(holder, 2, IN, 4))
            .tier(GTValues.MV)
            .rotationState(RotationState.ALL)
            .overlayTieredHullModel("wireless_resource_monitor")
            .register();

    public static final MachineDefinition WE_M13ONITOR = GTBSSRegistrate.REGISTRATION
            .machine("wire31nitor", (holder) -> new ManaHatchPartMachine(holder, 2, IN, 4))
            .tier(GTValues.MV)
            .rotationState(RotationState.ALL)
            .overlayTieredHullModel("wireless_resource_monitor")
            .register();

    public static final MachineDefinition WE_M13ON1ITOR = GTBSSRegistrate.REGISTRATION
            .machine("wire31ni1tor", (holder) -> new ManaHatchPartMachine(holder, 2, OUT, 4))
            .tier(GTValues.MV)
            .rotationState(RotationState.ALL)
            .overlayTieredHullModel("wireless_resource_monitor")
            .register();

    public static final MachineDefinition WIRELESS_RESOURCE_MONITOR = GTBSSRegistrate.REGISTRATION
            .machine("wireless_resource_monitor", WirelessResourceMonitor::new)
            .tier(GTValues.ULV)
            .rotationState(RotationState.ALL)
            .overlayTieredHullModel("wireless_resource_monitor")
            .tooltips(Component.translatable("gtbss.machine.wireless_resource_monitor.tooltip.summary"),
                    Component.translatable("gtbss.machine.wireless_resource_monitor.tooltip.usage"))
            .register();

    public static final MachineDefinition WIRELESS_STEAM_INPUT_HATCH = GTBSSRegistrate.REGISTRATION
            .machine("wireless_steam_input_hatch", (holder) -> new WirelessSteamHatchPartMachine(holder, IO.IN))
            .tier(GTValues.ULV)
            .abilities(PartAbility.STEAM)
            .rotationState(RotationState.ALL)
            .overlaySteamHullModel(GTCEu.id("block/machine/part/steam_hatch"))
            .modelProperty(IS_FORMED, false)
            .allowCoverOnFront(true)
            .tooltips(
                    Component.translatable("gtbss.machine.steam.wireless_steam_in_hatch.tooltip"),
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 64000 * 100),
                    Component.translatable("gtceu.machine.steam.steam_hatch.tooltip"))
            .register();

    public static final MachineDefinition WIRELESS_STEAM_OUTPUT_HATCH = GTBSSRegistrate.REGISTRATION
            .machine("wireless_steam_output_hatch", (holder) -> new WirelessSteamHatchPartMachine(holder, IO.OUT))
            .abilities(PartAbility.EXPORT_FLUIDS)
            .tier(GTValues.ULV)
            .rotationState(RotationState.ALL)
            .overlaySteamHullModel(GTCEu.id("block/machine/part/steam_hatch"))
            .modelProperty(IS_FORMED, false)
            .allowCoverOnFront(true)
            .tooltips(Component.translatable("gtbss.machine.steam.wireless_steam_out_hatch.tooltip"),
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 64000 * 100),
                    Component.translatable("gtceu.machine.steam.steam_hatch.tooltip"))
            .register();

    public final static MachineDefinition ME_DUAL_OUTPUT_PART = GTBSSRegistrate.REGISTRATION
            .machine("me_dual_output_part", MEOutputDualPartMachine::new)
            .tier(LuV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.EXPORT_FLUIDS)
            .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_output_hatch"))
            .tooltips(
                    Component.translatable("gtbss.machine.me_dual_output_part.tooltip.0"),
                    Component.translatable("gtbss.machine.me_dual_output_part.tooltip.1"),
                    Component.translatable("gtceu.machine.me.export.tooltip"),
                    Component.translatable("gtceu.part_sharing.enabled"))
            .register();

    public static final MachineDefinition[] ENERGY_INPUT_HATCH = registerTieredMachines("wireless_energy_input_hatch",
            (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, IN, 2),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Energy Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.INPUT_ENERGY)
                    .modelProperty(IS_FORMED, false)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_in",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_in", 2),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(
                                                    WirelessEnergyHatchPartMachine.getHatchEnergyCapacity(tier, 2))),
                            Component.translatable("gtceu.machine.energy_hatch.input.tooltip"))
                    .overlayTieredHullModel(GTCEu.id("block/machine/part/energy_input_hatch"))
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH = registerTieredMachines("wireless_energy_output_hatch",
            (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, OUT, 2),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Dynamo Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .modelProperty(IS_FORMED, false)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_out",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_out", 2),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(
                                                    WirelessEnergyHatchPartMachine.getHatchEnergyCapacity(tier, 2))),
                            Component.translatable("gtceu.machine.energy_hatch.output.tooltip"))
                    .overlayTieredHullModel(GTCEu.id("block/machine/part/energy_output_hatch"))
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] ENERGY_INPUT_HATCH_4A = registerTieredMachines(
            "wireless_energy_input_hatch_4a",
            (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, IN, 4),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 4A Energy Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.INPUT_ENERGY)
                    .modelProperty(IS_FORMED, false)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_in",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_in", 4),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(
                                                    WirelessEnergyHatchPartMachine.getHatchEnergyCapacity(tier, 4))),
                            Component.translatable("gtceu.machine.energy_hatch.input_hi_amp.tooltip"))
                    .overlayTieredHullModel(GTCEu.id("block/machine/part/energy_input_hatch_4a"))
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH_4A = registerTieredMachines(
            "wireless_energy_output_hatch_4a",
            (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, OUT, 4),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 4A Dynamo Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .modelProperty(IS_FORMED, false)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_out",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_out", 4),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(
                                                    WirelessEnergyHatchPartMachine.getHatchEnergyCapacity(tier, 4))),
                            Component.translatable("gtceu.machine.energy_hatch.output_hi_amp.tooltip"))
                    .overlayTieredHullModel(GTCEu.id("block/machine/part/energy_output_hatch_4a"))
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] ENERGY_INPUT_HATCH_16A = registerTieredMachines(
            "wireless_energy_input_hatch_16a",
            (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, IN, 16),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 16A Energy Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.INPUT_ENERGY)
                    .modelProperty(IS_FORMED, false)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_in",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_in", 16),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(
                                                    WirelessEnergyHatchPartMachine.getHatchEnergyCapacity(tier, 16))),
                            Component.translatable("gtceu.machine.energy_hatch.input_hi_amp.tooltip"))
                    .overlayTieredHullModel(GTCEu.id("block/machine/part/energy_input_hatch_16a"))
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH_16A = registerTieredMachines(
            "wireless_energy_output_hatch_16a",
            (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, OUT, 16),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 16A Dynamo Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .modelProperty(IS_FORMED, false)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_out",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_out", 16),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(
                                                    WirelessEnergyHatchPartMachine.getHatchEnergyCapacity(tier, 16))),
                            Component.translatable("gtceu.machine.energy_hatch.output_hi_amp.tooltip"))
                    .overlayTieredHullModel(GTCEu.id("block/machine/part/energy_output_hatch_16a"))
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] LASER_INPUT_HATCH_256 = registerWirelessLaserHatch(IN, 256,
            PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_OUTPUT_HATCH_256 = registerWirelessLaserHatch(OUT, 256,
            PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] LASER_INPUT_HATCH_1024 = registerWirelessLaserHatch(IN, 1024,
            PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_OUTPUT_HATCH_1024 = registerWirelessLaserHatch(OUT, 1024,
            PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] LASER_INPUT_HATCH_4096 = registerWirelessLaserHatch(IN, 4096,
            PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_OUTPUT_HATCH_4096 = registerWirelessLaserHatch(OUT, 4096,
            PartAbility.OUTPUT_LASER);
}

package top.ialdaiaxiariyay.gtms.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate;
import top.ialdaiaxiariyay.gtms.common.machine.noenergy.WirelessResourceMonitor;
import top.ialdaiaxiariyay.gtms.common.machine.part.energy.WirelessEnergyHatchPartMachine;
import top.ialdaiaxiariyay.gtms.common.machine.part.steam.WirelessSteamHatchPartMachine;

import static com.gregtechceu.gtceu.api.GTValues.V;
import static com.gregtechceu.gtceu.api.GTValues.VNF;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.IN;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.OUT;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.ALL_TIERS;
import static top.ialdaiaxiariyay.gtms.utils.GTMSMachineUtils.registerTieredMachines;

public class GTMSMachines {

    public static void init() {}

    static {
        GTMSRegistrate.REGISTRATE.creativeModeTab(() -> GTMSCreativeModeTab.MACHINE);
    }

    public static final MachineDefinition WIRELESS_RESOURCE_MONITOR = GTMSRegistrate.REGISTRATE
            .machine("wireless_resource_monitor", WirelessResourceMonitor::new)
            .tier(GTValues.ULV)
            .rotationState(RotationState.ALL)
            .overlayTieredHullModel("wireless_resource_monitor")
            .tooltips(Component.translatable("gtms.machine.wireless_resource_monitor.tooltip.summary"),
                    Component.translatable("gtms.machine.wireless_resource_monitor.tooltip.usage"))
            .register();

    public static final MachineDefinition WIRELESS_STEAM_INPUT_HATCH = GTMSRegistrate.REGISTRATE
            .machine("wireless_steam_input_hatch", (holder) -> new WirelessSteamHatchPartMachine(holder, IO.IN))
            .tier(GTValues.ULV)
            .abilities(PartAbility.STEAM)
            .rotationState(RotationState.ALL)
            .overlaySteamHullModel(GTCEu.id("block/machine/part/steam_hatch"))
            .tooltips(
                    Component.translatable("gtms.machine.steam.wireless_steam_in_hatch.tooltip"),
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 64000 * 100),
                    Component.translatable("gtceu.machine.steam.steam_hatch.tooltip"))
            .register();

    public static final MachineDefinition WIRELESS_STEAM_OUTPUT_HATCH = GTMSRegistrate.REGISTRATE
            .machine("wireless_steam_output_hatch", (holder) -> new WirelessSteamHatchPartMachine(holder, IO.OUT))
            .abilities(PartAbility.EXPORT_FLUIDS)
            .tier(GTValues.ULV)
            .rotationState(RotationState.ALL)
            .overlaySteamHullModel(GTCEu.id("block/machine/part/steam_hatch"))
            .tooltips(Component.translatable("gtms.machine.steam.wireless_steam_out_hatch.tooltip"),
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 64000 * 100),
                    Component.translatable("gtceu.machine.steam.steam_hatch.tooltip"))
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
}

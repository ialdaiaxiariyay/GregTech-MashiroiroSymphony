package top.ialdaiaxiariyay.gtms.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamHatchPartMachine;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate;
import top.ialdaiaxiariyay.gtms.common.machine.steam.WirelessSteamHatchPartMachine;

public class GTMSMachines {
    public static void init() {}

   public static final MachineDefinition WIRELESS_STEAM_INPUT_HATCH = GTMSRegistrate.REGISTRATE
           .machine("wireless_steam_input_hatch",(holder)-> new WirelessSteamHatchPartMachine(holder, IO.IN))
           .tier(GTValues.ULV)
           .abilities(PartAbility.STEAM)
           .rotationState(RotationState.ALL)
           .overlaySteamHullModel(GTCEu.id("block/machine/part/steam_hatch"))
           .tooltips(
                   Component.translatable("gtms.machine.steam.wireless_steam_in_hatch.tooltip"),
                   Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 64000*100),
                   Component.translatable("gtceu.machine.steam.steam_hatch.tooltip"))
           .register();

   public static final MachineDefinition WIRELESS_STEAM_OUTPUT_HATCH = GTMSRegistrate.REGISTRATE
           .machine("wireless_steam_output_hatch",(holder)-> new WirelessSteamHatchPartMachine(holder, IO.OUT))
           .abilities(PartAbility.EXPORT_FLUIDS)
           .tier(GTValues.ULV)
           .rotationState(RotationState.ALL)
           .overlaySteamHullModel(GTCEu.id("block/machine/part/steam_hatch"))
           .tooltips(Component.translatable("gtms.machine.steam.wireless_steam_out_hatch.tooltip"),
                   Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 64000*100),
                   Component.translatable("gtceu.machine.steam.steam_hatch.tooltip"))
           .register();

}

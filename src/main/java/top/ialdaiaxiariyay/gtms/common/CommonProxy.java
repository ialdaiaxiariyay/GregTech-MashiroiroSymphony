package top.ialdaiaxiariyay.gtms.common;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate;
import top.ialdaiaxiariyay.gtms.common.data.GTMSMachines;

@SuppressWarnings("removal")
public class CommonProxy {

    public CommonProxy() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        GTMSRegistrate.REGISTRATE.registerEventListeners(modEventBus);
        modEventBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        GTMS.LOGGER.info("CommonProxy is Load");
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GTMSMachines.init();
    }
}

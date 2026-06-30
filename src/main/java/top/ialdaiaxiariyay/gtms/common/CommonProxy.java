package top.ialdaiaxiariyay.gtms.common;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.capability.forge.GTMSCapability;
import top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate;
import top.ialdaiaxiariyay.gtms.common.data.*;
import top.ialdaiaxiariyay.gtms.network.NetworkHandler;

@SuppressWarnings("removal")
public class CommonProxy {

    public CommonProxy() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
        GTMSRegistrate.REGISTRATE.registerEventListeners(modEventBus);
        init();
        modEventBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        modEventBus.addGenericListener(GTRecipeType.class, this::registerRecipeType);
        modEventBus.addListener(this::onCommonSetup);
        GTMS.LOGGER.info("CommonProxy is Load");
    }

    private void init() {
        GTMSCreativeModeTab.init();
        GTMSBlocks.init();
        GTMSItems.init();
    }

    private void onCommonSetup(@NotNull FMLCommonSetupEvent event) {
        NetworkHandler.register();
        event.enqueueWork(GTMSMagicModuleCombo::init);
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GTMSMachines.init();
    }

    private void registerRecipeType(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        GTMSRecipeTypes.init();
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        GTMSCapability.register(event);
    }
}

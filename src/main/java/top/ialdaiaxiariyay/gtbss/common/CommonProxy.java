package top.ialdaiaxiariyay.gtbss.common;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.capability.forge.GTBSSCapability;
import top.ialdaiaxiariyay.gtbss.api.capability.recipe.ManaRecipeCapability;
import top.ialdaiaxiariyay.gtbss.api.registrate.GTBSSRegistrate;
import top.ialdaiaxiariyay.gtbss.common.data.*;
import top.ialdaiaxiariyay.gtbss.data.recipe.RemoveRecipe;
import top.ialdaiaxiariyay.gtbss.mixin.mc.sounds.MusicsAccessor;
import top.ialdaiaxiariyay.gtbss.network.NetworkHandler;

@SuppressWarnings({ "removal", "deprecation" })
public class CommonProxy {

    public CommonProxy() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
        GTBSSRegistrate.REGISTRATION.registerEventListeners(modEventBus);
        init();
        modEventBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        modEventBus.addGenericListener(GTRecipeType.class, this::registerRecipeType);
        modEventBus.addGenericListener(SoundEntry.class, this::registerSounds);
        modEventBus.addGenericListener(RecipeCapability.class, this::registerRecipeCapabilities);
        modEventBus.addGenericListener(GTOreDefinition.class, this::registerOreVeins);
        modEventBus.addGenericListener(IWorldGenLayer.class, this::registerWorldgenLayers);
        modEventBus.addListener(this::onCommonSetup);
        GTBSS.LOGGER.info("CommonProxy is Load");
    }

    private void init() {
        RemoveRecipe.init();
        GTBSSCreativeModeTab.init();
        GTBSSBlocks.init();
        GTBSSItems.init();
        GTBSSEntityTypes.init();
        GTBSSEnchantments.init();
        VanillaRecipeType.init();
    }

    private void onCommonSetup(@NotNull FMLCommonSetupEvent event) {
        NetworkHandler.register();
        event.enqueueWork(GTBSSMagicModuleCombo::init);
        event.enqueueWork(this::setupCustomMenuMusic);
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GTBSSMachines.init();
    }

    private void registerRecipeType(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        GTBSSRecipeTypes.init();
    }

    private void registerSounds(GTCEuAPI.RegisterEvent<ResourceLocation, SoundEntry> event) {
        GTBSSSoundEvent.init();
    }

    private void registerRecipeCapabilities(GTCEuAPI.RegisterEvent<ResourceLocation, RecipeCapability<?>> event) {
        GTRegistries.RECIPE_CAPABILITIES.register(ManaRecipeCapability.CAP.id, ManaRecipeCapability.CAP);
    }

    private void registerOreVeins(GTCEuAPI.RegisterEvent<ResourceLocation, GTOreDefinition> event) {
        GTBSSOres.init();
    }

    private void registerWorldgenLayers(GTCEuAPI.RegisterEvent<ResourceLocation, IWorldGenLayer> event) {
        GTBSSWorldGenLayers.init();
    }

    public void setupCustomMenuMusic() {
        ResourceLocation musicId = GTBSS.id("menu_music");
        ResourceKey<SoundEvent> key = ResourceKey.create(BuiltInRegistries.SOUND_EVENT.key(), musicId);
        BuiltInRegistries.SOUND_EVENT.getHolder(key).ifPresentOrElse(holder -> {
            Music customMusic = new Music(holder, 20, 600, true);
            MusicsAccessor.setMenu(customMusic);
        }, () -> GTBSS.LOGGER.error("Custom menu music '{}' not found in registry. Using vanilla.", musicId));
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        GTBSSCapability.register(event);
    }

    @SubscribeEvent
    public void onMaterialEvent(MaterialEvent event) {
        GTBSSMaterials.init();
    }
}

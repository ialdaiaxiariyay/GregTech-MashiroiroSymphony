package top.ialdaiaxiariyay.gtms;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;

import top.ialdaiaxiariyay.gtms.api.capability.recipe.ManaRecipeCapability;
import top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate;
import top.ialdaiaxiariyay.gtms.common.data.GTMSOres;
import top.ialdaiaxiariyay.gtms.common.data.GTMSSoundEvent;
import top.ialdaiaxiariyay.gtms.data.recipe.AAA;

import java.util.function.Consumer;

@GTAddon
public class GTMSAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return GTMSRegistrate.REGISTRATE;
    }

    @Override
    public void initializeAddon() {}

    @Override
    public String addonModId() {
        return GTMS.MOD_ID;
    }

    @Override
    public void registerSounds() {
        GTMSSoundEvent.init();
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        AAA.init(provider);
    }

    @Override
    public void registerRecipeCapabilities() {
        GTRegistries.RECIPE_CAPABILITIES.register(ManaRecipeCapability.CAP.name, ManaRecipeCapability.CAP);
    }

    @Override
    public boolean requiresHighTier() {
        return true;
    }

    @Override
    public void registerOreVeins() {
        GTMSOres.init();
    }
}

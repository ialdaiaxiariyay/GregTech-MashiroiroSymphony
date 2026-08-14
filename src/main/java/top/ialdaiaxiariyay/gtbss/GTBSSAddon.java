package top.ialdaiaxiariyay.gtbss;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;

import top.ialdaiaxiariyay.gtbss.api.registrate.GTBSSRegistrate;
import top.ialdaiaxiariyay.gtbss.data.GTBSSRecipes;

import java.util.function.Consumer;

@GTAddon
public class GTBSSAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return GTBSSRegistrate.REGISTRATION;
    }

    @Override
    public void initializeAddon() {}

    @Override
    public String addonModId() {
        return GTBSS.MOD_ID;
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        GTBSSRecipes.init(consumer);
    }

    @Override
    public boolean requiresHighTier() {
        return true;
    }
}

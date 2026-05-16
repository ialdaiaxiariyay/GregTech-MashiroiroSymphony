package top.ialdaiaxiariyay.gtms;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import top.ialdaiaxiariyay.gtms.api.registrate.GTMSRegistrate;
import top.ialdaiaxiariyay.gtms.common.data.GTMSSoundEvent;

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
}

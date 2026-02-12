package top.ialdaiaxiariyay.gtms.api.registrate;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import top.ialdaiaxiariyay.gtms.GTMS;

public class GTMSRegistrate extends GTRegistrate {

    protected GTMSRegistrate(String modId) {
        super(modId);
    }

    public static GTMSRegistrate REGISTRATE = new GTMSRegistrate(GTMS.MOD_ID);

}

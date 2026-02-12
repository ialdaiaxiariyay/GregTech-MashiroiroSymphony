package top.ialdaiaxiariyay.gtms.client;

import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.common.CommonProxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        super();
        init();
    }

    public static void init() {
        GTMS.LOGGER.info("ClientProxy is Load");
    }
}

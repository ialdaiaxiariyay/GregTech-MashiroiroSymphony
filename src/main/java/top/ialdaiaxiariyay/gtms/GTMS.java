package top.ialdaiaxiariyay.gtms;

import top.ialdaiaxiariyay.gtms.client.ClientProxy;
import top.ialdaiaxiariyay.gtms.common.CommonProxy;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.resources.ResourceLocation.tryBuild;

@Mod(GTMS.MOD_ID)
public class GTMS {

    public static final String MOD_ID = "gtms";
    public static final String NAME = "GregTech:Mashiroiro Symphony";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static ResourceLocation id(String name) {
        return tryBuild(MOD_ID, name);
    }

    public GTMS() {
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        MinecraftForge.EVENT_BUS.register(this);
    }
}

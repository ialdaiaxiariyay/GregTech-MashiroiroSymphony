package top.ialdaiaxiariyay.gtbss;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.ialdaiaxiariyay.gtbss.client.ClientProxy;
import top.ialdaiaxiariyay.gtbss.common.CommonProxy;

import static net.minecraft.resources.ResourceLocation.tryBuild;

@Mod(GTBSS.MOD_ID)
public class GTBSS {

    public static final String MOD_ID = "gtbss";
    public static final String NAME = "GregTech:BlueSky Symphony";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static ResourceLocation id(String name) {
        return tryBuild(MOD_ID, name);
    }

    public GTBSS() {
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        MinecraftForge.EVENT_BUS.register(this);
    }


}

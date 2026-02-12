package top.ialdaiaxiariyay.gtms.common;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.misc.wireless.WirelessContainer;
import top.ialdaiaxiariyay.gtms.api.misc.wireless.WirelessData;

@Mod.EventBusSubscriber(modid = GTMS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventListener {

    @SubscribeEvent
    public static void serverSetup(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
            if (overworld == null) return;
            WirelessData.INSTANCE = WirelessData.getOrCreate(overworld);
            WirelessContainer.server = level.getServer();
        }
    }
}

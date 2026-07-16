package top.ialdaiaxiariyay.gtbss.network;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.network.packet.SanitySyncPacket;
import top.ialdaiaxiariyay.gtbss.network.packet.TeleportAnimationPacket;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            GTBSS.id("sync"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, SanitySyncPacket.class,
                SanitySyncPacket::encode,
                SanitySyncPacket::decode,
                SanitySyncPacket::handle);
        INSTANCE.registerMessage(id++, TeleportAnimationPacket.class,
                TeleportAnimationPacket::encode,
                TeleportAnimationPacket::decode,
                TeleportAnimationPacket::handle);
    }
}

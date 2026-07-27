package top.ialdaiaxiariyay.gtbss.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import top.ialdaiaxiariyay.gtbss.api.annotation.NetworkPacket;
import top.ialdaiaxiariyay.gtbss.client.hud.TeleportClientHandler;

import java.util.function.Supplier;

@NetworkPacket
public class TeleportAnimationPacket {

    private final boolean start;

    public TeleportAnimationPacket(boolean start) {
        this.start = start;
    }

    public static void encode(TeleportAnimationPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.start);
    }

    public static TeleportAnimationPacket decode(FriendlyByteBuf buf) {
        return new TeleportAnimationPacket(buf.readBoolean());
    }

    public static void handle(TeleportAnimationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.start) {
                TeleportClientHandler.startAnimation();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

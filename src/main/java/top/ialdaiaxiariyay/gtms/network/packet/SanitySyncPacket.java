package top.ialdaiaxiariyay.gtms.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.common.ForgeEventListener;

import java.util.function.Supplier;

public class SanitySyncPacket {

    private final int playerId;
    private final int newSanity;

    public SanitySyncPacket(int playerId, int newSanity) {
        this.playerId = playerId;
        this.newSanity = newSanity;
    }

    public static void encode(@NotNull SanitySyncPacket msg, @NotNull FriendlyByteBuf buf) {
        buf.writeInt(msg.playerId);
        buf.writeInt(msg.newSanity);
    }

    @Contract("_ -> new")
    public static @NotNull SanitySyncPacket decode(@NotNull FriendlyByteBuf buf) {
        return new SanitySyncPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(SanitySyncPacket msg, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = null;
            if (Minecraft.getInstance().level != null) {
                entity = Minecraft.getInstance().level.getEntity(msg.playerId);
            }
            if (entity instanceof Player player) {
                player.getCapability(ForgeEventListener.SANITY).ifPresent(sanity -> {
                    sanity.setSanity(msg.newSanity);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

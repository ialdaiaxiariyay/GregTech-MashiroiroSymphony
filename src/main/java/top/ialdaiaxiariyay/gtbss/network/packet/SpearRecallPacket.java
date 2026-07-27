package top.ialdaiaxiariyay.gtbss.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.api.annotation.NetworkPacket;
import top.ialdaiaxiariyay.gtbss.common.entity.SpearEntity;

import java.util.function.Supplier;

@NetworkPacket
public class SpearRecallPacket {

    public static void encode(SpearRecallPacket packet, FriendlyByteBuf buf) {}

    @Contract(value = "_ -> new", pure = true)
    @SuppressWarnings("InstantiationOfUtilityClass")
    public static @NotNull SpearRecallPacket decode(FriendlyByteBuf buf) {
        return new SpearRecallPacket();
    }

    public static void handle(SpearRecallPacket packet, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.isAlive()) return;

            SpearEntity target = null;
            double minDist = Double.MAX_VALUE;

            for (SpearEntity spear : player.level().getEntitiesOfClass(
                    SpearEntity.class,
                    player.getBoundingBox().inflate(100.0))) {
                if (spear.isReturning()) continue;
                if (spear.getOwner() != player) continue;
                double dist = player.distanceToSqr(spear);
                if (dist < minDist) {
                    minDist = dist;
                    target = spear;
                }
            }

            if (target != null) {
                int handIndex = player.getMainHandItem().isEmpty() ? 0 :
                        (player.getOffhandItem().isEmpty() ? 1 : 0);
                target.startReturning(handIndex);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

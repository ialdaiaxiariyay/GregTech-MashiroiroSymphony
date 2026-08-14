package top.ialdaiaxiariyay.gtbss.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.annotation.NetworkPacket;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1.0.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            GTBSS.id("sync"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static final Type ANNOTATION_TYPE = Type.getType(NetworkPacket.class);

    private static void protectedSend(PacketDistributor.PacketTarget target, Object packet) {
        try {
            INSTANCE.send(target, packet);
        } catch (Exception e) {
            GTBSS.LOGGER.warn("Failed to send packet {}: {}", packet.getClass().getSimpleName(),
                    e.getLocalizedMessage());
        }
    }

    public static void sendToServer(Object packet) {
        try {
            INSTANCE.sendToServer(packet);
        } catch (Exception e) {
            GTBSS.LOGGER.warn("Failed to send packet to server: {}", e.getLocalizedMessage());
        }
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        protectedSend(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToAll(Object packet) {
        protectedSend(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendToPlayersInLevel(ResourceKey<Level> level, Object packet) {
        protectedSend(PacketDistributor.DIMENSION.with(() -> level), packet);
    }

    public static void sendToPlayersNearPoint(PacketDistributor.TargetPoint point, Object packet) {
        protectedSend(PacketDistributor.NEAR.with(() -> point), packet);
    }

    public static void sendToAllPlayersTrackingEntity(Entity entity, boolean includeSelf, Object packet) {
        protectedSend(includeSelf ?
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity) :
                PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
    }

    public static void sendToAllPlayersTrackingChunk(LevelChunk chunk, Object packet) {
        protectedSend(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), packet);
    }

    public static void reply(net.minecraftforge.network.NetworkEvent.Context context, Object packet) {
        try {
            INSTANCE.reply(packet, context);
        } catch (Exception e) {
            GTBSS.LOGGER.warn("Failed to reply packet: {}", e.getLocalizedMessage());
        }
    }

    public static void register() {
        List<Map.Entry<String, Integer>> sorted = getEntries();
        sorted.sort((e1, e2) -> {
            int id1 = e1.getValue(), id2 = e2.getValue();
            if (id1 == -1 && id2 == -1) return e1.getKey().compareTo(e2.getKey());
            if (id1 == -1) return 1;
            if (id2 == -1) return -1;
            return Integer.compare(id1, id2);
        });

        int nextAutoId = 0;
        for (Map.Entry<String, Integer> entry : sorted) {
            String className = entry.getKey();
            int specifiedId = entry.getValue();
            int finalId = specifiedId == -1 ? nextAutoId++ : specifiedId;

            try {
                Class<?> clazz = Class.forName(className);
                Method encode = clazz.getMethod("encode", clazz, FriendlyByteBuf.class);
                Method decode = clazz.getMethod("decode", FriendlyByteBuf.class);
                Method handle = clazz.getMethod("handle", clazz, Supplier.class);

                NetworkDirection direction = getDirection(clazz);

                @SuppressWarnings("unchecked")
                Class<Object> msgClass = (Class<Object>) clazz;

                INSTANCE.registerMessage(
                        finalId,
                        msgClass,
                        (msg, buf) -> {
                            try {
                                encode.invoke(null, msg, buf);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to encode packet " + className, e);
                            }
                        },
                        buf -> {
                            try {
                                return decode.invoke(null, buf);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to decode packet " + className, e);
                            }
                        },
                        (msg, ctx) -> {
                            ctx.get().enqueueWork(() -> {
                                try {
                                    handle.invoke(null, msg, ctx);
                                } catch (Exception e) {
                                    GTBSS.LOGGER.error("Failed to handle packet {}: {}", className, e);
                                    throw new RuntimeException(e);
                                }
                            });
                            ctx.get().setPacketHandled(true);
                        },
                        Optional.ofNullable(direction));
            } catch (ReflectiveOperationException | LinkageError e) {
                GTBSS.LOGGER.error("Failed to register network packet: {}", className, e);
            }
        }
    }

    @Nullable
    private static NetworkDirection getDirection(Class<?> clazz) {
        return null;
    }

    private static @NotNull List<Map.Entry<String, Integer>> getEntries() {
        Map<String, Integer> classIdMap = new LinkedHashMap<>();
        for (ModFileScanData scanData : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData data : scanData.getAnnotations()) {
                if (data.annotationType().equals(ANNOTATION_TYPE)) {
                    String className = data.memberName();
                    int id = -1;
                    Map<String, Object> props = data.annotationData();
                    if (props.containsKey("id")) {
                        Object val = props.get("id");
                        if (val instanceof Integer) {
                            id = (Integer) val;
                        }
                    }
                    classIdMap.put(className, id);
                }
            }
        }
        return new ArrayList<>(classIdMap.entrySet());
    }

    public static SimpleChannel getChannel() {
        return INSTANCE;
    }
}

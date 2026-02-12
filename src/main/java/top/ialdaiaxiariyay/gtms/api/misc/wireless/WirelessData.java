package top.ialdaiaxiariyay.gtms.api.misc.wireless;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WirelessData extends SavedData {

    public static WirelessData INSTANCE;

    public static @NotNull WirelessData getOrCreate(@NotNull ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(
                WirelessData::new,
                WirelessData::new,
                "wireless_resources");
    }

    public final Map<UUID, Map<String, WirelessContainer>> containerMap = new HashMap<>();

    public WirelessData() {}

    public WirelessData(@NotNull CompoundTag tag) {
        ListTag allResources = tag.getList("allResources", Tag.TAG_COMPOUND);
        for (int i = 0; i < allResources.size(); i++) {
            CompoundTag ct = allResources.getCompound(i);
            UUID uuid = ct.getUUID("uuid");
            String type = ct.getString("type");
            BigInteger storage = new BigInteger(ct.getString("storage"));
            long rate = ct.getLong("rate");
            GlobalPos bindPos = readGlobalPos(ct.getString("dim"), ct.getLong("pos"));
            WirelessContainer container = new WirelessContainer(uuid, type, storage, rate, bindPos);
            containerMap.computeIfAbsent(uuid, k -> new HashMap<>()).put(type, container);
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
        ListTag allResources = new ListTag();
        for (Map.Entry<UUID, Map<String, WirelessContainer>> uuidEntry : containerMap.entrySet()) {
            UUID uuid = uuidEntry.getKey();
            for (Map.Entry<String, WirelessContainer> typeEntry : uuidEntry.getValue().entrySet()) {
                WirelessContainer container = typeEntry.getValue();
                CompoundTag ct = new CompoundTag();
                ct.putUUID("uuid", uuid);
                ct.putString("type", container.getResourceType());

                if (!container.getStorage().equals(BigInteger.ZERO)) {
                    ct.putString("storage", container.getStorage().toString());
                }
                if (container.getRate() != 0) {
                    ct.putLong("rate", container.getRate());
                }
                GlobalPos bindPos = container.getBindPos();
                if (bindPos != null) {
                    ct.putString("dim", bindPos.dimension().location().toString());
                    ct.putLong("pos", bindPos.pos().asLong());
                }
                allResources.add(ct);
            }
        }
        compoundTag.put("allResources", allResources);
        return compoundTag;
    }

    private static @Nullable GlobalPos readGlobalPos(@NotNull String dimension, long pos) {
        if (dimension.isEmpty() || pos == 0) return null;
        ResourceLocation key = ResourceLocation.tryParse(dimension);
        if (key == null) return null;
        return GlobalPos.of(ResourceKey.create(Registries.DIMENSION, key), BlockPos.of(pos));
    }
}
package top.ialdaiaxiariyay.gtms.api.misc.wireless;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.utils.BigIntegerUtils;
import top.ialdaiaxiariyay.gtms.utils.TeamUtil;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@Getter
public class WirelessContainer {

    public static boolean observed = false;
    public static final WeakHashMap<MetaMachine, ITransferData> TRANSFER_DATA = new WeakHashMap<>();
    public static MinecraftServer server;

    public static WirelessContainer getOrCreateContainer(UUID playerUUID, String resourceType) {
        UUID teamUUID = TeamUtil.getTeamUUID(playerUUID);
        Map<String, WirelessContainer> typeMap = WirelessData.INSTANCE.containerMap
                .computeIfAbsent(teamUUID, k -> new java.util.HashMap<>());
        return typeMap.computeIfAbsent(resourceType, t -> new WirelessContainer(teamUUID, t));
    }

    private BigInteger storage;
    private long rate;
    private GlobalPos bindPos;
    private final UUID uuid;
    private final String resourceType;
    private final ResourceStat stat;

    public WirelessContainer(UUID uuid, String resourceType, BigInteger storage, long rate, GlobalPos bindPos) {
        this.uuid = uuid;
        this.resourceType = resourceType;
        this.storage = storage;
        this.rate = rate;
        this.bindPos = bindPos;
        this.stat = new ResourceStat(resourceType, server != null ? server.getTickCount() : 0);
    }

    private WirelessContainer(UUID uuid, String resourceType) {
        this.uuid = uuid;
        this.resourceType = resourceType;
        this.storage = BigInteger.ZERO;
        this.rate = 0;
        this.bindPos = null;
        this.stat = new ResourceStat(resourceType, server != null ? server.getTickCount() : 0);
    }

    public long addResource(long amount, @Nullable MetaMachine machine, @NotNull String type) {
        if (!type.equals(this.resourceType))
            throw new IllegalArgumentException("Resource type mismatch: expected " + this.resourceType + ", got " + type);
        if (amount <= 0) return 0;
        storage = storage.add(BigInteger.valueOf(amount));
        WirelessData.INSTANCE.setDirty(true);
        if (machine != null) {
            stat.update(BigInteger.valueOf(amount), server.getTickCount());
        }
        if (observed && machine != null) {
            TRANSFER_DATA.put(machine, new BasicTransferData(uuid, resourceType, amount, machine));
        }
        return amount;
    }

    public long removeResource(long amount, @Nullable MetaMachine machine, String type) {
        if (!type.equals(this.resourceType))
            throw new IllegalArgumentException("Resource type mismatch: expected " + this.resourceType + ", got " + type);
        long change = Math.min(BigIntegerUtils.getLongValue(storage), amount);
        if (change <= 0) return 0;
        storage = storage.subtract(BigInteger.valueOf(change));
        WirelessData.INSTANCE.setDirty(true);
        if (machine != null) {
            stat.update(BigInteger.valueOf(change).negate(), server.getTickCount());
        }
        if (observed && machine != null) {
            TRANSFER_DATA.put(machine, new BasicTransferData(uuid, resourceType, -change, machine));
        }
        return change;
    }

    public void setStorage(BigInteger energy) {
        this.storage = energy;
        WirelessData.INSTANCE.setDirty(true);
    }

    public void setRate(long rate) {
        this.rate = rate;
        WirelessData.INSTANCE.setDirty(true);
    }

    public void setBindPos(GlobalPos bindPos) {
        this.bindPos = bindPos;
        WirelessData.INSTANCE.setDirty(true);
    }
}
package top.ialdaiaxiariyay.gtms.api.misc.wireless;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import java.util.UUID;

public record BasicTransferData(UUID teamUUID, String resourceType, long throughput, MetaMachine machine)
        implements ITransferData {
    @Override
    public UUID UUID() { return teamUUID; }
    @Override
    public String resourceType() { return resourceType; }
    @Override
    public long Throughput() { return throughput; }
    @Override
    public MetaMachine machine() { return machine; }
}
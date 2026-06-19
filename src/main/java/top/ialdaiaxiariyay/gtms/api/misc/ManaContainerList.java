package top.ialdaiaxiariyay.gtms.api.misc;

import net.minecraft.core.Direction;

import lombok.Getter;
import top.ialdaiaxiariyay.gtms.api.capability.IManaContainer;

import java.util.List;

public class ManaContainerList implements IManaContainer {

    private final List<? extends IManaContainer> containers;
    @Getter
    private final long inputPacketSize;
    @Getter
    private final long inputPacketCount;
    @Getter
    private final long outputPacketSize;
    @Getter
    private final long outputPacketCount;

    public ManaContainerList(List<? extends IManaContainer> containers) {
        this.containers = containers;
        long totalInput = 0, totalOutput = 0;
        long inputPackets = 0, outputPackets = 0;

        for (IManaContainer container : containers) {
            totalInput += container.getInputPacketSize() * container.getInputPacketCount();
            totalOutput += container.getOutputPacketSize() * container.getOutputPacketCount();
            inputPackets += container.getInputPacketCount();
            outputPackets += container.getOutputPacketCount();
        }

        long[] in = computePacketParams(totalInput, inputPackets);
        long[] out = computePacketParams(totalOutput, outputPackets);

        this.inputPacketSize = in[0];
        this.inputPacketCount = in[1];
        this.outputPacketSize = out[0];
        this.outputPacketCount = out[1];
    }

    private long[] computePacketParams(long totalMana, long totalPackets) {
        if (totalMana <= 0 || totalPackets <= 0) return new long[] { 0, 0 };
        long packetSize = totalMana / totalPackets;
        long packetCount = totalPackets;

        if (packetCount > 4) {
            if (packetCount % 4 == 0) {
                while (packetCount > 4) {
                    packetCount /= 4;
                    packetSize *= 4;
                }
            } else {
                packetSize = totalMana;
                packetCount = 1;
            }
        }
        return new long[] { packetSize, packetCount };
    }

    @Override
    public long acceptManaFromNetwork(Direction side, long manaPerPacket, long packetCount) {
        long accepted = 0;
        for (IManaContainer container : containers) {
            accepted += container.acceptManaFromNetwork(side, manaPerPacket, packetCount - accepted);
            if (accepted >= packetCount) break;
        }
        return accepted;
    }

    @Override
    public boolean inputsMana(Direction side) {
        return containers.stream().anyMatch(c -> c.inputsMana(side));
    }

    @Override
    public boolean outputsMana(Direction side) {
        return containers.stream().anyMatch(c -> c.outputsMana(side));
    }

    @Override
    public long changeMana(long differenceAmount) {
        long changed = 0;
        for (IManaContainer container : containers) {
            changed += container.changeMana(differenceAmount - changed);
            if (changed == differenceAmount) {
                return changed;
            }
        }
        return changed;
    }

    @Override
    public long getManaStored() {
        return containers.stream().mapToLong(IManaContainer::getManaStored).sum();
    }

    @Override
    public long getManaCapacity() {
        return containers.stream().mapToLong(IManaContainer::getManaCapacity).sum();
    }

    @Override
    public long getInputPerSec() {
        return containers.stream().mapToLong(IManaContainer::getInputPerSec).sum();
    }

    @Override
    public long getOutputPerSec() {
        return containers.stream().mapToLong(IManaContainer::getOutputPerSec).sum();
    }
}

package top.ialdaiaxiariyay.gtms.api.wireless;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import java.util.UUID;

public interface ITransferData {

    UUID UUID();

    String resourceType();

    long Throughput();

    MetaMachine machine();
}

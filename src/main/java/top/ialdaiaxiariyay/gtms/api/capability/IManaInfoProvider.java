package top.ialdaiaxiariyay.gtms.api.capability;

import java.math.BigInteger;

public interface IManaInfoProvider {

    record ManaInfo(BigInteger capacity, BigInteger stored) {}

    ManaInfo getManaInfo();

    long getInputPerSec();

    long getOutputPerSec();

    boolean supportsBigIntManaValues();

    default boolean isOneProbeHidden() {
        return true;
    }
}

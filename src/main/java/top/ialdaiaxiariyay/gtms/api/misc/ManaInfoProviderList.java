package top.ialdaiaxiariyay.gtms.api.misc;

import top.ialdaiaxiariyay.gtms.api.capability.IManaInfoProvider;

import java.math.BigInteger;
import java.util.List;

public class ManaInfoProviderList implements IManaInfoProvider {

    private final List<? extends IManaInfoProvider> providers;

    public ManaInfoProviderList(List<? extends IManaInfoProvider> providers) {
        this.providers = providers;
    }

    @Override
    public ManaInfo getManaInfo() {
        BigInteger capacity = BigInteger.ZERO;
        BigInteger stored = BigInteger.ZERO;
        for (IManaInfoProvider p : providers) {
            ManaInfo info = p.getManaInfo();
            capacity = capacity.add(info.capacity());
            stored = stored.add(info.stored());
        }
        return new ManaInfo(capacity, stored);
    }

    @Override
    public long getInputPerSec() {
        return providers.stream().mapToLong(IManaInfoProvider::getInputPerSec).sum();
    }

    @Override
    public long getOutputPerSec() {
        return providers.stream().mapToLong(IManaInfoProvider::getOutputPerSec).sum();
    }

    @Override
    public boolean supportsBigIntManaValues() {
        return providers.size() > 1;
    }
}

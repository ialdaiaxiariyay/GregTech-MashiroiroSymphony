package top.ialdaiaxiariyay.gtms.api.misc.wireless;

import javax.annotation.Nullable;

public interface IWirelessContainerHolder extends IBindable {

    @Nullable
    WirelessContainer getWirelessContainerCache(String resourceType);

    void setWirelessContainerCache(String resourceType, WirelessContainer container);


    @Nullable
    default WirelessContainer getWirelessContainer(String resourceType) {
        if (getUUID() == null) return null;
        WirelessContainer container = getWirelessContainerCache(resourceType);
        if (container == null) {
            container = WirelessContainer.getOrCreateContainer(getUUID(), resourceType);
            setWirelessContainerCache(resourceType, container);
        }
        return container;
    }
}
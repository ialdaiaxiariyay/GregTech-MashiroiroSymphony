package top.ialdaiaxiariyay.gtms.api.misc.wireless;

import javax.annotation.Nullable;

/**
 * 实现此接口的对象可持有多个无线容器（每种资源类型一个）。
 * 实现类应维护一个资源类型→容器的缓存映射。
 */
public interface IWirelessContainerHolder extends IBindable {

    /**
     * 获取指定资源类型的容器缓存（由实现类存储）
     */
    @Nullable
    WirelessContainer getWirelessContainerCache(String resourceType);

    /**
     * 设置指定资源类型的容器缓存
     */
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
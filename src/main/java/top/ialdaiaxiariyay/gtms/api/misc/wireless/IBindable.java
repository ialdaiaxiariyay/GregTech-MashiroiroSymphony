package top.ialdaiaxiariyay.gtms.api.misc.wireless;

import java.util.UUID;

import javax.annotation.Nullable;

public interface IBindable {

    @Nullable
    UUID getUUID();

    default boolean cover() {
        return false;
    }

    default boolean display() {
        return true;
    }
}

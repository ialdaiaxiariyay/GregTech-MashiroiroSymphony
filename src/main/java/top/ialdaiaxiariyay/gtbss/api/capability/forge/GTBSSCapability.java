package top.ialdaiaxiariyay.gtbss.api.capability.forge;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.api.capability.IManaContainer;
import top.ialdaiaxiariyay.gtbss.api.capability.IManaInfoProvider;

public class GTBSSCapability {

    public static final Capability<IManaContainer> CAPABILITY_MANA_CONTAINER = CapabilityManager
            .get(new CapabilityToken<>() {});

    public static final Capability<IManaInfoProvider> CAPABILITY_MANA_INFO_PROVIDER = CapabilityManager
            .get(new CapabilityToken<>() {});

    public static void register(@NotNull RegisterCapabilitiesEvent event) {
        event.register(IManaContainer.class);
        event.register(IManaInfoProvider.class);
    }
}

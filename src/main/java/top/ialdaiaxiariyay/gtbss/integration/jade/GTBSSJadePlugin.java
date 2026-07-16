package top.ialdaiaxiariyay.gtbss.integration.jade;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import top.ialdaiaxiariyay.gtbss.integration.jade.provider.ManaContainerProvider;

@WailaPlugin
public class GTBSSJadePlugin implements IWailaPlugin {

    @Override
    public void register(@NotNull IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new ManaContainerProvider(), BlockEntity.class);
    }

    @Override
    public void registerClient(@NotNull IWailaClientRegistration registration) {
        registration.registerBlockComponent(new ManaContainerProvider(), Block.class);
    }
}

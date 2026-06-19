package top.ialdaiaxiariyay.gtms.api.player;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.common.ForgeEventListener;

public class SanityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private final PlayerSanity sanity = new PlayerSanity();
    private final LazyOptional<PlayerSanity> holder = LazyOptional.of(() -> sanity);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        return ForgeEventListener.SANITY.orEmpty(cap, holder);
    }

    @Override
    public CompoundTag serializeNBT() {
        return sanity.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        sanity.deserializeNBT(tag);
    }
}

package top.ialdaiaxiariyay.gtms.api.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.api.capability.forge.GTMSCapability;

public class GTMSCapabilityHelper {

    @Nullable
    public static IManaContainer getManaContainer(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTMSCapability.CAPABILITY_MANA_CONTAINER, level, pos, side);
    }

    @Nullable
    public static IManaInfoProvider getManaInfoProvider(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTMSCapability.CAPABILITY_MANA_INFO_PROVIDER, level, pos, side);
    }

    @Nullable
    private static <T> T getBlockEntityCapability(Capability<T> capability, Level level, BlockPos pos,
                                                  @Nullable Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(capability, side).resolve().orElse(null);
            }
        }
        return null;
    }
}

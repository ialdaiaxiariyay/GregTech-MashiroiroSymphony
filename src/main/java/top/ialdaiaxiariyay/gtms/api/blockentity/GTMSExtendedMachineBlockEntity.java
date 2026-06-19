package top.ialdaiaxiariyay.gtms.api.blockentity;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.api.capability.IManaContainer;
import top.ialdaiaxiariyay.gtms.api.capability.IManaInfoProvider;
import top.ialdaiaxiariyay.gtms.api.capability.forge.GTMSCapability;
import top.ialdaiaxiariyay.gtms.api.misc.ManaContainerList;
import top.ialdaiaxiariyay.gtms.api.misc.ManaInfoProviderList;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.SparkAttachable;

import java.util.ArrayList;
import java.util.List;

public class GTMSExtendedMachineBlockEntity extends MetaMachineBlockEntity {

    public GTMSExtendedMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        LazyOptional<T> superResult = super.getCapability(cap, side);
        if (superResult.isPresent()) {
            return superResult;
        }

        MetaMachine machine = getMetaMachine();

        if (cap == GTMSCapability.CAPABILITY_MANA_CONTAINER) {
            if (machine instanceof IManaContainer container) {
                return GTMSCapability.CAPABILITY_MANA_CONTAINER.orEmpty(cap, LazyOptional.of(() -> container));
            }
            List<IManaContainer> containers = collectTraits(machine, side, IManaContainer.class);
            if (!containers.isEmpty()) {
                if (containers.size() == 1) {
                    return GTMSCapability.CAPABILITY_MANA_CONTAINER.orEmpty(cap,
                            LazyOptional.of(() -> containers.get(0)));
                } else {
                    return GTMSCapability.CAPABILITY_MANA_CONTAINER.orEmpty(cap,
                            LazyOptional.of(() -> new ManaContainerList(containers)));
                }
            }
        }

        if (cap == GTMSCapability.CAPABILITY_MANA_INFO_PROVIDER) {
            if (machine instanceof IManaInfoProvider provider) {
                return GTMSCapability.CAPABILITY_MANA_INFO_PROVIDER.orEmpty(cap, LazyOptional.of(() -> provider));
            }
            List<IManaInfoProvider> providers = collectTraits(machine, side, IManaInfoProvider.class);
            if (!providers.isEmpty()) {
                if (providers.size() == 1) {
                    return GTMSCapability.CAPABILITY_MANA_INFO_PROVIDER.orEmpty(cap,
                            LazyOptional.of(() -> providers.get(0)));
                } else {
                    return GTMSCapability.CAPABILITY_MANA_INFO_PROVIDER.orEmpty(cap,
                            LazyOptional.of(() -> new ManaInfoProviderList(providers)));
                }
            }
        }

        if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) {
            if (machine instanceof ManaReceiver receiver) {
                return BotaniaForgeCapabilities.MANA_RECEIVER.orEmpty(cap, LazyOptional.of(() -> receiver));
            }
        }

        if (cap == BotaniaForgeCapabilities.SPARK_ATTACHABLE) {
            if (machine instanceof SparkAttachable sparkAttachable) {
                return BotaniaForgeCapabilities.SPARK_ATTACHABLE.orEmpty(cap, LazyOptional.of(() -> sparkAttachable));
            }
        }

        return LazyOptional.empty();
    }

    private <T> @NotNull List<T> collectTraits(@NotNull MetaMachine machine, @Nullable Direction side,
                                               Class<T> capabilityClass) {
        List<T> list = new ArrayList<>();
        for (MachineTrait trait : machine.getTraits()) {
            if (trait.hasCapability(side) && capabilityClass.isInstance(trait)) {
                list.add(capabilityClass.cast(trait));
            }
        }
        return list;
    }
}

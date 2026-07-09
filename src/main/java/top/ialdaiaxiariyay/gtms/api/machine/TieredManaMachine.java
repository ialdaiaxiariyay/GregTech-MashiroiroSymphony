package top.ialdaiaxiariyay.gtms.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.trait.EnvironmentalExplosionTrait;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.api.capability.*;
import top.ialdaiaxiariyay.gtms.api.capability.forge.GTMSCapability;
import top.ialdaiaxiariyay.gtms.api.machine.trait.NotifiableManaContainer;
import top.ialdaiaxiariyay.gtms.api.misc.ManaContainerList;
import top.ialdaiaxiariyay.gtms.api.misc.ManaInfoProviderList;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.SparkAttachable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TieredManaMachine extends TieredMachine {

    @SaveField
    @SyncToClient
    public final NotifiableManaContainer manaContainer;

    @Getter
    protected final EnvironmentalExplosionTrait environmentalExplosionTrait;

    public TieredManaMachine(BlockEntityCreationInfo info, int tier,
                             NotifiableManaContainer manaContainer) {
        super(info, tier);
        this.manaContainer = attachTrait(manaContainer);
        environmentalExplosionTrait = attachTrait(new EnvironmentalExplosionTrait(tier, tier * 10,
                () -> manaContainer.getManaStored() > 0));
    }

    public TieredManaMachine(BlockEntityCreationInfo info, int tier, boolean emitsMana) {
        this(info, tier,
                emitsMana ? NotifiableManaContainer.emitterContainer(GTValues.V[tier] * 64L, GTValues.V[tier], 1) :
                        NotifiableManaContainer.receiverContainer(GTValues.V[tier] * 64L, GTValues.V[tier], 1));
    }

    @Override
    public int getAnalogOutputSignal() {
        long manaStored = manaContainer.getManaStored();
        long manaCapacity = manaContainer.getManaCapacity();
        float f = manaCapacity == 0L ? 0.0f : manaStored / (manaCapacity * 1.0f);
        return Mth.floor(f * 14.0f) + (manaStored > 0 ? 1 : 0);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == GTMSCapability.CAPABILITY_MANA_CONTAINER) {
            if (this.manaContainer != null) {
                return GTMSCapability.CAPABILITY_MANA_CONTAINER.orEmpty(cap,
                        LazyOptional.of(() -> this.manaContainer));
            }
            List<IManaContainer> containers = getCapabilitiesFromTraits(this.getAllTraits(), side,
                    IManaContainer.class);
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
            if (this.manaContainer != null) {
                return GTMSCapability.CAPABILITY_MANA_INFO_PROVIDER.orEmpty(cap,
                        LazyOptional.of(() -> this.manaContainer));
            }
            List<IManaInfoProvider> providers = getCapabilitiesFromTraits(this.getAllTraits(), side,
                    IManaInfoProvider.class);
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
            if (this instanceof ManaReceiver) {
                return BotaniaForgeCapabilities.MANA_RECEIVER.orEmpty(cap,
                        LazyOptional.of(() -> (ManaReceiver) this));
            }
            List<ManaReceiver> receivers = getCapabilitiesFromTraits(this.getAllTraits(), side, ManaReceiver.class);
            if (!receivers.isEmpty()) {
                return BotaniaForgeCapabilities.MANA_RECEIVER.orEmpty(cap,
                        LazyOptional.of(() -> receivers.get(0)));
            }
        }

        if (cap == BotaniaForgeCapabilities.SPARK_ATTACHABLE) {
            if (this instanceof SparkAttachable) {
                return BotaniaForgeCapabilities.SPARK_ATTACHABLE.orEmpty(cap,
                        LazyOptional.of(() -> (SparkAttachable) this));
            }
            List<SparkAttachable> attachables = getCapabilitiesFromTraits(this.getAllTraits(), side,
                    SparkAttachable.class);
            if (!attachables.isEmpty()) {
                return BotaniaForgeCapabilities.SPARK_ATTACHABLE.orEmpty(cap,
                        LazyOptional.of(() -> attachables.get(0)));
            }
        }

        return super.getCapability(cap, side);
    }

    private static <T> List<T> getCapabilitiesFromTraits(List<MachineTrait> traits, @Nullable Direction accessSide,
                                                         Class<T> capability) {
        if (traits.isEmpty()) return Collections.emptyList();
        List<T> list = new ArrayList<>();
        for (MachineTrait trait : traits) {
            if (trait.hasCapability(accessSide) && capability.isInstance(trait)) {
                list.add(capability.cast(trait));
            }
        }
        return list;
    }
}

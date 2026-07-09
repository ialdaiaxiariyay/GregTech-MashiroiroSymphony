package top.ialdaiaxiariyay.gtms.common.machine.multiblock.part.mana;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.machine.trait.EnvironmentalExplosionTrait;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.api.capability.IManaContainer;
import top.ialdaiaxiariyay.gtms.api.capability.IManaInfoProvider;
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
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ManaHatchPartMachine extends TieredIOPartMachine implements ManaReceiver {

    @SaveField
    public final NotifiableManaContainer manaContainer;
    @Getter
    protected int packetCount;

    protected TickableSubscription transferSub;

    public ManaHatchPartMachine(BlockEntityCreationInfo info, int tier, IO io, int packetCount) {
        super(info, tier, io);
        this.packetCount = packetCount;
        this.manaContainer = attachTrait(createManaContainer());
        attachTrait(new EnvironmentalExplosionTrait(tier, tier * 10, () -> manaContainer.getManaStored() > 0));
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected NotifiableManaContainer createManaContainer() {
        NotifiableManaContainer container;
        if (io == IO.OUT) {
            container = NotifiableManaContainer.emitterContainer(GTValues.V[tier] * 64L * packetCount,
                    GTValues.V[tier], packetCount);
            container.setSideOutputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
            container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        } else {
            container = NotifiableManaContainer.receiverContainer(GTValues.V[tier] * 16L * packetCount,
                    GTValues.V[tier], packetCount);
            container.setSideInputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
            container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        }
        return container;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            transferSub = subscribeServerTick(this::tryTransferToAdjacent);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (transferSub != null) {
            transferSub.unsubscribe();
            transferSub = null;
        }
    }


    //////////////////////////////////////
    // ********** Misc **********//
    //////////////////////////////////////

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
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
            return BotaniaForgeCapabilities.MANA_RECEIVER.orEmpty(cap,
                    LazyOptional.of(() -> this));
        }

        if (cap == BotaniaForgeCapabilities.SPARK_ATTACHABLE) {
            if (this instanceof SparkAttachable) {
                return BotaniaForgeCapabilities.SPARK_ATTACHABLE.orEmpty(cap,
                        LazyOptional.of(() -> (SparkAttachable) this));
            }
            List<SparkAttachable> attachable = getCapabilitiesFromTraits(this.getAllTraits(), side,
                    SparkAttachable.class);
            if (!attachable.isEmpty()) {
                return BotaniaForgeCapabilities.SPARK_ATTACHABLE.orEmpty(cap,
                        LazyOptional.of(() -> attachable.get(0)));
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

    public static long getPacketSizeForTier(int tier) {
        return GTValues.V[tier];
    }

    public long getMaxManaPerTick() {
        return getPacketSizeForTier(tier) * packetCount;
    }

    private void tryTransferToAdjacent() {
        if (io != IO.OUT || !isWorkingEnabled()) return;
        Level level = getLevel();
        if (level == null) return;
        BlockPos frontPos = getBlockPos().relative(getFrontFacing());
        var te = level.getBlockEntity(frontPos);
        if (te instanceof ManaReceiver receiver && receiver.canReceiveManaFromBursts()) {
            if (receiver.isFull()) return;
            long maxTransfer = getMaxManaPerTick();
            int toTransfer = (int) Math.min(manaContainer.getManaStored(), maxTransfer);
            if (toTransfer <= 0) return;
            receiver.receiveMana(toTransfer);
            manaContainer.removeMana(toTransfer);
        }
    }

    @Override
    public Level getManaReceiverLevel() {
        return Objects.requireNonNull(getLevel());
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        return (int) Math.min(manaContainer.getManaStored(), Integer.MAX_VALUE);
    }

    @Override
    public boolean isFull() {
        return manaContainer.getManaStored() >= manaContainer.getManaCapacity();
    }

    @Override
    public void receiveMana(int mana) {
        if (mana > 0 && io == IO.IN) {
            manaContainer.addMana(mana);
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return io == IO.IN && isWorkingEnabled();
    }
}

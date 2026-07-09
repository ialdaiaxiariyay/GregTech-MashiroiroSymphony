package top.ialdaiaxiariyay.gtms.common.machine.multiblock.part.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableLaserContainer;

import org.jetbrains.annotations.NotNull;

public class WirelessLaserHatchPartMachine extends WirelessEnergyHatchPartMachine {

    public WirelessLaserHatchPartMachine(BlockEntityCreationInfo info, int tier, IO io, int amperage) {
        super(info, tier, io, amperage);
    }

    @Override
    protected @NotNull NotifiableEnergyContainer createEnergyContainer() {
        NotifiableLaserContainer buffer;
        if (io == IO.OUT) {
            buffer = NotifiableLaserContainer.emitterContainer(GTValues.V[tier] * 64L * amperage,
                    GTValues.V[tier], amperage);
            buffer.setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            buffer = NotifiableLaserContainer.receiverContainer(GTValues.V[tier] * 64L * amperage,
                    GTValues.V[tier], amperage);
            buffer.setSideInputCondition(s -> s == getFrontFacing());
        }
        return buffer;
    }
}

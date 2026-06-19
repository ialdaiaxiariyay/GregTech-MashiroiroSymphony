package top.ialdaiaxiariyay.gtms.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

import top.ialdaiaxiariyay.gtms.api.capability.recipe.ManaRecipeCapability;
import top.ialdaiaxiariyay.gtms.api.recipe.ingredient.ManaStack;

public class RecipeAmperageManaContainer extends NotifiableManaContainer {

    protected final IRecipeLogicMachine recipeMachine;

    public RecipeAmperageManaContainer(IRecipeLogicMachine machine,
                                       long manaCapacity,
                                       long inputPacketSize, long inputPacketCount,
                                       long outputPacketSize, long outputPacketCount) {
        super(machine.self(), manaCapacity, inputPacketSize, inputPacketCount, outputPacketSize, outputPacketCount);
        this.recipeMachine = machine;
    }

    public static RecipeAmperageManaContainer makeEmitterContainer(IRecipeLogicMachine machine,
                                                                   long manaCapacity,
                                                                   long outputPacketSize,
                                                                   long outputPacketCount) {
        return new RecipeAmperageManaContainer(machine, manaCapacity, 0L, 0L, outputPacketSize, outputPacketCount);
    }

    public static RecipeAmperageManaContainer makeReceiverContainer(IRecipeLogicMachine machine,
                                                                    long manaCapacity,
                                                                    long inputPacketSize,
                                                                    long inputPacketCount) {
        return new RecipeAmperageManaContainer(machine, manaCapacity, inputPacketSize, inputPacketCount, 0L, 0L);
    }

    @Override
    public long getInputPacketCount() {
        var lastRecipe = recipeMachine.getRecipeLogic().getLastRecipe();
        long baseCount = super.getInputPacketCount();
        if (lastRecipe != null) {
            var contents = lastRecipe.getInputContents(ManaRecipeCapability.CAP);
            long requiredMana = 0;
            for (var content : contents) {
                if (content.content instanceof ManaStack stack) {
                    requiredMana += stack.getTotalMana();
                }
            }
            long packetSize = getInputPacketSize();
            if (packetSize > 0 && requiredMana > 0) {
                baseCount = (requiredMana + packetSize - 1) / packetSize;
            }
        }
        if (getManaCapacity() / 2 > getManaStored() && recipeMachine.getRecipeLogic().isActive()) {
            return baseCount + 1;
        }
        return baseCount;
    }

    @Override
    public long getOutputPacketCount() {
        var lastRecipe = recipeMachine.getRecipeLogic().getLastRecipe();
        long baseCount = super.getOutputPacketCount();
        if (lastRecipe != null) {
            var contents = lastRecipe.getOutputContents(ManaRecipeCapability.CAP);
            long producedMana = 0;
            for (var content : contents) {
                if (content.content instanceof ManaStack stack) {
                    producedMana += stack.getTotalMana();
                }
            }
            long packetSize = getOutputPacketSize();
            if (packetSize > 0 && producedMana > 0) {
                baseCount = (producedMana + packetSize - 1) / packetSize;
            }
        }
        return baseCount;
    }

    @Override
    public long getInputPacketSize() {
        return super.getInputPacketSize();
    }

    @Override
    public long getOutputPacketSize() {
        return super.getOutputPacketSize();
    }
}

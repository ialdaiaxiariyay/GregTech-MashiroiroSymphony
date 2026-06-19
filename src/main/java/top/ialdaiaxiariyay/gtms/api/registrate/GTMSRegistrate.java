package top.ialdaiaxiariyay.gtms.api.registrate;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;

import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.blockentity.GTMSExtendedMachineBlockEntity;

import java.util.function.Function;

public class GTMSRegistrate extends GTRegistrate {

    protected GTMSRegistrate(String modId) {
        super(modId);
    }

    public MachineBuilder<MachineDefinition, ?> extMachine(String name,
                                                           Function<IMachineBlockEntity, MetaMachine> metaMachine) {
        return new MachineBuilder<>(this, name, MachineDefinition::new, metaMachine,
                MetaMachineBlock::new, MetaMachineItem::new, GTMSExtendedMachineBlockEntity::new);
    }

    public MultiblockMachineBuilder<MultiblockMachineDefinition, ?> extMultiblock(String name,
                                                                                  Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine) {
        return new MultiblockMachineBuilder<>(this, name, metaMachine,
                MetaMachineBlock::new, MetaMachineItem::new, GTMSExtendedMachineBlockEntity::new);
    }

    public static GTMSRegistrate REGISTRATE = new GTMSRegistrate(GTMS.MOD_ID);
}

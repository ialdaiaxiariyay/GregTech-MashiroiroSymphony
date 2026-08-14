package top.ialdaiaxiariyay.gtbss.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtbss.api.machine.trait.recipe.GTBSSMultipleRecipesLogic;
import top.ialdaiaxiariyay.gtbss.common.machine.multiblock.part.MultipleRecipeParallelHatchPartMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MultipleRecipeWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    public final List<MultiblockPartMachine> parts = new ArrayList<>();

    @Getter
    @SyncToClient
    protected BlockPos[] partPositions = new BlockPos[0];

    private @Nullable MultipleRecipeParallelHatchPartMachine multipleRecipeParallelHatch = null;

    public MultipleRecipeWorkableElectricMultiblockMachine(BlockEntityCreationInfo info) {
        super(info, new GTBSSMultipleRecipesLogic());
    }

    public @NotNull Optional<MultipleRecipeParallelHatchPartMachine> getMultipleRecipeParallelHatch() {
        return Optional.ofNullable(multipleRecipeParallelHatch);
    }

    @Override
    protected void updatePartPositions() {
        super.updatePartPositions();

        this.parts.clear();
        this.parts.addAll(getParts());

        this.partPositions = this.parts.stream()
                .map(BlockEntity::getBlockPos)
                .toArray(BlockPos[]::new);

        syncDataHolder.markClientSyncFieldDirty("partPositions");
    }

    @Override
    @ClientFieldChangeListener(fieldName = "partPositions")
    protected void onPartsUpdated() {
        super.onPartsUpdated();
        this.parts.clear();
        for (BlockPos pos : this.partPositions) {
            if (getMachine(getLevel(), pos) instanceof MultiblockPartMachine part) {
                this.parts.add(part);
            }
        }
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        if (DEFAULT_STRUCTURE.equals(substructureName) && isFormed()) {
            for (MultiblockPartMachine part : getParts()) {
                if (part instanceof MultipleRecipeParallelHatchPartMachine pHatch) {
                    this.multipleRecipeParallelHatch = pHatch;
                    break;
                }
            }
        }
        updatePartPositions();
    }

    @Override
    public void invalidateStructure(@NotNull String name) {
        super.invalidateStructure(name);

        if (DEFAULT_STRUCTURE.equals(name)) {
            this.multipleRecipeParallelHatch = null;
        }
        updatePartPositions();
    }

    public List<MultiblockPartMachine> getOwnParts() {
        return this.parts;
    }
}

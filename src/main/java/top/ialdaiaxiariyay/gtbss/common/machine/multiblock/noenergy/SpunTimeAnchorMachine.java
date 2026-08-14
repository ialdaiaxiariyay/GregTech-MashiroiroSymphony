package top.ialdaiaxiariyay.gtbss.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.network.PacketDistributor;

import lombok.Setter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.bettergtae.api.recipe.CustomRecipeLogic;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.common.data.GTBSSDimension;
import top.ialdaiaxiariyay.gtbss.network.NetworkHandler;
import top.ialdaiaxiariyay.gtbss.network.packet.TeleportAnimationPacket;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class SpunTimeAnchorMachine extends WorkableMultiblockMachine {

    private int teleportCooldown = 0;
    private static final int COOLDOWN_TICKS = 20 * 4;
    private static final ResourceKey<Level> TARGET_DIM = ResourceKey.create(Registries.DIMENSION,
            GTBSS.id(GTBSSDimension.TheDarkroom));
    private static final BlockPos TARGET_POS = new BlockPos(0, 132, 0);

    @SaveField
    @SyncToClient
    private final NotifiableItemStackHandler inventory;
    @Nullable
    private ISubscription inventorySubs;

    public SpunTimeAnchorMachine(BlockEntityCreationInfo info) {
        super(info, new SpunTimeAnchorRecipeLogic());
        if (getRecipeLogic() instanceof SpunTimeAnchorRecipeLogic custom) {
            custom.setRecipeSupplier(this::getGTRecipe);
        }
        this.inventory = createInventory();
        attachTrait(inventory);
    }

    protected NotifiableItemStackHandler createInventory() {
        return new NotifiableItemStackHandler(1, IO.BOTH) {

            @Override
            public void onContentsChanged() {
                super.onContentsChanged();
                recipeLogic.updateTickSubscription();
            }
        };
    }

    public static class SpunTimeAnchorRecipeLogic extends CustomRecipeLogic {

        public SpunTimeAnchorRecipeLogic() {
            super();
        }

        @Setter
        private Supplier<GTRecipe> recipeSupplier;

        @Override
        public void onRecipeFinish() {
            IRecipeLogicMachine machine = getRLMachine();
            machine.afterWorking();
            if (lastRecipe != null) {
                handleRecipeIO(lastRecipe, IO.OUT);
            }
            if (suspendAfterFinish) {
                setStatus(Status.SUSPEND);
                suspendAfterFinish = false;
            } else {
                if (RecipeHelper.matchRecipe(machine, lastRecipe).isSuccess()) {
                    if (lastRecipe != null) {
                        setupRecipe(lastRecipe);
                    }
                    return;
                } else {
                    GTRecipe match = recipeSupplier.get();
                    if (match != null) {
                        setupRecipe(match);
                        return;
                    }
                }
                setStatus(Status.IDLE);
            }
            progress = 0;
            duration = 0;
            isActive = false;
        }

        @Override
        public void serverTick() {
            super.serverTick();
            if (getMachine() instanceof SpunTimeAnchorMachine spunTimeAnchorMachine) {
                spunTimeAnchorMachine.checkTeleport();
            }
        }
    }

    private @NotNull GTRecipe getGTRecipe() {
        return GTRecipeBuilder.ofRaw().notConsumable(Items.AIR).duration(20 * 5).buildRawRecipe();
    }

    public void checkTeleport() {
        if (teleportCooldown > 0) {
            teleportCooldown--;
            return;
        }
        if (!isFormed()) return;

        Level level = getLevel();
        if (level == null || level.isClientSide) return;

        BlockPos abovePos = getBlockPos();
        Player player = level.getNearestPlayer(abovePos.getX() + 0.5, abovePos.getY(), abovePos.getZ() + 0.5, 1.2,
                false);
        if (player != null && player.onGround() && !player.isPassenger()) {
            structurePlacement(player);
            performTeleport(player);
            teleportCooldown = COOLDOWN_TICKS;
        }
    }

    private void structurePlacement(@NotNull Player player) {
        ServerLevel targetWorld = Objects.requireNonNull(player.getServer()).getLevel(TARGET_DIM);
        if (targetWorld == null) {
            GTBSS.LOGGER.error("Target dimension {} does not exist!", TARGET_DIM.location());
            return;
        }

        BlockPos spawnPos = new BlockPos(-31, 128, -31);
        targetWorld.getChunk(spawnPos);
        StructureTemplateManager structureManager = targetWorld.getStructureManager();
        Optional<StructureTemplate> template = structureManager.get(GTBSS.id("glow_altar"));
        if (template.isEmpty()) {
            GTBSS.LOGGER.error("Structure glow_altar not found!");
            return;
        }

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(Rotation.NONE)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false);
        template.get().placeInWorld(targetWorld, spawnPos, spawnPos, settings, targetWorld.random, 2);
    }

    private void performTeleport(@NotNull Player player) {
        if (player.level().isClientSide) return;

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new TeleportAnimationPacket(true));
        }

        ServerLevel targetWorld = Objects.requireNonNull(player.getServer()).getLevel(TARGET_DIM);
        if (targetWorld == null) return;
        targetWorld.getChunk(TARGET_POS);

        ITeleporter teleporter = new ITeleporter() {

            @Override
            public @NotNull Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld,
                                               float yaw, @NotNull Function<Boolean, Entity> repositionEntity) {
                Entity relocated = repositionEntity.apply(false);
                relocated.teleportTo(TARGET_POS.getX() + 0.5, TARGET_POS.getY(), TARGET_POS.getZ() + 0.5);
                return relocated;
            }
        };

        player.changeDimension(targetWorld, teleporter);
        player.playSound(SoundEvents.PORTAL_TRAVEL, 1.0F, 1.0F);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        inventorySubs = inventory.addChangedListener(recipeLogic::updateTickSubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (inventorySubs != null) {
            inventorySubs.unsubscribe();
            inventorySubs = null;
        }
    }

    @Override
    @MustBeInvokedByOverriders
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        recipeLogic.updateTickSubscription();
    }
}

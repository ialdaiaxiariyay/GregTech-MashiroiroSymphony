package top.ialdaiaxiariyay.gtms.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.network.PacketDistributor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.bettergtae.api.recipe.CustomRecipeLogic;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.common.data.GTMSDimension;
import top.ialdaiaxiariyay.gtms.network.NetworkHandler;
import top.ialdaiaxiariyay.gtms.network.packet.TeleportAnimationPacket;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class SpunTimeAnchorMachine extends WorkableMultiblockMachine implements IMachineLife, IFancyUIMachine {

    private int teleportCooldown = 0;
    private static final int COOLDOWN_TICKS = 20 * 4;
    private static final ResourceKey<Level> TARGET_DIM = ResourceKey.create(Registries.DIMENSION,
            GTMS.id(GTMSDimension.TheDarkroom));
    private static final BlockPos TARGET_POS = new BlockPos(0, 132, 0);

    @Persisted
    private final NotifiableItemStackHandler inventory;
    @Nullable
    private ISubscription inventorySubs;

    public SpunTimeAnchorMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.inventory = createInventory();
        attachTraits(inventory);
    }

    protected NotifiableItemStackHandler createInventory() {
        return new NotifiableItemStackHandler(this, 1, IO.BOTH) {

            @Override
            public void onContentsChanged() {
                super.onContentsChanged();
                if (recipeLogic != null) {
                    recipeLogic.updateTickSubscription();
                }
            }
        };
    }

    public static class SpunTimeAnchorRecipeLogic extends CustomRecipeLogic {

        private final SpunTimeAnchorMachine machine;

        private final Supplier<GTRecipe> recipeSupplier;

        public SpunTimeAnchorRecipeLogic(SpunTimeAnchorMachine machine, Supplier<GTRecipe> recipeSupplier) {
            super(machine, recipeSupplier);
            this.machine = machine;
            this.recipeSupplier = recipeSupplier;
        }

        @Override
        public void onRecipeFinish() {
            machine.afterWorking();
            if (lastRecipe != null) {
                handleRecipeIO(lastRecipe, IO.OUT);
            }
            if (suspendAfterFinish) {
                setStatus(Status.SUSPEND);
                suspendAfterFinish = false;
            } else {
                if (RecipeHelper.matchRecipe(machine, lastRecipe).isSuccess()) {
                    setupRecipe(lastRecipe);
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
            machine.checkTeleport();
        }
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new SpunTimeAnchorRecipeLogic(this, this::getGTRecipe);
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

        BlockPos abovePos = getPos().above();
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
            GTMS.LOGGER.error("Target dimension {} does not exist!", TARGET_DIM.location());
            return;
        }

        BlockPos spawnPos = new BlockPos(-31, 128, -31);
        targetWorld.getChunk(spawnPos);
        StructureTemplateManager structureManager = targetWorld.getStructureManager();
        Optional<StructureTemplate> template = structureManager.get(GTMS.id("glow_altar"));
        if (template.isEmpty()) {
            GTMS.LOGGER.error("Structure glow_altar not found!");
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
        player.playSound(net.minecraft.sounds.SoundEvents.PORTAL_TRAVEL, 1.0F, 1.0F);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        inventorySubs = inventory.addChangedListener(() -> {
            if (recipeLogic != null) {
                recipeLogic.updateTickSubscription();
            }
        });
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
    public void onStructureFormed() {
        super.onStructureFormed();
        if (recipeLogic != null) {
            recipeLogic.updateTickSubscription();
        }
    }
}

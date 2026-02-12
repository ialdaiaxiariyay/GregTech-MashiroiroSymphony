package top.ialdaiaxiariyay.gtms.common.machine.steam;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import top.ialdaiaxiariyay.bettergtae.utils.NumberUtil;
import top.ialdaiaxiariyay.gtms.api.misc.wireless.IWirelessContainerHolder;
import top.ialdaiaxiariyay.gtms.api.misc.wireless.WirelessContainer;
import top.ialdaiaxiariyay.gtms.api.misc.wireless.WirelessType;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessSteamHatchPartMachine extends FluidHatchPartMachine implements IFancyUIMachine, IWirelessContainerHolder {

    public static final boolean IS_STEEL = ConfigHolder.INSTANCE.machines.steelSteamMultiblocks;
    public static final int STEAM_TRANSFER_RATE = 10000;
    private static final String RESOURCE_TYPE = WirelessType.STEAM;

    private final Map<String, WirelessContainer> wirelessContainerCache = new HashMap<>();

    @Nullable
    @Override
    public WirelessContainer getWirelessContainerCache(String resourceType) {
        return wirelessContainerCache.get(resourceType);
    }

    @Override
    public void setWirelessContainerCache(String resourceType, WirelessContainer container) {
        wirelessContainerCache.put(resourceType, container);
    }

    @Override
    public UUID getUUID() {
        if (getOwnerUUID() != null) {
            return getOwnerUUID();
        }
        return null;
    }

    private TickableSubscription updSteamFluidSubs;

    public WirelessSteamHatchPartMachine(IMachineBlockEntity holder, IO io, Object... args) {
        super(holder, 0, io, 64000*100, 1, args);
    }

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WirelessSteamHatchPartMachine.class, FluidHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        return super.createTank(initialCapacity, slots)
                .setFilter(fluidStack -> fluidStack.getFluid().is(GTMaterials.Steam.getFluidTag()));
    }

    // ========== GUI ==========
    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(IS_STEEL))
                .widget(new ImageWidget(7, 16, 81, 55, GuiTextures.DISPLAY_STEAM.get(IS_STEEL)))
                .widget(new LabelWidget(11, 20, "gtceu.gui.fluid_amount"))
                .widget(new LabelWidget(11, 30, () -> String.valueOf(tank.getFluidInTank(0).getAmount()))
                        .setTextColor(-1).setDropShadow(true))
                .widget(new LabelWidget(11, 40, "gtms.gui.wireless_steam"))
                .widget(new LabelWidget(11, 50, this::getTeamSteamDisplay)
                        .setTextColor(-1).setDropShadow(true))
                .widget(new LabelWidget(6, 6, getBlockState().getBlock().getDescriptionId()))
                .widget(new TankWidget(tank.getStorages()[0], 90, 35, true, true)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(IS_STEEL), 7, 84, true));
    }

    private String getTeamSteamDisplay() {
        WirelessContainer container = getWirelessContainer(RESOURCE_TYPE);
        return container == null ? "§cError" : NumberUtil.formatLong(container.getStorage().longValue());
    }

    @Override
    public void onLoad() {
        super.onLoad();

        updateSteamSubscription();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (updSteamFluidSubs != null) {
            updSteamFluidSubs.unsubscribe();
            updSteamFluidSubs = null;
        }
    }

    private void updateSteamSubscription() {
        updSteamFluidSubs = subscribeServerTick(updSteamFluidSubs, this::updateSteamTransfer);
    }

    private void updateSteamTransfer() {
        if (io == IO.IN) {
            useSteamFromNetwork();
        } else if (io == IO.OUT) {
            addSteamToNetwork();
        }
    }

    private void useSteamFromNetwork() {
        int stored = tank.getFluidInTank(0).getAmount();
        int capacity = tank.getTankCapacity(0);
        int space = capacity - stored;
        if (space <= 0) return;

        int toExtract = Math.min(STEAM_TRANSFER_RATE, space);

        WirelessContainer container = getWirelessContainer(RESOURCE_TYPE);
        if (container == null) return;
        long removed = container.removeResource(toExtract, this, RESOURCE_TYPE);
        if (removed <= 0) return;
        FluidStack steamStack = new FluidStack(GTMaterials.Steam.getFluid(), (int) removed);
        int filled = tank.fillInternal(steamStack, IFluidHandler.FluidAction.EXECUTE);
        if (filled < removed) {
            long notFilled = removed - filled;
            container.addResource(notFilled, this, RESOURCE_TYPE);
        }
    }

    private void addSteamToNetwork() {
        int stored = tank.getFluidInTank(0).getAmount();
        if (stored <= 0) return;

        int toDrain = Math.min(STEAM_TRANSFER_RATE, stored);
        FluidStack drained = tank.drainInternal(toDrain, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty() || drained.getAmount() <= 0) return;
        int drainedAmount = drained.getAmount();
        WirelessContainer container = getWirelessContainer(RESOURCE_TYPE);
        if (container != null) {
            container.addResource(drainedAmount, this, RESOURCE_TYPE);
        }
    }
}
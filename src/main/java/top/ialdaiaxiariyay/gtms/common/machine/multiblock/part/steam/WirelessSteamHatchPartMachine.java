package top.ialdaiaxiariyay.gtms.common.machine.multiblock.part.steam;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.TextWidget;
import top.ialdaiaxiariyay.bettergtae.utils.NumberUtil;
import top.ialdaiaxiariyay.gtms.api.wireless.IWirelessContainerHolder;
import top.ialdaiaxiariyay.gtms.api.wireless.WirelessContainer;
import top.ialdaiaxiariyay.gtms.api.wireless.WirelessType;

import java.util.UUID;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static top.ialdaiaxiariyay.gtms.api.wireless.WirelessType.STEAM;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessSteamHatchPartMachine extends FluidHatchPartMachine
                                           implements IWirelessContainerHolder {

    public static final int STEAM_TRANSFER_RATE = 10000;
    private static final String RESOURCE_TYPE = WirelessType.STEAM;

    @Nullable
    private WirelessContainer wirelessContainer;

    @Override
    @Nullable
    public WirelessContainer getWirelessContainerCache(String resourceType) {
        if (STEAM.equals(resourceType)) {
            return wirelessContainer;
        }
        return null;
    }

    @Override
    public void setWirelessContainerCache(String resourceType, @Nullable WirelessContainer container) {
        if (STEAM.equals(resourceType)) {
            this.wirelessContainer = container;
        }
    }

    @Override
    @Nullable
    public UUID getUUID() {
        return getOwnerUUID();
    }

    private TickableSubscription updSteamFluidSubs;

    public WirelessSteamHatchPartMachine(BlockEntityCreationInfo info, IO io) {
        super(info, 0, io, 64000 * 100, 1); // tier 0, capacity, slots = 1
    }

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots) {
        return super.createTank(initialCapacity, slots)
                .setFilter(fluidStack -> fluidStack.getFluid().is(GTMaterials.Steam.getFluidTag()));
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        super.buildMainUI(mainWidget, guiData, syncManager, settings);

        mainWidget.child(new TextWidget<>(Text.dynamic(this::getTeamSteamDisplay))
                .top(40)
                .left(10)
                .color(0xFFFFFFFF));
    }

    private Component getTeamSteamDisplay() {
        WirelessContainer container = getWirelessContainer(RESOURCE_TYPE);
        return container == null ? Component.literal("0") : Component.translatable("gtms.gui.wireless_steam",
                NumberUtil.formatLong(container.getStorage().longValue()));
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
        if (container == null) return;
        container.addResource(drainedAmount, this, RESOURCE_TYPE);
    }
}

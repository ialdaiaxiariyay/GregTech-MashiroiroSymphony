package top.ialdaiaxiariyay.gtbss.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.gui.AEKeyStorageSyncHandler;
import com.gregtechceu.gtceu.integration.ae2.gui.AEStackDisplayWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.ScrollPreservingGrid;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.DynamicLinkedSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.scroll.VerticalScrollData;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MEOutputDualPartMachine extends TieredIOPartMachine
                                     implements IGridConnectedMachine, IMuiMachine {

    @SaveField
    @Getter
    private final KeyStorage itemBuffer = new KeyStorage();

    @SaveField
    @Getter
    private final KeyStorage fluidBuffer = new KeyStorage();

    @Getter
    private final NotifiableItemStackHandler inventory;
    @Getter
    private final NotifiableFluidTank tank;

    @SaveField
    private final GridNodeHolder nodeHolder;

    @SyncToClient
    @Getter
    private boolean isOnline;

    protected final IActionSource actionSource;

    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription inventorySubs;
    @Nullable
    protected ISubscription tankSubs;

    public MEOutputDualPartMachine(BlockEntityCreationInfo info) {
        super(info, GTValues.UHV, IO.OUT);

        this.inventory = attachTrait(new InaccessibleItemHandler());
        this.tank = attachTrait(new InaccessibleFluidTank());

        itemBuffer.setOnContentsChanged(this::updateSubscriptions);
        fluidBuffer.setOnContentsChanged(this::updateSubscriptions);

        this.nodeHolder = attachTrait(new GridNodeHolder(this));
        this.actionSource = IActionSource.ofMachine(nodeHolder.getMainNode()::getNode);
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        IGridConnectedMachine.super.onMainNodeStateChanged(reason);
        updateSubscriptions();
    }

    public void setOnline(boolean online) {
        isOnline = online;
        syncDataHolder.markClientSyncFieldDirty("isOnline");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        scheduleForNextServerTick(this::updateSubscriptions);
        getMainNode().setExposedOnSides(EnumSet.of(getFrontFacing()));
        inventorySubs = inventory.addChangedListener(this::updateSubscriptions);
        tankSubs = tank.addChangedListener(this::updateSubscriptions);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (inventorySubs != null) {
            inventorySubs.unsubscribe();
            inventorySubs = null;
        }
        if (tankSubs != null) {
            tankSubs.unsubscribe();
            tankSubs = null;
        }
        flushAll();
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        flushAll();
    }

    protected boolean shouldSubscribe() {
        return isWorkingEnabled() && isOnline() && (!itemBuffer.isEmpty() || !fluidBuffer.isEmpty());
    }

    public void autoIO() {
        if (!this.shouldSyncME()) return;
        if (this.updateMEStatus()) {
            IGrid grid = getMainNode().getGrid();
            if (grid != null) {
                MEStorage network = grid.getStorageService().getInventory();
                if (!itemBuffer.isEmpty()) {
                    itemBuffer.insertInventory(network, actionSource);
                }
                if (!fluidBuffer.isEmpty()) {
                    fluidBuffer.insertInventory(network, actionSource);
                }
            }
            updateSubscriptions();
        }
    }

    protected void updateSubscriptions() {
        boolean shouldSubscribe = shouldSubscribe();
        if (shouldSubscribe) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    private void flushAll() {
        IGrid grid = getMainNode().getGrid();
        if (grid != null) {
            MEStorage network = grid.getStorageService().getInventory();
            if (!itemBuffer.isEmpty()) {
                itemBuffer.insertInventory(network, actionSource);
                itemBuffer.storage.clear();
            }
            if (!fluidBuffer.isEmpty()) {
                fluidBuffer.insertInventory(network, actionSource);
                fluidBuffer.storage.clear();
            }
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateSubscriptions();
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateSubscriptions();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        getMainNode().setExposedOnSides(EnumSet.of(newFacing));
        updateSubscriptions();
    }

    // ------------------------- GUI -------------------------
    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        BooleanSyncValue isOnlineValue = new BooleanSyncValue(this::isOnline, this::setOnline);
        syncManager.syncValue("is_online", isOnlineValue);

        var flow = Flow.col().coverChildren();

        flow.child(Text.dynamic(() -> isOnlineValue.getBoolValue() ?
                Component.translatable("gtceu.gui.me_network.online") :
                Component.translatable("gtceu.gui.me_network.offline"))
                .asWidget().marginTop(2).marginBottom(4));

        var itemSyncHandler = new AEKeyStorageSyncHandler(itemBuffer);
        syncManager.syncValue("item_output_display", itemSyncHandler);
        int[] itemScroll = { 0 };
        var itemDynamic = new DynamicLinkedSyncHandler<>(itemSyncHandler)
                .widgetProvider((sm, value) -> {
                    var col = Flow.col().leftRel(0.5f).coverChildrenHeight();
                    var list = value.getValue();
                    if (list.isEmpty()) return col.child(new TextWidget<>(Text.lang("gtceu.gui.waiting_list_empty")));
                    col.child(new TextWidget<>(Text.lang("gtceu.gui.waiting_list_items")).margin(0, 2));
                    col.child(new ScrollPreservingGrid(itemScroll)
                            .size(167, 80)
                            .scrollable(new VerticalScrollData())
                            .gridOfSizeWidth(9, 1, (x, y, index) -> new AEStackDisplayWidget(list, index)));
                    return col;
                });

        var fluidSyncHandler = new AEKeyStorageSyncHandler(fluidBuffer);
        syncManager.syncValue("fluid_output_display", fluidSyncHandler);
        int[] fluidScroll = { 0 };
        var fluidDynamic = new DynamicLinkedSyncHandler<>(fluidSyncHandler)
                .widgetProvider((sm, value) -> {
                    var col = Flow.col().leftRel(0.5f).coverChildrenHeight();
                    var list = value.getValue();
                    if (list.isEmpty()) return col.child(new TextWidget<>(Text.lang("gtceu.gui.waiting_list_empty")));
                    col.child(new TextWidget<>(Text.lang("gtceu.gui.waiting_list_fluids")).margin(0, 2));
                    col.child(new ScrollPreservingGrid(fluidScroll)
                            .size(167, 80)
                            .scrollable(new VerticalScrollData())
                            .gridOfSizeWidth(9, 1, (x, y, index) -> new AEStackDisplayWidget(list, index)));
                    return col;
                });

        var subFlow = Flow.row().coverChildren().childPadding(5);
        subFlow.child(new DynamicWidget<>()
                .syncHandler(itemDynamic)
                .size(167, 80));
        subFlow.child(new DynamicWidget<>()
                .syncHandler(fluidDynamic)
                .size(167, 80));

        flow.child(subFlow);

        mainWidget.child(flow);
    }

    private class InaccessibleItemHandler extends NotifiableItemStackHandler {

        public InaccessibleItemHandler() {
            super(1, IO.OUT, IO.NONE, ItemStackHandlerDelegate::new);
            ItemStackHandlerDelegate delegate = (ItemStackHandlerDelegate) storage;
            delegate.setKeyStorage(itemBuffer);
        }

        @Override
        public List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }
    }

    private static class ItemStackHandlerDelegate extends CustomItemStackHandler {

        @Getter
        @Setter
        @Nullable
        private KeyStorage keyStorage;

        public ItemStackHandlerDelegate() {
            super();
        }

        public ItemStackHandlerDelegate(Integer ignored) {
            super();
        }

        @Override
        public int getSlots() {
            return Short.MAX_VALUE;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {}

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (keyStorage == null) return stack;
            var key = AEItemKey.of(stack);
            if (key == null) return stack;
            long current = keyStorage.storage.getOrDefault(key, 0);
            long toAdd = Math.min(Long.MAX_VALUE - current, stack.getCount());
            if (toAdd > 0) {
                if (!simulate) {
                    keyStorage.storage.put(key, current + toAdd);
                    keyStorage.onChanged();
                }
                return stack.copyWithCount((int) (stack.getCount() - toAdd));
            }
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    }

    private class InaccessibleFluidTank extends NotifiableFluidTank {

        private final FluidStorageDelegate delegate;

        public InaccessibleFluidTank() {
            super(List.of(new FluidStorageDelegate()), IO.OUT, IO.NONE);
            this.delegate = (FluidStorageDelegate) getStorages()[0];
            this.delegate.setKeyStorage(fluidBuffer);
            allowSameFluids = true;
        }

        @Override
        public int getTanks() {
            return 128;
        }

        @Override
        public List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public void setFluidInTank(int tank, FluidStack fluidStack) {}

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                       boolean simulate) {
            if (io != IO.OUT) return left;
            FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
            for (var it = left.iterator(); it.hasNext();) {
                var ingredient = it.next();
                if (ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }
                FluidStack[] fluids;
                if (ingredient instanceof IntProviderFluidIngredient provider) {
                    if (provider.stacks != null) {
                        provider.stacks.clone();
                    }
                    provider.setSampledCount(-1);
                    fluids = simulate ? new FluidStack[] { provider.getMaxSizeStack() } : provider.getStacks();
                } else {
                    fluids = ingredient.getStacks();
                }
                if (fluids.length == 0 || fluids[0].isEmpty()) {
                    it.remove();
                    continue;
                }
                FluidStack output = fluids[0];
                int filled = delegate.fill(output, action);
                ingredient.shrink(filled);
                if (filled <= 0) it.remove();
            }
            return left;
        }
    }

    private static class FluidStorageDelegate extends CustomFluidTank {

        @Getter
        @Setter
        @Nullable
        private KeyStorage keyStorage;

        public FluidStorageDelegate() {
            super(0);
        }

        @Override
        public int getCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void setFluid(FluidStack fluid) {}

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (keyStorage == null) return 0;
            var key = AEFluidKey.of(resource.getFluid(), resource.getTag());
            long current = keyStorage.storage.getOrDefault(key, 0);
            long toAdd = Math.min(Long.MAX_VALUE - current, resource.getAmount());
            if (toAdd > 0 && action.execute()) {
                keyStorage.storage.put(key, current + toAdd);
                keyStorage.onChanged();
            }
            return (int) toAdd;
        }

        @Override
        public boolean supportsFill(int tank) {
            return super.supportsFill(tank);
        }

        @Override
        public boolean supportsDrain(int tank) {
            return false;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}

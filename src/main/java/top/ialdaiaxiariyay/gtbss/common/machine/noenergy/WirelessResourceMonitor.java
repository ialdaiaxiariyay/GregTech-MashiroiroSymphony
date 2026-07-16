package top.ialdaiaxiariyay.gtbss.common.machine.noenergy;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import brachy.modularui.api.MCHelper;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtbss.api.wireless.*;
import top.ialdaiaxiariyay.gtbss.utils.BigIntegerUtils;
import top.ialdaiaxiariyay.gtbss.utils.TeamUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

import static top.ialdaiaxiariyay.gtbss.utils.FormatUtil.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings({ "unused", "rawtypes" })
public class WirelessResourceMonitor extends MetaMachine implements IMuiMachine, IWirelessContainerHolder {

    @SaveField
    private boolean all;
    @SaveField
    private String selectedResourceType = WirelessType.ENERGY;
    @SaveField
    private String transferInfo = Component.translatable("gtbss.machine.wireless_resource_monitor.transfers")
            .getString();

    private final Map<String, WirelessContainer> wirelessContainerCache = new HashMap<>();
    private TickableSubscription refreshSubscription;

    private static final List<String> RESOURCE_TYPES = List.of(
            WirelessType.ENERGY,
            WirelessType.STEAM,
            WirelessType.MANA);
    private static final int UI_WIDTH = 180;
    private static final int UI_HEIGHT = 140;

    private BooleanSyncValue allSync;
    private StringSyncValue resourceSync;
    private BooleanSyncValue teleportTriggerSync;
    private StringSyncValue teleportDataSync;
    private StringSyncValue transferSync;

    public WirelessResourceMonitor(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            WirelessContainer.observed = true;
            refreshSubscription = subscribeServerTick(this::tickHandler);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (refreshSubscription != null) {
            refreshSubscription.unsubscribe();
            refreshSubscription = null;
        }
        transferSync = null;
    }

    private void tickHandler() {
        if (getOffsetTimer() % 10 == 0) {
            if (teleportTriggerSync != null && teleportTriggerSync.getBoolValue()) {
                teleportTriggerSync.setBoolValue(false);
                String data = teleportDataSync != null ? teleportDataSync.getValue() : null;
                if (data != null && !data.isEmpty()) {
                    executeTeleport(data);
                    teleportDataSync.setValue("");
                }
            }

            if (!isRemote()) {
                WirelessContainer container = getWirelessContainer(selectedResourceType);
                if (container != null) {
                    container.getStat().tick();
                }
            }

            if (!isRemote() && transferSync != null && transferSync.isValid()) {
                updateTransferInfo();
            }
        }
    }

    private void updateTransferInfo() {
        if (transferSync == null || !transferSync.isValid()) {
            return;
        }

        UUID teamUUID = TeamUtil.getTeamUUID(getUUID());
        if (teamUUID == null) {
            transferSync
                    .setValue(Component.translatable("gtbss.machine.wireless_resource_monitor.no_team").getString());
            return;
        }

        List<ITransferData> transfers = WirelessContainer.TRANSFER_DATA.values().stream()
                .filter(data -> (all || data.UUID().equals(teamUUID)) &&
                        data.resourceType().equals(selectedResourceType))
                .sorted(Comparator.comparingLong(ITransferData::Throughput))
                .toList();

        if (transfers.isEmpty()) {
            transferSync
                    .setValue(Component.translatable("gtbss.machine.wireless_resource_monitor.transfers").getString());
        } else {
            StringBuilder sb = new StringBuilder();
            for (ITransferData transfer : transfers) {
                String name = transfer.machine().getBlockState().getBlock().getName().getString();
                String throughput = (transfer.Throughput() > 0 ? "+" : "") + transfer.Throughput();
                sb.append(name).append(": ").append(throughput).append(" ").append(getCurrentUnit()).append("/t\n");
            }
            transferSync.setValue(sb.toString());
        }

        WirelessContainer.TRANSFER_DATA.clear();
    }

    // ---------- IWirelessContainerHolder ----------
    @Override
    @Nullable
    public UUID getUUID() {
        return getOwnerUUID();
    }

    @Override
    @Nullable
    public WirelessContainer getWirelessContainerCache(String resourceType) {
        return wirelessContainerCache.get(resourceType);
    }

    @Override
    public void setWirelessContainerCache(String resourceType, @Nullable WirelessContainer container) {
        wirelessContainerCache.put(resourceType, container);
    }

    private String getCurrentUnit() {
        return switch (selectedResourceType) {
            case WirelessType.ENERGY -> "EU";
            case WirelessType.STEAM -> "L";
            case WirelessType.MANA -> "Mana";
            default -> "unit";
        };
    }

    // ---------- IMuiMachine ----------
    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        allSync = syncManager.getOrCreateSyncHandler("allSync", BooleanSyncValue.class,
                () -> new BooleanSyncValue(() -> all, b -> all = b));
        allSync.allowC2S(true);

        resourceSync = syncManager.getOrCreateSyncHandler("resourceSync", StringSyncValue.class,
                () -> new StringSyncValue(() -> selectedResourceType, s -> selectedResourceType = s));
        resourceSync.allowC2S(true);

        teleportTriggerSync = syncManager.getOrCreateSyncHandler("teleportTrigger", BooleanSyncValue.class,
                () -> new BooleanSyncValue(() -> false, b -> {}));
        teleportTriggerSync.allowC2S(true);

        teleportDataSync = syncManager.getOrCreateSyncHandler("teleportData", StringSyncValue.class,
                () -> new StringSyncValue(() -> "", s -> {}));
        teleportDataSync.allowC2S(true);

        transferSync = syncManager.getOrCreateSyncHandler("transferSync", StringSyncValue.class,
                () -> new StringSyncValue(() -> transferInfo, s -> transferInfo = s));

        var panel = new ParentWidget<>()
                .size(UI_WIDTH, UI_HEIGHT)
                .background(GuiTextures.DISPLAY);

        ListWidget list = new ListWidget();
        list.width(UI_WIDTH - 6);
        list.height(UI_HEIGHT - 6);
        list.crossAxisAlignment(Alignment.CrossAxis.START);
        list.collapseDisabledChildren();

        List<IWidget> children = getWidgetsForDisplay(syncManager);
        // noinspection unchecked
        list.children(children);

        list.left(3);
        list.top(3);
        panel.child(list);

        mainWidget.child(panel.margin(4, 2));
    }

    // ---------- UI Content Generation ----------
    private List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();

        UUID teamUUID = TeamUtil.getTeamUUID(getUUID());
        String teamName = teamUUID != null ? TeamUtil.GetName(getLevel(), teamUUID).getString() : "?";

        Flow titleRow = Flow.row().coverChildren().childPadding(0);
        titleRow.child(Text.dynamic(() -> Component.translatable("gtbss.machine.wireless_resource_monitor.title",
                Component.literal(selectedResourceType.toUpperCase()).withStyle(ChatFormatting.YELLOW),
                Component.literal(teamName).withStyle(ChatFormatting.AQUA))).asWidget().maxWidth(UI_WIDTH - 6)
                .height(12));
        titleRow.height(12);
        widgets.add(titleRow);

        ButtonWidget<?> switchBtn = new ButtonWidget<>()
                .child(Text.dynamic(() -> Text.lang("gtbss.machine.wireless_resource_monitor.switch")
                        .append(Component.literal(selectedResourceType.toUpperCase()).withStyle(ChatFormatting.YELLOW)))
                        .asWidget())
                .onMousePressed((context, button) -> {
                    if (resourceSync != null) {
                        String current = resourceSync.getValue();
                        int idx = RESOURCE_TYPES.indexOf(current);
                        idx = (idx + 1) % RESOURCE_TYPES.size();
                        resourceSync.setValue(RESOURCE_TYPES.get(idx));
                    }
                    return true;
                })
                .height(14)
                .coverChildrenWidth()
                .background(IDrawable.EMPTY);
        Flow switchRow = Flow.row().coverChildren().childPadding(0);
        switchRow.child(switchBtn);
        switchRow.height(14);
        widgets.add(switchRow);

        if (teamUUID == null) {
            Flow noTeamRow = Flow.row().coverChildren().childPadding(0);
            noTeamRow.child(Text.lang("gtbss.machine.wireless_resource_monitor.no_team")
                    .withStyle(ChatFormatting.RED)
                    .asWidget()
                    .maxWidth(UI_WIDTH - 6)
                    .height(12));
            noTeamRow.height(12);
            widgets.add(noTeamRow);
            return widgets;
        }

        addDataRow(widgets, Text.dynamic(() -> {
            WirelessContainer c = getWirelessContainer(selectedResourceType);
            if (c == null) return Component.literal("N/A");
            return Component.translatable("gtbss.machine.wireless_resource_monitor.storage",
                    Component.literal(formatBigIntegerNumberOrSic(c.getStorage())).withStyle(ChatFormatting.GREEN));
        }).asWidget());

        addDataRow(widgets, Text.dynamic(() -> {
            WirelessContainer c = getWirelessContainer(selectedResourceType);
            if (c == null) return Component.literal("N/A");
            BigDecimal avgRate = c.getStat().getAvgRate();
            return Component.translatable("gtbss.machine.wireless_resource_monitor.net_rate",
                    Component.literal(formatBigDecimalNumberOrSic(avgRate))
                            .withStyle(avgRate.signum() >= 0 ? ChatFormatting.DARK_AQUA : ChatFormatting.RED));
        }).asWidget());

        addDataRow(widgets, Text.dynamic(() -> {
            WirelessContainer c = getWirelessContainer(selectedResourceType);
            if (c == null) return Component.literal("N/A");
            return Component.translatable("gtbss.machine.wireless_resource_monitor.last_minute",
                    Component.literal(formatBigDecimalNumberOrSic(c.getStat().getMinuteAvg()))
                            .withStyle(ChatFormatting.DARK_AQUA));
        }).asWidget());

        addDataRow(widgets, Text.dynamic(() -> {
            WirelessContainer c = getWirelessContainer(selectedResourceType);
            if (c == null) return Component.literal("N/A");
            return Component.translatable("gtbss.machine.wireless_resource_monitor.last_hour",
                    Component.literal(formatBigDecimalNumberOrSic(c.getStat().getHourAvg()))
                            .withStyle(ChatFormatting.YELLOW));
        }).asWidget());

        addDataRow(widgets, Text.dynamic(() -> {
            WirelessContainer c = getWirelessContainer(selectedResourceType);
            if (c == null) return Component.literal("N/A");
            return Component.translatable("gtbss.machine.wireless_resource_monitor.last_day",
                    Component.literal(formatBigDecimalNumberOrSic(c.getStat().getDayAvg()))
                            .withStyle(ChatFormatting.DARK_GREEN));
        }).asWidget());

        addDataRow(widgets, Text.dynamic(() -> {
            WirelessContainer c = getWirelessContainer(selectedResourceType);
            if (c == null) return Component.empty();
            BigDecimal avgRate = c.getStat().getAvgRate();
            if (avgRate.compareTo(BigDecimal.ZERO) != 0 && c.getCapacity() != null) {
                BigInteger storage = c.getStorage();
                BigInteger timeTicks;
                if (avgRate.signum() > 0) {
                    BigInteger remain = c.getCapacity().subtract(storage);
                    if (remain.signum() > 0) {
                        timeTicks = remain.divide(avgRate.toBigInteger());
                        return Component.translatable("gtceu.multiblock.power_substation.time_to_fill",
                                getTimeText(timeTicks)).withStyle(ChatFormatting.GRAY);
                    }
                } else {
                    timeTicks = storage.divide(avgRate.abs().toBigInteger());
                    return Component.translatable("gtceu.multiblock.power_substation.time_to_drain",
                            getTimeText(timeTicks)).withStyle(ChatFormatting.GRAY);
                }
            }
            return Component.empty();
        }).asWidget());

        ButtonWidget<?> toggleShowBtn = new ButtonWidget<>()
                .child(Text.dynamic(() -> Text.lang("gtbss.machine.wireless_resource_monitor.show")
                        .append(Component.literal(" "))
                        .append(Component.literal(all ? "All" : "Team Only").withStyle(ChatFormatting.AQUA)))
                        .asWidget())
                .onMousePressed((context, button) -> {
                    if (allSync != null) {
                        allSync.setBoolValue(!allSync.getBoolValue());
                    }
                    return true;
                })
                .height(14)
                .coverChildrenWidth()
                .background(IDrawable.EMPTY);
        Flow toggleRow = Flow.row().coverChildren().childPadding(0);
        toggleRow.child(toggleShowBtn);
        toggleRow.height(14);
        widgets.add(toggleRow);

        Flow emptyRow = Flow.row().coverChildren().childPadding(0);
        emptyRow.height(4);
        widgets.add(emptyRow);

        Flow transferTitleRow = Flow.row().coverChildren().childPadding(0);
        transferTitleRow.child(Text.lang("gtbss.machine.wireless_resource_monitor.recent_transfers")
                .withStyle(ChatFormatting.UNDERLINE)
                .asWidget()
                .maxWidth(UI_WIDTH - 6)
                .height(12));
        transferTitleRow.height(12);
        widgets.add(transferTitleRow);

        Flow spacerRow = Flow.row().coverChildren().childPadding(0);
        spacerRow.height(5);
        widgets.add(spacerRow);

        Flow transferRow = Flow.row().coverChildren().childPadding(0);
        int lineCount = Math.max(1, transferInfo.split("\n").length);
        int rowHeight = Math.max(12, lineCount * 12);
        TextWidget<?> transferText = Text.dynamic(() -> Component.literal(transferInfo))
                .asWidget()
                .maxWidth(UI_WIDTH - 6)
                .height(rowHeight);
        transferRow.child(transferText);
        transferRow.height(rowHeight);
        widgets.add(transferRow);

        return widgets;
    }

    private void addDataRow(List<IWidget> widgets, TextWidget<?> textWidget) {
        textWidget.maxWidth(UI_WIDTH - 6).height(12);
        Flow row = Flow.row().coverChildren().childPadding(0);
        row.child(textWidget);
        row.height(12);
        widgets.add(row);
    }

    private void executeTeleport(String teleportData) {
        if (getLevel() == null) return;
        Player player = MCHelper.getPlayer();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        int lastColon = teleportData.lastIndexOf(':');
        if (lastColon == -1) return;
        String dimStr = teleportData.substring(0, lastColon);
        String[] xyz = teleportData.substring(lastColon + 1).split(",");
        if (xyz.length != 3) return;

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.parse(dimStr));
        ServerLevel targetWorld = serverPlayer.server.getLevel(dimKey);
        if (targetWorld != null) {
            try {
                double x = Double.parseDouble(xyz[0]) + 0.5;
                double y = Double.parseDouble(xyz[1]) + 0.5;
                double z = Double.parseDouble(xyz[2]) + 0.5;
                serverPlayer.teleportTo(targetWorld, x, y, z,
                        serverPlayer.getYRot(), serverPlayer.getXRot());
                serverPlayer.sendSystemMessage(
                        Component.translatable("gtbss.machine.wireless_resource_monitor.teleport_success",
                                xyz[0], xyz[1], xyz[2], dimStr)
                                .withStyle(ChatFormatting.GREEN));
            } catch (NumberFormatException ignored) {}
        }
    }

    private Component getTimeText(BigInteger ticks) {
        if (ticks.compareTo(BigIntegerUtils.BIG_INTEGER_MAX_LONG) > 0)
            ticks = BigIntegerUtils.BIG_INTEGER_MAX_LONG;
        long seconds = ticks.longValue() / 20;
        Duration duration = Duration.ofSeconds(seconds);
        long value;
        String key;
        if (duration.getSeconds() <= 180) {
            value = duration.getSeconds();
            key = "gtceu.multiblock.power_substation.time_seconds";
        } else if (duration.toMinutes() <= 180) {
            value = duration.toMinutes();
            key = "gtceu.multiblock.power_substation.time_minutes";
        } else if (duration.toHours() <= 72) {
            value = duration.toHours();
            key = "gtceu.multiblock.power_substation.time_hours";
        } else if (duration.toDays() <= 730) {
            value = duration.toDays();
            key = "gtceu.multiblock.power_substation.time_days";
        } else if (duration.toDays() / 365 < 1_000_000) {
            value = duration.toDays() / 365;
            key = "gtceu.multiblock.power_substation.time_years";
        } else {
            return Component.translatable("gtceu.multiblock.power_substation.time_forever");
        }
        return Component.translatable(key, FormattingUtil.formatNumbers(value));
    }
}

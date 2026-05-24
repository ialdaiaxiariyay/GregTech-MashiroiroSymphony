package top.ialdaiaxiariyay.gtms.common.machine.noenergy;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.api.gui.AlignComponentPanelWidget;
import top.ialdaiaxiariyay.gtms.api.wireless.*;
import top.ialdaiaxiariyay.gtms.utils.BigIntegerUtils;
import top.ialdaiaxiariyay.gtms.utils.TeamUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

import static top.ialdaiaxiariyay.gtms.utils.FormatUtil.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessResourceMonitor extends MetaMachine implements IFancyUIMachine, IWirelessContainerHolder {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WirelessResourceMonitor.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    public WirelessResourceMonitor(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private final Map<String, WirelessContainer> wirelessContainerCache = new HashMap<>();

    private List<Component> textListCache;

    @Persisted
    private boolean all;

    @Persisted
    private String selectedResourceType = WirelessType.ENERGY;

    private static final List<String> RESOURCE_TYPES = List.of(
            WirelessType.ENERGY,
            WirelessType.STEAM,
            WirelessType.MANA);

    public void cycleResourceType() {
        int idx = RESOURCE_TYPES.indexOf(selectedResourceType);
        idx = (idx + 1) % RESOURCE_TYPES.size();
        selectedResourceType = RESOURCE_TYPES.get(idx);
        textListCache = null;
    }

    private String getCurrentUnit() {
        return switch (selectedResourceType) {
            case WirelessType.ENERGY -> "EU";
            case WirelessType.STEAM -> "L";
            case WirelessType.MANA -> "MP";
            default -> "unit";
        };
    }

    public static int DISPLAY_TEXT_WIDTH = 220;

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, DISPLAY_TEXT_WIDTH + 8 + 8, 117 + 8);

        var scrollable = new DraggableScrollableWidgetGroup(4, 4, DISPLAY_TEXT_WIDTH + 8, 117)
                .setBackground(GuiTextures.DISPLAY)
                .addWidget(new AlignComponentPanelWidget(4, 17, this::addDisplayText)
                        .setMaxWidthLimit(DISPLAY_TEXT_WIDTH)
                        .clickHandler(this::handleDisplayClick)
                        .setSplitChar("."));

        group.addWidget(scrollable);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    public void addDisplayText(@NotNull List<Component> textList) {
        if (textListCache == null || getOffsetTimer() % 10 == 0) {
            textListCache = getDisplayText(all, DISPLAY_TEXT_WIDTH);
        }
        textList.addAll(textListCache);
    }

    private void handleDisplayClick(String componentData, ClickData clickData) {
        if (componentData.equals("all")) {
            if (!clickData.isRemote) {
                all = !all;
            }
        } else if (componentData.equals("switch_resource")) {
            if (!clickData.isRemote) {
                cycleResourceType();
            }
        }
    }

    private List<Component> getDisplayText(boolean allTeams, int maxWidth) {
        List<Component> lines = new ArrayList<>();
        UUID teamUUID = TeamUtil.getTeamUUID(getUUID());
        if (teamUUID == null) {
            lines.add(Component.translatable("gtms.machine.wireless_resource_monitor.no_team"));
            return lines;
        }

        WirelessContainer container = getWirelessContainer(selectedResourceType);
        if (container == null) {
            lines.add(Component.translatable("gtms.machine.wireless_resource_monitor.no_data_for_type",
                    selectedResourceType));
            return lines;
        }

        String unit = getCurrentUnit();
        BigInteger storage = container.getStorage();
        ResourceStat stat = container.getStat();

        Component resourceButton = AlignComponentPanelWidget.withButton(
                Component.literal(selectedResourceType.toUpperCase()).withStyle(ChatFormatting.YELLOW,
                        ChatFormatting.BOLD),
                "switch_resource");
        lines.add(Component.translatable("gtms.machine.wireless_resource_monitor.title", resourceButton,
                TeamUtil.GetName(getLevel(), teamUUID))
                .withStyle(ChatFormatting.AQUA));

        lines.add(formatWithConstantWidth("gtms.machine.wireless_resource_monitor.storage",
                Component.literal(formatBigIntegerNumberOrSic(storage)).withStyle(ChatFormatting.GREEN)));

        BigDecimal avgRate = stat.getAvgRate();
        lines.add(formatWithConstantWidth("gtms.machine.wireless_resource_monitor.net_rate",
                Component.literal(formatBigDecimalNumberOrSic(avgRate))
                        .withStyle(avgRate.signum() >= 0 ? ChatFormatting.DARK_AQUA : ChatFormatting.RED)));

        BigDecimal avgMinute = stat.getMinuteAvg();
        BigDecimal avgHour = stat.getHourAvg();
        BigDecimal avgDay = stat.getDayAvg();
        lines.add(formatWithConstantWidth("gtms.machine.wireless_resource_monitor.last_minute",
                Component.literal(formatBigDecimalNumberOrSic(avgMinute)).withStyle(ChatFormatting.DARK_AQUA)));
        lines.add(formatWithConstantWidth("gtms.machine.wireless_resource_monitor.last_hour",
                Component.literal(formatBigDecimalNumberOrSic(avgHour)).withStyle(ChatFormatting.YELLOW)));
        lines.add(formatWithConstantWidth("gtms.machine.wireless_resource_monitor.last_day",
                Component.literal(formatBigDecimalNumberOrSic(avgDay)).withStyle(ChatFormatting.DARK_GREEN)));

        if (avgRate.compareTo(BigDecimal.ZERO) != 0 && container.getCapacity() != null) {
            BigInteger timeTicks;
            if (avgRate.signum() > 0) {
                BigInteger remain = container.getCapacity().subtract(storage);
                if (remain.signum() > 0) {
                    timeTicks = remain.divide(avgRate.toBigInteger());
                    lines.add(Component.translatable("gtceu.multiblock.power_substation.time_to_fill",
                            getTimeText(timeTicks)).withStyle(ChatFormatting.GRAY));
                }
            } else {
                timeTicks = storage.divide(avgRate.abs().toBigInteger());
                lines.add(Component.translatable("gtceu.multiblock.power_substation.time_to_drain",
                        getTimeText(timeTicks)).withStyle(ChatFormatting.GRAY));
            }
        }

        lines.add(Component.translatable("gtms.machine.wireless_resource_monitor.show")
                .append(AlignComponentPanelWidget.withButton(allTeams ?
                        Component.translatable("gtms.machine.wireless_resource_monitor.all") :
                        Component.translatable("gtms.machine.wireless_resource_monitor.team_only"), "all")));

        List<ITransferData> transfers = WirelessContainer.TRANSFER_DATA.values().stream()
                .filter(data -> (allTeams || data.UUID().equals(teamUUID)) &&
                        data.resourceType().equals(selectedResourceType))
                .sorted(Comparator.comparingLong(ITransferData::Throughput))
                .toList();

        if (!transfers.isEmpty()) {
            lines.add(Component.literal(""));
            lines.add(Component.translatable("gtms.machine.wireless_resource_monitor.recent_transfers")
                    .withStyle(ChatFormatting.UNDERLINE));
            for (ITransferData transfer : transfers) {
                lines.add(transfer.getInfo(unit));
            }
        }

        WirelessContainer.observed = true;
        WirelessContainer.TRANSFER_DATA.clear();

        return lines;
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

    @Override
    public @Nullable UUID getUUID() {
        return this.getOwnerUUID();
    }

    @Nullable
    @Override
    public WirelessContainer getWirelessContainerCache(String resourceType) {
        return wirelessContainerCache.get(resourceType);
    }

    @Override
    public void setWirelessContainerCache(String resourceType, WirelessContainer container) {
        wirelessContainerCache.put(resourceType, container);
    }
}

package top.ialdaiaxiariyay.gtms.api.misc.wireless;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import top.ialdaiaxiariyay.bettergtae.utils.NumberUtil;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static top.ialdaiaxiariyay.gtms.utils.TeamUtil.GetName;

public interface ITransferData {

    UUID UUID();
    String resourceType();
    long Throughput();
    MetaMachine machine();

    default Component getInfo(String resourceUnit) {
        MetaMachine machine = machine();
        long throughput = Throughput();
        String pos = machine.getPos().toShortString();
        return Component.translatable(machine.getBlockState().getBlock().getDescriptionId())
                .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("recipe.condition.dimension.tooltip",
                                        Objects.requireNonNull(machine.getLevel()).dimension().location())
                                .append(" [").append(pos).append("] ")
                                .append(Component.translatable("gtms.machine.wireless_resource_monitor.tooltip",
                                        GetName(machine.getLevel(), UUID()))))))
                .append((throughput > 0 ? " +" : " ") + NumberUtil.formatLong(BigDecimal.valueOf(throughput).longValue()))
                .append(" ").append(resourceUnit).append("/t")
                .append(ComponentPanelWidget.withButton(Component.literal(" [ ] "), pos));
    }
}
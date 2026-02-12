package top.ialdaiaxiariyay.gtms.api.misc.wireless;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import java.math.BigDecimal;
import java.util.UUID;

import static top.ialdaiaxiariyay.gtms.utils.FormatUtil.formatBigDecimalNumberOrSic;
import static top.ialdaiaxiariyay.gtms.utils.TeamUtil.GetName;

public interface ITransferData {

    UUID UUID();                // 队伍UUID
    String resourceType();      // 新增：资源类型
    long Throughput();          // 传输量
    MetaMachine machine();

    // 显示信息中增加资源类型，并保留原EU专用格式（可根据需要调整）
    default Component getInfo() {
        MetaMachine machine = machine();
        long eut = Throughput();
        String pos = machine.getPos().toShortString();
        String resourceDisplay = resourceType().equals("energy") ? "EU" : resourceType(); // 示例处理
        return Component.translatable(machine.getBlockState().getBlock().getDescriptionId())
                .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("recipe.condition.dimension.tooltip",
                                        machine.getLevel().dimension().location())
                                .append(" [").append(pos).append("] ")
                                .append(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0",
                                        GetName(machine.getLevel(), UUID()))))))
                .append((eut > 0 ? " +" : " ") + formatBigDecimalNumberOrSic(BigDecimal.valueOf(eut)))
                .append(" ").append(resourceDisplay).append("/t")   // 显示资源单位
                .append(ComponentPanelWidget.withButton(Component.literal(" [ ] "), pos));
    }
}
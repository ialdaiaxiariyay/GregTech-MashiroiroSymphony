package top.ialdaiaxiariyay.gtms.api.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.annotation.Nullable;

public abstract class MagicModuleItem extends Item implements IMagicModule {

    private List<Component> tooltipList = List.of();

    public MagicModuleItem(Properties properties) {
        super(properties);
    }

    public void setTooltips(List<Component> tooltips) {
        this.tooltipList = tooltips != null ? List.copyOf(tooltips) : List.of();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        tooltipComponents
                .add(Component.translatable("gtms.tooltip.magic_module.charge_time_modifier", getChargeTimeModifier()));
        tooltipComponents.add(Component.translatable("gtms.tooltip.magic_module.manacost", getManaCost()));
        tooltipComponents.add(Component.translatable("gtms.tooltip.magic_module.skill").withStyle(ChatFormatting.AQUA));
        tooltipComponents.addAll(tooltipList);
    }
}

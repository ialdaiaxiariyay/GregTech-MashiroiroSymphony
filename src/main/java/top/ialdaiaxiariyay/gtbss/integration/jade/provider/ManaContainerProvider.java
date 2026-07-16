package top.ialdaiaxiariyay.gtbss.integration.jade.provider;

import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.capability.GTBSSCapabilityHelper;
import top.ialdaiaxiariyay.gtbss.api.capability.IManaInfoProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class ManaContainerProvider extends CapabilityBlockProvider<IManaInfoProvider> {

    public ManaContainerProvider() {
        super(GTBSS.id("mana_container"));
    }

    @Override
    protected @Nullable IManaInfoProvider getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        return GTBSSCapabilityHelper.getManaInfoProvider(level, pos, side);
    }

    @Override
    protected void write(@NotNull CompoundTag data, @NotNull IManaInfoProvider capability) {
        data.putByteArray("Mana", capability.getManaInfo().stored().toByteArray());
        data.putByteArray("MaxMana", capability.getManaInfo().capacity().toByteArray());
    }

    @Override
    protected void addTooltip(@NotNull CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (!capData.contains("Mana") && !capData.contains("MaxMana")) return;

        var mana = new BigInteger(capData.getByteArray("Mana"));
        var maxMana = new BigInteger(capData.getByteArray("MaxMana"));
        if (maxMana.compareTo(BigInteger.ZERO) <= 0) return;
        var threshold = BigInteger.valueOf((long) 1e12);
        var manaStr = FormattingUtil.formatNumberOrSic(mana, threshold);
        var maxManaStr = FormattingUtil.formatNumberOrSic(maxMana, threshold);
        var progress = getProgress(mana, maxMana);

        var helper = tooltip.getElementHelper();

        tooltip.add(
                helper.progress(
                        progress,
                        Component.translatable("gtbss.jade.mana_stored", manaStr, maxManaStr),
                        helper.progressStyle().color(0xFF33AAFF, 0xFF33AAFF).textColor(-1),
                        Util.make(BoxStyle.DEFAULT, style -> style.borderColor = 0xFF555555),
                        true));
    }

    @Override
    protected boolean allowDisplaying(@NotNull IManaInfoProvider capability) {
        return capability.isOneProbeHidden();
    }

    protected float getProgress(BigInteger progress, @NotNull BigInteger maxProgress) {
        if (maxProgress.compareTo(BigInteger.ZERO) <= 0) return 0;
        return new BigDecimal(progress).divide(new BigDecimal(maxProgress), MathContext.DECIMAL32).floatValue();
    }
}

package top.ialdaiaxiariyay.gtms.mixin.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeLogicProvider;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import top.ialdaiaxiariyay.bettergtae.utils.NumberUtil;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.capability.recipe.ManaRecipeCapability;

@Mixin(RecipeLogicProvider.class)
public abstract class MixinRecipeLogicProvider {

    @Inject(method = "write(Lnet/minecraft/nbt/CompoundTag;Lcom/gregtechceu/gtceu/api/machine/trait/RecipeLogic;)V",
            at = @At("RETURN"),
            remap = false)
    private void injectWrite(CompoundTag data, RecipeLogic capability, CallbackInfo ci) {
        GTRecipe recipe = capability.getLastRecipe();
        if (recipe == null) return;

        long inputMana = 0;
        long outputMana = 0;

        for (Content content : recipe.getTickInputContents(ManaRecipeCapability.CAP)) {
            inputMana += ManaRecipeCapability.CAP.of(content.getContent()).amount();
        }
        for (Content content : recipe.getTickOutputContents(ManaRecipeCapability.CAP)) {
            outputMana += ManaRecipeCapability.CAP.of(content.getContent()).amount();
        }

        GTMS.LOGGER.info("Input Mana content size: {}, Output: {}", inputMana, outputMana);

        long netMana = inputMana - outputMana;
        if (netMana == 0) return;

        CompoundTag manaInfo = new CompoundTag();
        manaInfo.putLong("ManaPerTick", Math.abs(netMana));
        manaInfo.putLong("TotalMana", Math.abs(netMana) * recipe.duration);
        manaInfo.putBoolean("IsInput", netMana > 0);
        data.put("ManaInfo", manaInfo);
        GTMS.LOGGER.info("Wrote ManaInfo: perTick={}, total={}, isInput={}",
                Math.abs(netMana), Math.abs(netMana) * recipe.duration, netMana > 0);
    }

    @Inject(method = "addTooltip", at = @At("RETURN"), remap = false)
    private void injectAddTooltip(@NotNull CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                                  BlockEntity blockEntity, IPluginConfig config, CallbackInfo ci) {
        if (!capData.getBoolean("Working")) return;

        if (!capData.contains("ManaInfo")) return;
        CompoundTag manaInfo = capData.getCompound("ManaInfo");

        long manaPerTick = manaInfo.getLong("ManaPerTick");
        long totalMana = manaInfo.getLong("TotalMana");
        boolean isInput = manaInfo.getBoolean("IsInput");

        MutableComponent manaLine;
        if (isInput) {
            manaLine = Component.translatable("gtms.jade.mana_consumption",
                    NumberUtil.formatLong(manaPerTick));
        } else {
            manaLine = Component.translatable("gtms.jade.mana_production",
                    NumberUtil.formatLong(manaPerTick));
        }
        tooltip.add(manaLine);
        tooltip.add(Component.translatable("gtms.jade.mana_unit",
                NumberUtil.formatLong(totalMana)));
    }
}

package top.ialdaiaxiariyay.gtbss.common.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.common.item.SpearItem;

public class SpearFallProtectionEnchantment extends Enchantment {

    public SpearFallProtectionEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... slots) {
        super(rarity, category, slots);
    }

    @Override
    public boolean canApplyAtEnchantingTable(@NotNull ItemStack stack) {
        return stack.getItem() instanceof SpearItem;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }
}

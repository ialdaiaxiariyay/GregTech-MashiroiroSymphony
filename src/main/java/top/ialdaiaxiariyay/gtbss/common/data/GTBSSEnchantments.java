package top.ialdaiaxiariyay.gtbss.common.data;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import com.tterrag.registrate.util.entry.RegistryEntry;
import top.ialdaiaxiariyay.gtbss.api.registrate.GTBSSRegistrate;
import top.ialdaiaxiariyay.gtbss.common.enchantment.SpearFallProtectionEnchantment;

public class GTBSSEnchantments {

    public static void init() {}

    public static final RegistryEntry<SpearFallProtectionEnchantment> SPEAR_FALL_PROTECTION = GTBSSRegistrate.REGISTRATION
            .enchantment("spear_fall_protection", EnchantmentCategory.WEAPON, SpearFallProtectionEnchantment::new)
            .rarity(Enchantment.Rarity.RARE)
            .addSlots(EquipmentSlot.MAINHAND)
            .lang("Spear Fall Protection")
            .register();
}

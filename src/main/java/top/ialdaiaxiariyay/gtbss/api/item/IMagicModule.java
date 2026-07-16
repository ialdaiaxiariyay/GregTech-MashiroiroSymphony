package top.ialdaiaxiariyay.gtbss.api.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Interface for all magic modules that can be installed into a wand.
 * Each module must define its casting behavior and may optionally affect charge time.
 */
public interface IMagicModule {

    /**
     * Cast the module's effect.
     *
     * @param level      the world
     * @param player     the caster
     * @param wand       the wand item stack
     * @param chargeTime charge progress in [0.0, 1.0], where 1.0 means fully charged
     */
    void cast(Level level, Player player, ItemStack wand, float chargeTime);

    /**
     * Modifier for the wand's total charge time.
     * Negative values reduce charge time (faster), positive increase (slower).
     * Default 0.0 means no modification.
     *
     * @return a modifier to be added to the base charge multiplier (e.g. -0.6 reduces by 60%)
     */
    default float getChargeTimeModifier() {
        return 0.0f;
    }

    /**
     * Mana cost of this module when cast alone (ignored if a combination overrides).
     * Default 0 means no mana cost.
     *
     * @return mana points consumed per cast
     */
    default int getManaCost() {
        return 0;
    }
}

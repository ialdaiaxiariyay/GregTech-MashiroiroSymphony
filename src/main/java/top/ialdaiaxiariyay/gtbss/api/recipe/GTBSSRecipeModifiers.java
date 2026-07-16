package top.ialdaiaxiariyay.gtbss.api.recipe;

import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class GTBSSRecipeModifiers {

    public static final Function<Boolean, RecipeModifier> MANA_OVERCLOCK = Util
            .memoize(perfect -> (machine, recipe) -> {
                if (!(machine instanceof IOverclockMachine manaMachine)) {
                    return ModifierFunction.IDENTITY;
                }
                int currentTier = manaMachine.getMaxOverclockTier();
                int baseTier = ManaRecipeHelper.getTierFromMana(getBaseManaRequirement(recipe));
                if (currentTier < baseTier) {
                    return ModifierFunction.cancel(Component.translatable("gtbss.recipe_modifier.insufficient_mana"));
                }
                return r -> ManaRecipeHelper.createOverclockedRecipe(r, currentTier, baseTier, perfect);
            });

    public static final RecipeModifier MANA_OC_PERFECT = MANA_OVERCLOCK.apply(true);
    public static final RecipeModifier MANA_OC_NON_PERFECT = MANA_OVERCLOCK.apply(false);

    private static long getBaseManaRequirement(@NotNull GTRecipe recipe) {
        var manaWithIO = ManaRecipeHelper.getRealManaWithIO(recipe);
        return manaWithIO.stack().amount();
    }
}

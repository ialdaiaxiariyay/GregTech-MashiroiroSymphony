package top.ialdaiaxiariyay.gtms.api.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.api.recipe.ingredient.ManaStack;

import java.util.*;

import static top.ialdaiaxiariyay.gtms.api.capability.recipe.ManaRecipeCapability.CAP;

public class ManaRecipeHelper {

    private static void modifyManaContentsInMap(@NotNull Map<RecipeCapability<?>, List<Content>> map, double multiplier,
                                                int parallels) {
        List<Content> originalList = map.get(CAP);
        if (originalList == null || originalList.isEmpty()) return;

        List<Content> newList = new ArrayList<>(originalList.size());
        for (Content content : originalList) {
            if (content.content instanceof ManaStack stack) {
                long singleAmount = (long) Math.ceil((double) stack.amount() / parallels);
                long newSingle = (long) Math.ceil(singleAmount * multiplier);
                long newTotal = newSingle * parallels;
                ManaStack newStack = stack.withAmount(newTotal);
                newList.add(new Content(newStack, content.chance, content.maxChance, content.tierChanceBoost));
            } else {
                newList.add(content);
            }
        }
        map.put(CAP, newList);
    }

    public static void applyOverclockToRecipe(@NotNull GTRecipe recipe, double manaMultiplier,
                                              double durationMultiplier) {
        recipe.duration = (int) Math.max(1, recipe.duration * durationMultiplier);
        int parallels = recipe.parallels > 0 ? recipe.parallels : 1;
        modifyManaContentsInMap(recipe.inputs, manaMultiplier, parallels);
        modifyManaContentsInMap(recipe.outputs, manaMultiplier, parallels);
        modifyManaContentsInMap(recipe.tickInputs, manaMultiplier, parallels);
        modifyManaContentsInMap(recipe.tickOutputs, manaMultiplier, parallels);
    }

    public static ManaStack.WithIO getRealManaWithIO(@NotNull GTRecipe recipe) {
        long inputMana = recipe.getTickInputContents(CAP).stream()
                .mapToLong(c -> ((ManaStack) c.getContent()).amount()).sum();
        long outputMana = recipe.getTickOutputContents(CAP).stream()
                .mapToLong(c -> ((ManaStack) c.getContent()).amount()).sum();

        long totalMana;
        IO io;
        if (inputMana > 0) {
            totalMana = inputMana;
            io = IO.IN;
        } else if (outputMana > 0) {
            totalMana = outputMana;
            io = IO.OUT;
        } else {
            return ManaStack.WithIO.EMPTY;
        }
        int parallels = recipe.parallels > 0 ? recipe.parallels : 1;
        long singleMana = (long) Math.ceil((double) totalMana / parallels);
        return new ManaStack.WithIO(singleMana, io);
    }

    public static @NotNull GTRecipe createOverclockedRecipe(GTRecipe original, int currentTier, int baseTier,
                                                            boolean perfect) {
        if (currentTier <= baseTier) return original.copy();
        GTRecipe copy = original.copy();
        int ocLevel = currentTier - baseTier;

        int maxOC = 0;
        int tempDuration = original.duration;
        int divisor = perfect ? 4 : 2;
        while (tempDuration > 1) {
            tempDuration /= divisor;
            maxOC++;
        }
        int effectiveOC = Math.min(ocLevel, maxOC);

        double durationMul;
        double manaMul;
        if (perfect) {
            durationMul = Math.pow(0.25, effectiveOC);
        } else {
            durationMul = Math.pow(0.5, effectiveOC);
        }
        manaMul = Math.pow(4, effectiveOC);
        applyOverclockToRecipe(copy, manaMul, durationMul);
        return copy;
    }

    public static @NotNull GTRecipe createOverclockedRecipe(GTRecipe original, int currentTier, int baseTier) {
        return createOverclockedRecipe(original, currentTier, baseTier, false);
    }

    public static int getTierFromMana(long mana) {
        for (int i = 0; i < GTValues.VEX.length; i++) {
            if (mana <= GTValues.VEX[i]) {
                return i;
            }
        }
        return GTValues.VEX.length - 1;
    }
}

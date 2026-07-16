package top.ialdaiaxiariyay.gtbss.api.capability.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.capability.IManaContainer;
import top.ialdaiaxiariyay.gtbss.api.recipe.content.SerializerManaStack;
import top.ialdaiaxiariyay.gtbss.api.recipe.ingredient.ManaStack;

import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic.adjustMultiplier;

public class ManaRecipeCapability extends RecipeCapability<ManaStack> {

    public static final ManaRecipeCapability CAP = new ManaRecipeCapability();

    protected ManaRecipeCapability() {
        super(GTBSS.id("mana"), 0xFF4C9A2A, false, 2, SerializerManaStack.INSTANCE);
    }

    @Override
    public ManaStack copyInner(ManaStack content) {
        return content;
    }

    @Override
    public ManaStack copyWithModifier(@NotNull ManaStack content, @NotNull ContentModifier modifier) {
        return content.withAmount(modifier.apply(content.amount()));
    }

    /**
     * Creates a List<Content> with the specified Mana.
     */
    @Contract("_ -> new")
    public static @NotNull @Unmodifiable List<Content> makeManaContent(ManaStack mana) {
        return List.of(new Content(mana, ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue()));
    }

    /**
     * Puts a Mana singleton Content into the given content map.
     */
    public static void putManaContent(@NotNull Map<RecipeCapability<?>, List<Content>> contents, ManaStack mana) {
        contents.put(ManaRecipeCapability.CAP, makeManaContent(mana));
    }

    @Override
    public int limitMaxParallelByOutput(IRecipeCapabilityHolder holder, GTRecipe recipe, int multiplier, boolean tick) {
        if (holder instanceof ICustomParallel p) {
            return p.limitManaParallel(recipe, multiplier, tick);
        }
        List<Content> outputs = tick ? recipe.getTickOutputContents(this) : recipe.getOutputContents(this);
        if (outputs.isEmpty()) return multiplier;
        long recipeMana = outputs.stream().mapToLong(c -> ((ManaStack) c.content()).amount()).sum();
        if (recipeMana == 0) return multiplier;

        if (tick) {
            long maxOut = getMaxManaOutputPerTick(holder);
            if (maxOut <= 0) return 0;
            return Math.min(multiplier, (int) (maxOut / recipeMana));
        } else {
            if (!holder.hasCapabilityProxies()) return 0;
            var handlers = holder.getCapabilitiesFlat(IO.OUT, this);
            if (handlers.isEmpty()) return 0;

            int min = 0, max = multiplier;
            if (recipeMana > 0 && multiplier > Long.MAX_VALUE / recipeMana) {
                max = multiplier = (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE / recipeMana);
            }
            while (min != max) {
                long total = recipeMana * multiplier;
                List<Long> manaList = List.of(total);
                boolean failed = false;
                for (var handler : handlers) {
                    var result = handler.handleRecipe(IO.OUT, recipe, manaList, true);
                    if (result.isEmpty()) {
                        failed = true;
                        break;
                    }
                    manaList = (List<Long>) result;
                }
                int[] bin = adjustMultiplier(failed, min, multiplier, max);
                min = bin[0];
                multiplier = bin[1];
                max = bin[2];
            }
            return multiplier;
        }
    }

    @Override
    public int getMaxParallelByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int limit, boolean tick) {
        if (holder instanceof ICustomParallel p) {
            return p.getMaxManaParallel(recipe, limit, tick);
        }
        List<Content> inputs = tick ? recipe.getTickInputContents(this) : recipe.getInputContents(this);
        if (inputs.isEmpty()) return limit;
        long recipeMana = inputs.stream().mapToLong(c -> ((ManaStack) c.content()).amount()).sum();
        if (recipeMana == 0) return limit;

        if (tick) {
            long maxIn = getMaxManaInputPerTick(holder);
            if (maxIn <= 0) return 0;
            return Math.min(limit, (int) (maxIn / recipeMana));
        } else {
            if (!holder.hasCapabilityProxies()) return 0;
            long totalStored = getTotalManaStoredForInput(holder);
            if (totalStored < recipeMana) return 0;
            return Math.min(limit, (int) (totalStored / recipeMana));
        }
    }

    private long getMaxManaInputPerTick(IRecipeCapabilityHolder holder) {
        var containers = holder.getCapabilitiesFlat(IO.IN, ManaRecipeCapability.CAP);
        if (!containers.isEmpty() && containers.get(0) instanceof IManaContainer container) {
            return container.getInputPerSec() / 20;
        }
        return Long.MAX_VALUE;
    }

    private long getMaxManaOutputPerTick(IRecipeCapabilityHolder holder) {
        var containers = holder.getCapabilitiesFlat(IO.OUT, ManaRecipeCapability.CAP);
        if (!containers.isEmpty() && containers.get(0) instanceof IManaContainer container) {
            return container.getOutputPerSec() / 20;
        }
        return Long.MAX_VALUE;
    }

    private long getTotalManaStoredForInput(IRecipeCapabilityHolder holder) {
        long total = 0;
        var containers = holder.getCapabilitiesFlat(IO.IN, ManaRecipeCapability.CAP);
        for (var obj : containers) {
            if (obj instanceof IManaContainer container) {
                total += container.getManaStored();
            }
        }
        return total;
    }

    public interface ICustomParallel {

        int limitManaParallel(GTRecipe recipe, int multiplier, boolean tick);

        int getMaxManaParallel(GTRecipe recipe, int limit, boolean tick);
    }
}

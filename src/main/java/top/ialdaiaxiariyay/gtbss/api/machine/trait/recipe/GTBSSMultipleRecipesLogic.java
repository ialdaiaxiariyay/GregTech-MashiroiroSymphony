package top.ialdaiaxiariyay.gtbss.api.machine.trait.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.machine.multiblock.MultipleRecipeWorkableElectricMultiblockMachine;
import top.ialdaiaxiariyay.gtbss.common.machine.multiblock.part.MultipleRecipeParallelHatchPartMachine;

import java.util.*;

public class GTBSSMultipleRecipesLogic extends RecipeLogic {

    private static final int MAX_OUTPUT_RETRIES = 20;
    private static final int RECIPE_CACHE_TTL = 100;

    @Getter
    private final List<ActiveRecipe> activeRecipes = new ArrayList<>();
    private int recipeRoundRobinIndex = 0;

    private List<GTRecipe> cachedAvailableRecipes;
    private long lastCacheTime = 0;

    public GTBSSMultipleRecipesLogic() {
        super();
    }

    @Override
    public void onTraitAttached() {
        super.onTraitAttached();
        setKeepSubscribing(true);
        setStatus(Status.IDLE);
        updateTickSubscription();
    }

    public int getMaxThreads() {
        int threads = 1;
        if (getMachine() instanceof MultipleRecipeWorkableElectricMultiblockMachine controller &&
                controller.isFormed()) {
            threads = controller.getMultipleRecipeParallelHatch()
                    .map(MultipleRecipeParallelHatchPartMachine::getMultipleRecipeParallel)
                    .orElse(1);
        }
        return Math.max(1, threads);
    }

    public int getMaxParallel() {
        if (getMachine() instanceof MultiblockControllerMachine controller && controller.isFormed()) {
            return controller.getParallelHatch()
                    .map(ParallelHatchPartMachine::getCurrentParallel)
                    .orElse(1);
        }
        return 1;
    }

    @Override
    public void serverTick() {
        MetaMachine machine = getMachine();
        if (machine.getLevel() == null || machine.getLevel().isClientSide) return;

        boolean isWorkingEnabled = true;
        if (machine instanceof IRecipeLogicMachine rlm) {
            isWorkingEnabled = rlm.isWorkingEnabled();
        }
        if (!isWorkingEnabled) {
            if (!activeRecipes.isEmpty()) {
                activeRecipes.clear();
                setStatus(Status.SUSPEND);
                updateTickSubscription();
            }
            return;
        }

        double globalMaxProgress = 0;
        for (ActiveRecipe active : activeRecipes) {
            if (active.maxProgress > globalMaxProgress) {
                globalMaxProgress = active.maxProgress;
            }
        }

        Iterator<ActiveRecipe> iterator = activeRecipes.iterator();
        while (iterator.hasNext()) {
            ActiveRecipe active = iterator.next();

            if (active.progress < active.maxProgress) {
                double delta = active.maxProgress / globalMaxProgress;
                active.progress = Math.min(active.progress + delta, active.maxProgress);
            }

            if (active.progress >= active.maxProgress) {
                ActionResult result = RecipeHelper.handleRecipeIO(
                        (IRecipeCapabilityHolder) getMachine(),
                        active.recipe,
                        IO.OUT,
                        active.chanceCaches);
                if (result.isSuccess()) {
                    iterator.remove();
                } else {
                    active.retryCount++;
                    if (active.retryCount < MAX_OUTPUT_RETRIES) {
                        active.progress = active.maxProgress - 0.5;
                    } else {
                        GTBSS.LOGGER.error("Thread for recipe {} exceeded retry limit, abandoned.", active.recipe.id);
                        iterator.remove();
                    }
                }
            }
        }

        int maxThreads = getMaxThreads();
        if (activeRecipes.size() < maxThreads && machine.getOffsetTimer() % 5 == 0) {
            List<GTRecipe> available = getAvailableRecipes();
            if (!available.isEmpty()) {
                int attempts = 0;
                int maxAttempts = maxThreads * available.size();
                while (activeRecipes.size() < maxThreads && attempts < maxAttempts) {
                    GTRecipe recipe = available.get(recipeRoundRobinIndex % available.size());
                    if (tryStartRecipe(recipe)) {
                        recipeRoundRobinIndex++;
                        attempts = 0;
                    } else {
                        recipeRoundRobinIndex++;
                        attempts++;
                    }
                }
            }
        }

        setStatus(activeRecipes.isEmpty() ? Status.IDLE : Status.WORKING);
        updateTickSubscription();
    }

    private boolean tryStartRecipe(GTRecipe originalRecipe) {
        MetaMachine machine = getMachine();

        GTRecipe baseRecipe = originalRecipe.copy();
        if (machine instanceof MultiblockControllerMachine controller) {
            for (MultiblockPartMachine part : controller.getParts()) {
                baseRecipe = part.modifyRecipe(baseRecipe);
                if (baseRecipe == null) return false;
            }
        }

        if (machine instanceof MultipleRecipeWorkableElectricMultiblockMachine custom) {
            baseRecipe = custom.doModifyRecipe(baseRecipe);
            if (baseRecipe == null) return false;
        }

        if (machine instanceof MultipleRecipeWorkableElectricMultiblockMachine custom && custom.isBatchEnabled()) {
            int minDuration = 20;
            for (MultiblockPartMachine part : custom.getParts()) {
                if (part instanceof MultipleRecipeParallelHatchPartMachine hatch) {
                    minDuration = hatch.getMinRecipeDuration();
                    break;
                }
            }
            if (baseRecipe.duration < minDuration) {
                int factor = (int) Math.ceil((double) minDuration / baseRecipe.duration);
                if (factor > 1) {
                    baseRecipe.duration *= factor;
                    EnergyStack energyStack = RecipeHelper.getRealEUt(baseRecipe);
                    if (!energyStack.isEmpty()) {
                        long originalVoltage = energyStack.voltage();
                        long originalAmperage = energyStack.amperage();
                        long newVoltage = Math.max(1, originalVoltage / factor);
                        Map<RecipeCapability<?>, List<Content>> tickInputs = baseRecipe.tickInputs;
                        if (tickInputs.containsKey(EURecipeCapability.CAP)) {
                            List<Content> euList = tickInputs.get(EURecipeCapability.CAP);
                            if (euList != null && !euList.isEmpty()) {
                                List<Content> newEuList = getContents(euList, newVoltage, originalAmperage);
                                tickInputs.put(EURecipeCapability.CAP, newEuList);
                            }
                        }
                    }
                }
            }
        }

        if (baseRecipe.parallels > 1) {
            if (RecipeHelper.matchContents((IRecipeCapabilityHolder) machine, baseRecipe).isSuccess()) {
                ActionResult result = RecipeHelper.handleRecipeIO(
                        (IRecipeCapabilityHolder) machine,
                        baseRecipe,
                        IO.IN,
                        getChanceCaches());
                if (result.isSuccess()) {
                    double avgRatio = calculateAverageProgress();
                    ActiveRecipe active = new ActiveRecipe(baseRecipe, baseRecipe.duration, getChanceCaches());
                    active.progress = avgRatio * active.maxProgress;
                    activeRecipes.add(active);
                    return true;
                }
            }
            return false;
        }

        int attempted = getMaxParallel();
        GTRecipe finalRecipe = null;
        while (attempted > 0) {
            GTRecipe testRecipe = baseRecipe.copy();
            if (attempted > 1) {
                ModifierFunction parallelModifier = ModifierFunction.builder()
                        .modifyAllContents(ContentModifier.multiplier(attempted))
                        .eutMultiplier(attempted)
                        .parallels(attempted)
                        .build();
                testRecipe = parallelModifier.apply(testRecipe);
            }

            if (RecipeHelper.matchContents((IRecipeCapabilityHolder) machine, testRecipe).isSuccess()) {
                ActionResult result = RecipeHelper.handleRecipeIO(
                        (IRecipeCapabilityHolder) machine,
                        testRecipe,
                        IO.IN,
                        getChanceCaches());
                if (result.isSuccess()) {
                    finalRecipe = testRecipe;
                    break;
                }
            }
            attempted = Math.max(1, attempted / 2);
        }

        if (finalRecipe == null) return false;

        double avgRatio = calculateAverageProgress();
        ActiveRecipe active = new ActiveRecipe(finalRecipe, finalRecipe.duration, getChanceCaches());
        active.progress = avgRatio * active.maxProgress;
        activeRecipes.add(active);
        return true;
    }

    private static @NotNull List<Content> getContents(List<Content> euList, long newVoltage, long newAmperage) {
        List<Content> newEuList = new ArrayList<>(euList);
        Content old = newEuList.get(0);
        Object newContent;
        if (old.content() instanceof EnergyStack) {
            newContent = new EnergyStack(newVoltage, newAmperage);
        } else {
            newContent = newVoltage;
        }
        newEuList.set(0, new Content(newContent, old.chance(), old.maxChance()));
        return newEuList;
    }

    private double calculateAverageProgress() {
        if (activeRecipes.isEmpty()) return 0;
        double sum = 0;
        for (ActiveRecipe a : activeRecipes) {
            sum += a.progress / (double) a.maxProgress;
        }
        return sum / activeRecipes.size();
    }

    private List<GTRecipe> getAvailableRecipes() {
        long now = System.currentTimeMillis();
        if (cachedAvailableRecipes != null && (now - lastCacheTime) < RECIPE_CACHE_TTL * 50) {
            return cachedAvailableRecipes;
        }

        List<GTRecipe> result = new ArrayList<>();
        IRecipeLogicMachine logicMachine = (IRecipeLogicMachine) getMachine();
        GTRecipeType activeType = logicMachine.getRecipeType();
        if (getMachine().getLevel() == null) {
            return result;
        }

        RecipeManager recipeManager = getMachine().getLevel().getRecipeManager();
        List<GTRecipe> all = recipeManager.getAllRecipesFor(activeType);
        for (Recipe<?> r : all) {
            if (r instanceof GTRecipe gtRecipe) {
                if (RecipeHelper.checkConditions(gtRecipe, this).isSuccess()) {
                    result.add(gtRecipe);
                }
            }
        }

        cachedAvailableRecipes = result;
        lastCacheTime = now;
        return result;
    }

    @Override
    public double getProgressPercent() {
        if (activeRecipes.isEmpty()) return 0;
        return activeRecipes.stream()
                .mapToDouble(a -> a.progress / (double) a.maxProgress)
                .average()
                .orElse(0);
    }

    @Override
    public int getMaxProgress() {
        if (activeRecipes.isEmpty()) return 0;
        return activeRecipes.stream().mapToInt(a -> a.maxProgress).max().orElse(0);
    }

    @Override
    public int getProgress() {
        if (activeRecipes.isEmpty()) return 0;
        return (int) (getProgressPercent() * getMaxProgress());
    }

    public List<Component> getRecipeDisplayInfo() {
        List<Component> infoList = new ArrayList<>();
        for (int i = 0; i < activeRecipes.size(); i++) {
            ActiveRecipe active = activeRecipes.get(i);
            double prog = active.progress;
            int max = active.maxProgress;
            float currentSec = (float) (prog / 20.0);
            float maxSec = max / 20.0f;
            int percentage = max > 0 ? (int) ((prog / (double) max) * 100) : 0;
            ChatFormatting percentColor = percentage < 33 ? ChatFormatting.RED :
                    (percentage < 66 ? ChatFormatting.YELLOW : ChatFormatting.GREEN);

            MutableComponent line1 = Component.literal("Thread " + (i + 1) + ": ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(String.format(Locale.US, "%.1fs / %.1fs ", currentSec, maxSec))
                            .withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(String.format("(%d%%)", percentage))
                            .withStyle(percentColor));
            infoList.add(line1);

            String outputName = "Unknown";
            int totalCount = 1;
            if (active.recipe.outputs.containsKey(ItemRecipeCapability.CAP)) {
                List<Content> itemOutputs = active.recipe.outputs.get(ItemRecipeCapability.CAP);
                if (itemOutputs != null && !itemOutputs.isEmpty()) {
                    Content content = itemOutputs.get(0);
                    Object inner = content.content();
                    if (inner instanceof ItemStack stack) {
                        outputName = stack.getHoverName().getString();
                        totalCount = stack.getCount();
                    } else if (inner instanceof SizedIngredient sized) {
                        ItemStack[] stacks = sized.getItems();
                        if (stacks.length > 0) outputName = stacks[0].getHoverName().getString();
                        totalCount = sized.getAmount();
                    } else if (inner instanceof Ingredient ing) {
                        ItemStack[] stacks = ing.getItems();
                        if (stacks.length > 0) outputName = stacks[0].getHoverName().getString();
                    }
                }
            }
            double timePerItem = (maxSec > 0 && totalCount > 0) ? (maxSec / totalCount) : maxSec;
            String displayName = outputName.length() > 20 ? outputName.substring(0, 20) + "..." : outputName;
            MutableComponent line2 = Component.literal(" -> ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(displayName)
                            .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal(outputName)))))
                    .append(Component.literal(" x" + totalCount)
                            .withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(String.format(Locale.US, " (%.2fs/item)", timePerItem))
                            .withStyle(ChatFormatting.GRAY));
            infoList.add(line2);
        }
        return infoList;
    }

    public static class ActiveRecipe {

        public final GTRecipe recipe;
        public double progress;
        public final int maxProgress;
        public final Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches;
        public int retryCount = 0;

        public ActiveRecipe(GTRecipe recipe, int maxProgress,
                            @NotNull IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> chanceCaches) {
            this.recipe = recipe;
            this.progress = 0.0;
            this.maxProgress = Math.max(1, maxProgress);

            this.chanceCaches = new IdentityHashMap<>();
            for (Map.Entry<RecipeCapability<?>, Object2IntMap<?>> entry : chanceCaches.entrySet()) {
                RecipeCapability<?> cap = entry.getKey();
                Object2IntMap<?> original = entry.getValue();
                Object2IntMap<Object> copy = new Object2IntOpenHashMap<>();
                for (Object2IntMap.Entry<?> e : original.object2IntEntrySet()) {
                    copy.put(e.getKey(), e.getIntValue());
                }
                this.chanceCaches.put(cap, copy);
            }
        }
    }
}

package top.ialdaiaxiariyay.gtms.api.registrate;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.GTMS;

import java.util.*;
import java.util.function.Supplier;

public class MagicModuleCombinationRegistry {

    @FunctionalInterface
    public interface CombinationAction {

        /**
         * Execute the combined spell.
         *
         * @param level      the world
         * @param player     the caster
         * @param chargeTime charge progress [0.0, 1.0]
         */
        void execute(Level level, Player player, float chargeTime);
    }

    // 增加 manaCost 字段
    public record CombinationEntry(CombinationAction action, Component displayName,
                                   List<Component> tooltips, @Nullable Integer manaCost) {}

    private static final Map<String, CombinationEntry> COMBOS_UNORDERED = new HashMap<>();
    private static final Map<String, CombinationEntry> COMBOS_ORDERED = new HashMap<>();
    private static final List<Runnable> PENDING = new ArrayList<>();
    private static boolean frozen = true;

    // 重载注册方法，增加 manaCost 参数
    @SafeVarargs
    public static void register(CombinationAction action, Component displayName, boolean ordered,
                                List<Component> tooltips, @Nullable Integer manaCost,
                                Supplier<? extends Item>... modules) {
        if (frozen) throw new IllegalStateException("Registry is frozen!");
        PENDING.add(() -> {
            List<String> ids = new ArrayList<>();
            for (Supplier<? extends Item> sup : modules) {
                Item item = sup.get();
                ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
                if (key == null) throw new IllegalStateException("Item not registered: " + item);
                ids.add(key.toString());
            }
            CombinationEntry entry = new CombinationEntry(action, displayName,
                    tooltips != null ? List.copyOf(tooltips) : List.of(), manaCost);
            if (ordered) {
                String key = String.join("|", ids);
                COMBOS_ORDERED.put(key, entry);
            } else {
                Collections.sort(ids);
                String key = String.join("|", ids);
                COMBOS_UNORDERED.put(key, entry);
            }
        });
    }

    @SafeVarargs
    public static void register(CombinationAction action, Component displayName, List<Component> tooltips,
                                Supplier<? extends Item>... modules) {
        register(action, displayName, false, tooltips, null, modules);
    }

    @SafeVarargs
    public static CombinationBuilder combination(Supplier<? extends Item>... modules) {
        return new CombinationBuilder(modules);
    }

    public static class CombinationBuilder {

        private final Supplier<? extends Item>[] modules;
        private CombinationAction action;
        private Component displayName;
        private List<Component> tooltips;
        private boolean ordered = false;
        private Integer manaCost = null;

        @SafeVarargs
        private CombinationBuilder(Supplier<? extends Item>... modules) {
            this.modules = modules;
        }

        public CombinationBuilder then(CombinationAction action) {
            this.action = action;
            return this;
        }

        public CombinationBuilder displayName(Component name) {
            this.displayName = name;
            return this;
        }

        public CombinationBuilder ordered() {
            this.ordered = true;
            return this;
        }

        public CombinationBuilder tooltips(Component... tooltips) {
            this.tooltips = Arrays.asList(tooltips);
            return this;
        }

        public CombinationBuilder manaCost(int manaCost) {
            this.manaCost = manaCost;
            return this;
        }

        public void register() {
            if (action == null) throw new IllegalStateException("No action defined!");
            if (displayName == null) {
                displayName = Component.translatable("gtms.tooltip.wand.combination");
            }
            MagicModuleCombinationRegistry.register(action, displayName, ordered, tooltips, manaCost, modules);
        }
    }

    public static @Nullable CombinationEntry getCombinationEntry(List<String> moduleIds) {
        String orderedKey = String.join("|", moduleIds);
        CombinationEntry entry = COMBOS_ORDERED.get(orderedKey);
        if (entry != null) return entry;

        List<String> sorted = new ArrayList<>(moduleIds);
        Collections.sort(sorted);
        return COMBOS_UNORDERED.get(String.join("|", sorted));
    }

    public static @Nullable CombinationAction getCombinationAction(List<String> moduleIds) {
        CombinationEntry entry = getCombinationEntry(moduleIds);
        return entry != null ? entry.action() : null;
    }

    public static void unfreeze() {
        if (!frozen) {
            GTMS.LOGGER.warn("Registry already unfrozen, ignoring duplicate call.");
            return;
        }
        frozen = false;
    }

    public static void freeze() {
        if (frozen) {
            GTMS.LOGGER.warn("Registry already frozen, ignoring duplicate call.");
            return;
        }
        PENDING.forEach(Runnable::run);
        PENDING.clear();
        frozen = true;
        GTMS.LOGGER.info("Registered {} unordered and {} ordered magic combinations.",
                COMBOS_UNORDERED.size(), COMBOS_ORDERED.size());
    }
}

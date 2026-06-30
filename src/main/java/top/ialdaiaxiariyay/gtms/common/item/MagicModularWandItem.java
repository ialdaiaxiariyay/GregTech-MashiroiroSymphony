package top.ialdaiaxiariyay.gtms.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.Nullable;
import top.ialdaiaxiariyay.gtms.api.item.IMagicModule;
import top.ialdaiaxiariyay.gtms.api.item.MagicModuleItem;
import top.ialdaiaxiariyay.gtms.api.registrate.MagicModuleCombinationRegistry;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaItemHandler;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MagicModularWandItem extends Item {

    private static final String MODULES_TAG = "Modules";
    private static final String MANA_TAG = "Mana";
    private static final String MAX_MANA_TAG = "MaxMana";
    private static final int BASE_CHARGE_TICKS = 60;
    private static final int MIN_CHARGE_TICKS = 5;
    private static final int MAX_USE_DURATION = 72000;
    private final int maxMana;
    private final int maxModules;

    public MagicModularWandItem(Properties properties, int maxModules, int maxMana) {
        super(properties);
        this.maxModules = maxModules;
        this.maxMana = maxMana;
    }

    public static List<String> getInstalledModules(ItemStack wand) {
        CompoundTag tag = wand.getTag();
        if (tag == null || !tag.contains(MODULES_TAG)) {
            return new ArrayList<>();
        }
        ListTag listTag = tag.getList(MODULES_TAG, Tag.TAG_STRING);
        List<String> modules = new ArrayList<>();
        for (int i = 0; i < listTag.size(); i++) {
            modules.add(listTag.getString(i));
        }
        return modules;
    }

    private static void saveModules(ItemStack wand, List<String> modules) {
        ListTag listTag = new ListTag();
        for (String id : modules) {
            listTag.add(StringTag.valueOf(id));
        }
        wand.getOrCreateTag().put(MODULES_TAG, listTag);
    }

    public boolean addModule(ItemStack wand, String moduleId) {
        List<String> modules = getInstalledModules(wand);
        if (modules.size() >= maxModules || modules.contains(moduleId)) {
            return false;
        }
        modules.add(moduleId);
        saveModules(wand, modules);
        return true;
    }

    public boolean removeLastModule(ItemStack wand) {
        List<String> modules = getInstalledModules(wand);
        if (modules.isEmpty()) return false;
        modules.remove(modules.size() - 1);
        saveModules(wand, modules);
        return true;
    }

    public static int getManaFromNBT(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(MANA_TAG)) {
            setManaInNBT(stack, 0);
            return 0;
        }
        return tag.getInt(MANA_TAG);
    }

    public static void setManaInNBT(ItemStack stack, int mana) {
        stack.getOrCreateTag().putInt(MANA_TAG, Math.max(0, mana));
    }

    public static int getMaxManaFromNBT(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(MAX_MANA_TAG)) {
            return tag.getInt(MAX_MANA_TAG);
        }
        if (stack.getItem() instanceof MagicModularWandItem wandItem) {
            return wandItem.maxMana;
        }
        return 0;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {

            private final LazyOptional<ManaItem> manaCap = LazyOptional.of(() -> new ManaItemImpl(stack));

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
                if (cap == BotaniaForgeCapabilities.MANA_ITEM) {
                    return manaCap.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    private record ManaItemImpl(ItemStack stack) implements ManaItem {

        @Override
        public int getMana() {
            return getManaFromNBT(stack);
        }

        @Override
        public int getMaxMana() {
            return getMaxManaFromNBT(stack);
        }

        @Override
        public void addMana(int mana) {
            int current = getMana();
            int max = getMaxMana();
            setManaInNBT(stack, Math.min(max, current + mana));
        }

        @Override
        public boolean canReceiveManaFromPool(BlockEntity pool) {
            return getMana() < getMaxMana();
        }

        @Override
        public boolean canReceiveManaFromItem(ItemStack otherStack) {
            return getMana() < getMaxMana();
        }

        @Override
        public boolean canExportManaToPool(BlockEntity pool) {
            return false;
        }

        @Override
        public boolean canExportManaToItem(ItemStack otherStack) {
            return false;
        }

        @Override
        public boolean isNoExport() {
            return true;
        }
    }

    private int getMaxChargeTicks(ItemStack stack) {
        List<String> moduleIds = getInstalledModules(stack);
        if (moduleIds.isEmpty()) {
            return BASE_CHARGE_TICKS;
        }
        float totalModifier = 0.0f;
        for (String id : moduleIds) {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(id));
            if (item instanceof IMagicModule module) {
                totalModifier += module.getChargeTimeModifier();
            }
        }
        float adjusted = BASE_CHARGE_TICKS * (1.0f + totalModifier);
        return Math.max(MIN_CHARGE_TICKS, Math.round(adjusted));
    }

    private int calcTotalManaCost(List<String> moduleIds) {
        MagicModuleCombinationRegistry.CombinationEntry combo = MagicModuleCombinationRegistry
                .getCombinationEntry(moduleIds);
        if (combo != null && combo.manaCost() != null) {
            return combo.manaCost();
        }
        int total = 0;
        for (String id : moduleIds) {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(id));
            if (item instanceof IMagicModule module) {
                total += module.getManaCost();
            }
        }
        return total;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return MAX_USE_DURATION;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;
        int usedTicks = MAX_USE_DURATION - remainingUseDuration;
        int maxCharge = getMaxChargeTicks(stack);
        if (usedTicks >= maxCharge) {
            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.getBoolean("FullChargePlayed")) {
                if (!level.isClientSide) {
                    level.playSound(null, player.blockPosition(),
                            SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
                    tag.putBoolean("FullChargePlayed", true);
                }
            }
        }
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack wand = player.getItemInHand(hand);
        wand.getOrCreateTag().putBoolean("FullChargePlayed", false);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                List<String> modules = getInstalledModules(wand);
                if (!modules.isEmpty()) {
                    String lastId = modules.get(modules.size() - 1);
                    if (removeLastModule(wand)) {
                        Item moduleItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(lastId));
                        if (moduleItem != null) {
                            ItemStack dropStack = new ItemStack(moduleItem);
                            if (!player.getInventory().add(dropStack)) {
                                player.drop(dropStack, false);
                            }
                        }
                        level.playSound(null, player.blockPosition(),
                                SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.5F, 1.5F);
                    }
                }
            }
            return InteractionResultHolder.sidedSuccess(wand, level.isClientSide);
        }

        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && offhand.getItem() instanceof MagicModuleItem) {
            if (!level.isClientSide) {
                ResourceLocation key = ForgeRegistries.ITEMS.getKey(offhand.getItem());
                if (key != null) {
                    String moduleId = key.toString();
                    if (addModule(wand, moduleId)) {
                        if (!player.isCreative()) {
                            offhand.shrink(1);
                        }
                        level.playSound(null, player.blockPosition(),
                                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 2.0F);
                    } else {
                        level.playSound(null, player.blockPosition(),
                                SoundEvents.NOTE_BLOCK_BASS.get(), SoundSource.PLAYERS, 0.5F, 0.5F);
                    }
                }
            }
            return InteractionResultHolder.sidedSuccess(wand, level.isClientSide);
        }

        if (getInstalledModules(wand).isEmpty()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("gtms.tooltip.wand.no_modules"), true);
            }
            return InteractionResultHolder.fail(wand);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(wand);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!(livingEntity instanceof Player player)) return;

        int usedTicks = MAX_USE_DURATION - timeCharged;
        int maxCharge = getMaxChargeTicks(stack);
        float chargeTime = Math.min(1.0f, (float) usedTicks / (float) maxCharge);
        if (chargeTime < 0.05f) return;

        List<String> modules = getInstalledModules(stack);
        if (modules.isEmpty()) return;

        if (!level.isClientSide) {
            int manaCost = calcTotalManaCost(modules);
            if (manaCost > 0) {
                int currentMana = getManaFromNBT(stack);
                int totalAvailable = currentMana +
                        ManaItemHandler.instance().requestMana(stack, player, manaCost, false);
                if (totalAvailable < manaCost) {
                    player.displayClientMessage(Component.translatable("gtms.recipe_modifier.insufficient_mana"), true);
                    level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS.get(),
                            SoundSource.PLAYERS, 0.5f, 0.5f);
                    stack.getOrCreateTag().remove("FullChargePlayed");
                    return;
                }
                int fromWand = Math.min(currentMana, manaCost);
                setManaInNBT(stack, currentMana - fromWand);
                int remaining = manaCost - fromWand;
                if (remaining > 0) {
                    ManaItemHandler.instance().requestMana(stack, player, remaining, true);
                }
            }

            castSpell(level, player, stack, modules, chargeTime);
            player.getCooldowns().addCooldown(this, 10);
        }

        stack.getOrCreateTag().remove("FullChargePlayed");
    }

    private void castSpell(Level level, Player player, ItemStack wand, List<String> moduleIds, float chargeTime) {
        MagicModuleCombinationRegistry.CombinationAction combo = MagicModuleCombinationRegistry
                .getCombinationAction(moduleIds);
        if (combo != null) {
            combo.execute(level, player, chargeTime);
            return;
        }
        for (String id : moduleIds) {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(id));
            if (item instanceof IMagicModule module) {
                module.cast(level, player, wand, chargeTime);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        List<String> modules = getInstalledModules(stack);
        if (modules.isEmpty()) {
            tooltip.add(Component.translatable("gtms.tooltip.wand.no_modules").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("gtms.tooltip.wand.installed").withStyle(ChatFormatting.GOLD));
            for (String id : modules) {
                Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(id));
                if (item != null) {
                    Component name = new ItemStack(item).getHoverName();
                    tooltip.add(Component.literal(" - ").append(name).withStyle(ChatFormatting.BLUE));
                }
            }

            MagicModuleCombinationRegistry.CombinationEntry entry = MagicModuleCombinationRegistry
                    .getCombinationEntry(modules);
            if (entry != null) {
                MutableComponent comboName = entry.displayName().copy();
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("gtms.tooltip.wand.combo").withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltip.add(comboName.withStyle(ChatFormatting.AQUA));
                tooltip.add(Component.translatable("gtms.tooltip.wand.combo.tooltip"));
                tooltip.addAll(entry.tooltips());
            }

            int chargeTicks = getMaxChargeTicks(stack);
            float seconds = chargeTicks / 20.0f;
            tooltip.add(Component.translatable("gtms.tooltip.wand.charge_time", String.format("%.1f", seconds))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        int mana = getManaFromNBT(stack);
        int maxMana = getMaxManaFromNBT(stack);
        tooltip.add(Component.translatable("gtms.jade.mana_stored", mana, maxMana)
                .withStyle(ChatFormatting.AQUA));

        tooltip.add(Component.translatable("gtms.tooltip.wand.max_slots", maxModules)
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("gtms.tooltip.wand.controls")
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GREEN));
    }
}

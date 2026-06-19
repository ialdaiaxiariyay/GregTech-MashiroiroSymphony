package top.ialdaiaxiariyay.gtms.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.network.NetworkHandler;
import top.ialdaiaxiariyay.gtms.network.packet.SanitySyncPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static top.ialdaiaxiariyay.gtms.common.ForgeEventListener.SANITY;

public class GTMSUtils {

    public static void Sanity(@NotNull Player player, int san) {
        player.getCapability(SANITY).ifPresent(sanity -> {
            sanity.addSanity(san);
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new SanitySyncPacket(serverPlayer.getId(), sanity.getSanity()));
            }
        });
    }

    public static @NotNull Supplier<HolderSet<Biome>> getBiomeHolderSet(ResourceLocation id) {
        Optional<? extends Holder<Biome>> holderOpt = ForgeRegistries.BIOMES.getHolder(id);
        if (holderOpt.isPresent()) {
            Holder<Biome> holder = holderOpt.get();
            return () -> HolderSet.direct(holder);
        } else {
            GTMS.LOGGER.error("Biome {} not found", id);
            return HolderSet::direct;
        }
    }

    public static @NotNull Supplier<HolderSet<Biome>> getBiomeTagHolderSet(ResourceLocation tagId) {
        TagKey<Biome> tagKey = TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), tagId);
        ITag<Biome> tag = Objects.requireNonNull(ForgeRegistries.BIOMES.tags()).getTag(tagKey);
        if (tag.isEmpty()) {
            return HolderSet::direct;
        }
        List<Holder<Biome>> holders = new ArrayList<>();
        for (Biome biome : tag) {
            ResourceLocation key = ForgeRegistries.BIOMES.getKey(biome);
            if (key != null) {
                Optional<? extends Holder<Biome>> holderOpt = ForgeRegistries.BIOMES.getHolder(key);
                holderOpt.ifPresent(holders::add);
            }
        }
        if (holders.isEmpty()) {
            return HolderSet::direct;
        }
        return () -> HolderSet.direct(holders);
    }
}

package top.ialdaiaxiariyay.gtbss.mixin.mc.sounds;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvent;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ialdaiaxiariyay.gtbss.GTBSS;

@Mixin(Musics.class)
public abstract class MusicsMixin {

    @Final
    @Shadow
    @Mutable
    public static Music MENU;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onClinit(CallbackInfo ci) {
        ResourceLocation musicId = GTBSS.id("menu_music");
        ResourceKey<SoundEvent> key = ResourceKey.create(BuiltInRegistries.SOUND_EVENT.key(), musicId);
        Holder<SoundEvent> holder = BuiltInRegistries.SOUND_EVENT.getHolder(key)
                .orElseThrow(() -> new IllegalStateException("Custom menu music not registered: " + musicId));

        MENU = new Music(holder, 20, 600, true);
    }
}

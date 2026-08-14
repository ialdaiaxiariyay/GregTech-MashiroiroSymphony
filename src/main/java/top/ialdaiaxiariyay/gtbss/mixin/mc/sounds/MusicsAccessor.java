package top.ialdaiaxiariyay.gtbss.mixin.mc.sounds;

import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Musics.class)
public interface MusicsAccessor {

    @Accessor("MENU")
    @Mutable
    static void setMenu(Music music) {
        throw new UnsupportedOperationException("Mixin Accessor not applied!");
    }
}

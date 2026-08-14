package top.ialdaiaxiariyay.gtbss.mixin.mc.world.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabMixin {

    @Final
    @Shadow
    private Component displayName;

    @Mutable
    @Final
    @Shadow(remap = false)
    private boolean hasSearchBar;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (displayName.getString().equals("itemGroup.botania")) {
            hasSearchBar = false;
        }
    }
}

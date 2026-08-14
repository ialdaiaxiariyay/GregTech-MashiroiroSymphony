package top.ialdaiaxiariyay.gtbss.utils;

import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;
import vazkii.botania.api.BotaniaAPI;

public class ModIdUtil {

    public static ResourceLocation Botania(String path) {
        return ResourceLocation.tryBuild(BotaniaAPI.MODID, path);
    }

    public static ResourceLocation Ae2(String path) {
        return ResourceLocation.tryBuild(AppEng.MOD_ID, path);
    }
}

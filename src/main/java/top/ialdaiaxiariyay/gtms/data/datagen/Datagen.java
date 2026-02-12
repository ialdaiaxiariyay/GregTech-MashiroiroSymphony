package top.ialdaiaxiariyay.gtms.data.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.data.datagen.lang.UnifiedLanguageProvider;

@Mod.EventBusSubscriber(modid = GTMS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Datagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        // ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        generator.addProvider(event.includeClient(), new UnifiedLanguageProvider(generator, "en_us"));
        generator.addProvider(event.includeClient(), new UnifiedLanguageProvider(generator, "zh_cn"));
    }
}

package top.ialdaiaxiariyay.gtbss.api.addon;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import top.ialdaiaxiariyay.gtbss.api.annotation.GTBSSAddon;

import java.lang.reflect.Constructor;
import java.util.*;

public class GTBSSAddonFinder {

    private static final Logger LOGGER = LogManager.getLogger();
    protected static List<IGTBSSAddon> cache = null;
    protected static Map<String, IGTBSSAddon> modIdMap = new HashMap<>();

    public static List<IGTBSSAddon> getAddons() {
        if (cache == null) {
            cache = getInstances(GTBSSAddon.class, IGTBSSAddon.class);
            for (IGTBSSAddon addon : cache) {
                modIdMap.put(addon.addonModId(), addon);
            }
        }

        return cache;
    }

    @Nullable
    public static IGTBSSAddon getAddon(String modId) {
        return modIdMap.get(modId);
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> List<T> getInstances(Class<?> annotationClass, Class<T> instanceClass) {
        Type annotationType = Type.getType(annotationClass);
        Set<String> pluginClassNames = getStrings(annotationType);
        List<T> instances = new ArrayList<>();
        for (String className : pluginClassNames) {
            try {
                Class<?> asmClass = Class.forName(className);
                Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
                Constructor<? extends T> constructor = asmInstanceClass.getDeclaredConstructor();
                T instance = constructor.newInstance();
                instances.add(instance);
            } catch (ReflectiveOperationException | LinkageError e) {
                LOGGER.error("Failed to load: {}", className, e);
            }
        }
        return instances;
    }

    private static @NotNull Set<String> getStrings(Type annotationType) {
        List<ModFileScanData> allScanData = ModList.get().getAllScanData();
        Set<String> pluginClassNames = new LinkedHashSet<>();
        for (ModFileScanData scanData : allScanData) {
            Iterable<ModFileScanData.AnnotationData> annotations = scanData.getAnnotations();
            for (ModFileScanData.AnnotationData a : annotations) {
                if (Objects.equals(a.annotationType(), annotationType)) {
                    String memberName = a.memberName();
                    pluginClassNames.add(memberName);
                }
            }
        }
        return pluginClassNames;
    }
}

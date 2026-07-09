package top.ialdaiaxiariyay.gtms.api.registrate;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtms.GTMS;
import top.ialdaiaxiariyay.gtms.api.item.MagicModuleItem;

import java.util.function.Supplier;

public class GTMSRegistrate extends GTRegistrate {

    protected GTMSRegistrate(String modId) {
        super(modId);
    }

    @Contract(value = "_ -> new", pure = true)
    @SafeVarargs
    public final MagicModuleCombinationRegistry.@NotNull CombinationBuilder magicModuleRegistry(Supplier<? extends MagicModuleItem>... module) {
        return MagicModuleCombinationRegistry.combination(module);
    }

    public static GTMSRegistrate REGISTRATE = new GTMSRegistrate(GTMS.MOD_ID);
}

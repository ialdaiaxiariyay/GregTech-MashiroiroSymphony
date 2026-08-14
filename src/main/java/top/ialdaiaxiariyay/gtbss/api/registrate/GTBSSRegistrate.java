package top.ialdaiaxiariyay.gtbss.api.registrate;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.ialdaiaxiariyay.gtbss.GTBSS;
import top.ialdaiaxiariyay.gtbss.api.item.MagicModuleItem;

import java.util.function.Supplier;

public class GTBSSRegistrate extends GTRegistrate {

    protected GTBSSRegistrate(String modId) {
        super(modId);
    }

    @Contract(value = "_ -> new", pure = true)
    @SafeVarargs
    public final MagicModuleCombinationRegistry.@NotNull CombinationBuilder magicModuleRegistry(Supplier<? extends MagicModuleItem>... module) {
        return MagicModuleCombinationRegistry.combination(module);
    }

    public static GTBSSRegistrate REGISTRATION = new GTBSSRegistrate(GTBSS.MOD_ID);
}

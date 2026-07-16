package top.ialdaiaxiariyay.gtbss.api.addon;

public interface IGTBSSAddon {

    String addonModId();

    default void registerMagicModuleCombo() {}
}

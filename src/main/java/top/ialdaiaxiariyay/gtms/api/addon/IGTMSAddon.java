package top.ialdaiaxiariyay.gtms.api.addon;

public interface IGTMSAddon {

    String addonModId();

    default void registerMagicModuleCombo() {}
}

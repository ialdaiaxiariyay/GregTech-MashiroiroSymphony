package top.ialdaiaxiariyay.gtbss.common.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import top.ialdaiaxiariyay.gtbss.GTBSS;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.METALLIC;
import static top.ialdaiaxiariyay.gtbss.common.data.GTBSSMaterials.*;

public class GTBSSMaterialsBuilder {

    public static void init() {
        Dream = new Material.Builder(GTBSS.id("dream"))
                .dust()
                .color(0x87a6ed)
                .secondaryColor(0x6080c8)
                .iconSet(METALLIC)
                .buildAndRegister();
    }
}

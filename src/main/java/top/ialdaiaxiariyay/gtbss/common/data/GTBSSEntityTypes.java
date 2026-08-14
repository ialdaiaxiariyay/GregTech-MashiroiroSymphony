package top.ialdaiaxiariyay.gtbss.common.data;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.MobCategory;

import com.tterrag.registrate.util.entry.EntityEntry;
import top.ialdaiaxiariyay.gtbss.api.registrate.GTBSSRegistrate;
import top.ialdaiaxiariyay.gtbss.common.entity.SpearEntity;

public class GTBSSEntityTypes {

    public static void init() {}

    public static final EntityEntry<SpearEntity> SPEAR = GTBSSRegistrate.REGISTRATION
            .<SpearEntity>entity("spear", SpearEntity::new, MobCategory.MISC)
            .properties(builder -> builder.sized(0.5F, 0.5F).clientTrackingRange(64).updateInterval(1))
            .tag(EntityTypeTags.IMPACT_PROJECTILES)
            .register();
}

package top.ialdaiaxiariyay.gtbss.common.item.module;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import top.ialdaiaxiariyay.gtbss.api.item.MagicModuleItem;
import top.ialdaiaxiariyay.gtbss.utils.GTBSSDamageUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WaterModule extends MagicModuleItem {

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    public WaterModule(Properties properties) {
        super(properties);
    }

    @Override
    public void cast(Level level, Player player, ItemStack wand, float chargeTime) {
        if (level.isClientSide) return;

        double radius = (3 + chargeTime * 2) / 2.0;
        int durationSeconds = (int) (4 + chargeTime * 3);
        float healPerSecond = 2 + chargeTime * 2;
        Vec3 center = player.position();

        ServerLevel server = (ServerLevel) level;

        for (int second = 0; second < durationSeconds; second++) {
            SCHEDULER.schedule(() -> {
                server.getServer().execute(() -> {
                    if (server.getServer().isRunning()) {
                        GTBSSDamageUtils.splitHeal(level, center, radius, healPerSecond,
                                e -> e != null && e.isAlive() && (e == player || e.isAlliedTo(player)));
                        for (int i = 0; i < 20; i++) {
                            double x = center.x + (level.random.nextDouble() - 0.5) * radius * 2;
                            double z = center.z + (level.random.nextDouble() - 0.5) * radius * 2;
                            double y = center.y + level.random.nextDouble() * 1.5;
                            server.sendParticles(ParticleTypes.SPLASH, x, y, z, 1, 0, 0.1, 0, 0.05);
                            server.sendParticles(ParticleTypes.BUBBLE, x, y, z, 1, 0, 0.1, 0, 0.05);
                        }
                        for (int i = 0; i < 5; i++) {
                            server.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP,
                                    center.x + (level.random.nextDouble() - 0.5) * 0.5,
                                    center.y + level.random.nextDouble() * 2,
                                    center.z + (level.random.nextDouble() - 0.5) * 0.5,
                                    1, 0, 0.2, 0, 0);
                        }
                    }
                });
            }, second, TimeUnit.SECONDS);
        }
    }

    @Override
    public float getChargeTimeModifier() {
        return -0.10f;
    }

    @Override
    public int getManaCost() {
        return 1100;
    }
}

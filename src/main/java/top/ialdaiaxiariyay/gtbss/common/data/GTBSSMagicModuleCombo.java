package top.ialdaiaxiariyay.gtbss.common.data;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class GTBSSMagicModuleCombo {

    private static void registry() {}

    private static void drawLineParticles(ServerLevel level, Vec3 from, Vec3 to, SimpleParticleType type, int count) {
        for (int i = 0; i <= count; i++) {
            double t = i / (double) count;
            Vec3 point = from.lerp(to, t);
            level.sendParticles(type, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }

    public static void init() {
        registry();
    }
}

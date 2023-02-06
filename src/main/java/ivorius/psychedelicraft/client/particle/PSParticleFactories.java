package ivorius.psychedelicraft.client.particle;

import ivorius.psychedelicraft.particle.PSParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSParticleFactories {
    static void bootstrap() {
        ParticleFactoryRegistry.getInstance().register(PSParticles.EXHALED_SMOKE, createFactory(ExhaledSmokeParticle::new));
        ParticleFactoryRegistry.getInstance().register(PSParticles.BUBBLE, createFactory(BubbleParticle::new));
    }

    private static <T extends ParticleEffect> PendingParticleFactory<T> createFactory(ParticleSupplier<T> supplier) {
        return provider -> (effect, world, x, y, z, dx, dy, dz) -> supplier.get(effect, provider, world, x, y, z, dx, dy, dz);
    }

    interface ParticleSupplier<T extends ParticleEffect> {
        Particle get(T effect, SpriteProvider provider, ClientWorld world, double x, double y, double z, double dx, double dy, double dz);
    }
}

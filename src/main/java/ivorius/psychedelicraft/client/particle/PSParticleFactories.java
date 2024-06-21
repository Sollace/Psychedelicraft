package ivorius.psychedelicraft.client.particle;

import org.joml.Vector3f;

import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.util.MathUtils;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory;
import net.minecraft.client.particle.BlockLeakParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.WaterSplashParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSParticleFactories {
    static void bootstrap() {
        ParticleFactoryRegistry.getInstance().register(PSParticles.EXHALED_SMOKE, createFactory(ExhaledSmokeParticle::new));
        ParticleFactoryRegistry.getInstance().register(PSParticles.BUBBLE, PSParticleFactories.<DrugDustParticleEffect>createFactory(FluidBubbleParticle::new));
        ParticleFactoryRegistry.getInstance().register(PSParticles.FLUID_SPLASH, createFactory(createSplash()));
        ParticleFactoryRegistry.getInstance().register(PSParticles.FLUID_BUBBLE, PSParticleFactories.<FluidParticleEffect>createFactory(FluidBubbleParticle::new));
        ParticleFactoryRegistry.getInstance().register(PSParticles.DRIPPING_FLUID, createFactory(PSParticleFactories::createDrippingFluid));
        ParticleFactoryRegistry.getInstance().register(PSParticles.FALLING_FLUID, createFactory(PSParticleFactories::createFallingFluid));
    }

    static ParticleSupplier<FluidParticleEffect> createSplash() {
        return (effect, provider, clientWorld, d, e, f, g, h, i) -> setColor(new WaterSplashParticle.SplashFactory(provider).createParticle(ParticleTypes.SPLASH, clientWorld, d, e, f, g, h, i), effect);
    }

    static Particle createDrippingFluid(FluidParticleEffect type, ClientWorld world,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ) {
        return setColor(new BlockLeakParticle.Dripping(world, x, y, z, type.fluid().getPhysical().getStandingFluid(), new FluidParticleEffect(PSParticles.FALLING_FLUID, type.fluid())), type);
    }

    static Particle createFallingFluid(FluidParticleEffect type, ClientWorld world,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ) {
        return setColor(new BlockLeakParticle.ContinuousFalling(world, x, y, z, type.fluid().getPhysical().getStandingFluid(), new FluidParticleEffect(PSParticles.FLUID_SPLASH, type.fluid())), type);
    }

    static Particle setColor(Particle particle, FluidParticleEffect effect) {
        Vector3f color = MathUtils.unpackRgb(effect.fluid().getColor(effect.fluid().getDefaultStack()));
        particle.setColor(color.x, color.y, color.z);
        return particle;
    }

    private static <T extends ParticleEffect> PendingParticleFactory<T> createFactory(ParticleSupplier<T> supplier) {
        return provider -> (effect, world, x, y, z, dx, dy, dz) -> supplier.get(effect, provider, world, x, y, z, dx, dy, dz);
    }

    private static <T extends ParticleEffect> PendingParticleFactory<T> createFactory(ParticleFactory<T> supplier) {
        return provider -> (effect, world, x, y, z, dx, dy, dz) -> {
            var particle = supplier.createParticle(effect, world, x, y, z, dx, dy, dz);
            if (particle instanceof SpriteBillboardParticle b) {
                b.setSprite(provider);
            }
            return particle;
        };
    }

    interface ParticleSupplier<T extends ParticleEffect> {
        Particle get(T effect, SpriteProvider provider, ClientWorld world, double x, double y, double z, double dx, double dy, double dz);
    }
}

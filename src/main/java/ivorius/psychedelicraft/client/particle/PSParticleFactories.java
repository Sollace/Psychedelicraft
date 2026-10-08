package ivorius.psychedelicraft.client.particle;

import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory;
import net.minecraft.client.particle.BlockLeakParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.WaterSplashParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.random.Random;

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
        ParticleFactoryRegistry.getInstance().register(PSParticles.BITS, createFactory(BitsParticle::new));
    }

    static ParticleSupplier<FluidParticleEffect> createSplash() {
        return (effect, provider, world, x, y, z, dx, dy, dz, random) -> setColor(new WaterSplashParticle.SplashFactory(provider).createParticle(ParticleTypes.SPLASH, world, x, y, z, dx, dy, dz, random), effect);
    }

    static Particle createDrippingFluid(FluidParticleEffect type, SpriteProvider provider, ClientWorld world,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ, Random random) {
        return setColor(new BlockLeakParticle.Dripping(world, x, y, z, type.fluid().fluid().getPhysical().getStandingFluid(), new FluidParticleEffect(PSParticles.FALLING_FLUID, type.fluid()), provider.getSprite(random)), type);
    }

    static Particle createFallingFluid(FluidParticleEffect type, SpriteProvider provider, ClientWorld world,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ, Random random) {
        return setColor(new BlockLeakParticle.ContinuousFalling(world, x, y, z, type.fluid().fluid().getPhysical().getStandingFluid(), new FluidParticleEffect(PSParticles.FLUID_SPLASH, type.fluid()), provider.getSprite(random)), type);
    }

    static Particle setColor(Particle particle, FluidParticleEffect effect) {
        if (particle instanceof BillboardParticle billboard) {
            int color = effect.fluid().fluid().getColor(effect.fluid());
            billboard.setColor(ColorHelper.getRedFloat(color), ColorHelper.getGreenFloat(color), ColorHelper.getBlueFloat(color));
        }
        return particle;
    }

    private static <T extends ParticleEffect> PendingParticleFactory<T> createFactory(ParticleSupplier<T> supplier) {
        return provider -> (effect, world, x, y, z, dx, dy, dz, random) -> supplier.get(effect, provider, world, x, y, z, dx, dy, dz, random);
    }

    interface ParticleSupplier<T extends ParticleEffect> {
        Particle get(T effect, SpriteProvider provider, ClientWorld world, double x, double y, double z, double dx, double dy, double dz, Random random);
    }
}

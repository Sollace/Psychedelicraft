package ivorius.psychedelicraft.particle;

import java.util.function.Function;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface PSParticles {
    ParticleType<DrugDustParticleEffect> EXHALED_SMOKE = register("exhaled_smoke", DrugDustParticleEffect.createType());
    ParticleType<DrugDustParticleEffect> BUBBLE = register("bubble", DrugDustParticleEffect.createType());

    ParticleType<FluidParticleEffect> DRIPPING_FLUID = register("dripping_fluid", false, FluidParticleEffect::createCodec, FluidParticleEffect::createPacketCodec);
    ParticleType<FluidParticleEffect> FALLING_FLUID = register("falling_fluid", false, FluidParticleEffect::createCodec, FluidParticleEffect::createPacketCodec);
    ParticleType<FluidParticleEffect> SPLASH = register("splash", false, FluidParticleEffect::createCodec, FluidParticleEffect::createPacketCodec);

    static <T extends ParticleType<?>> T register(String name, T type) {
        return Registry.register(Registries.PARTICLE_TYPE, Psychedelicraft.id(name), type);
    }

    private static <T extends ParticleEffect> ParticleType<T> register(
            String name,
            boolean alwaysShow,
            Function<ParticleType<T>, MapCodec<T>> codecGetter,
            Function<ParticleType<T>, PacketCodec<? super RegistryByteBuf, T>> packetCodecGetter
        ) {
            return Registry.register(Registries.PARTICLE_TYPE, Psychedelicraft.id(name), new ParticleType<T>(alwaysShow) {
                @Override
                public MapCodec<T> getCodec() {
                    return codecGetter.apply(this);
                }

                @Override
                public PacketCodec<? super RegistryByteBuf, T> getPacketCodec() {
                    return packetCodecGetter.apply(this);
                }
            });
        }

    static void bootstrap() {}
}

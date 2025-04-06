package ivorius.psychedelicraft.particle;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.util.compat.PacketCodec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface PSParticles {
    ParticleType<DrugDustParticleEffect> EXHALED_SMOKE = register("exhaled_smoke", DrugDustParticleEffect.createType());
    ParticleType<DrugDustParticleEffect> BUBBLE = register("bubble", DrugDustParticleEffect.createType());

    ParticleType<FluidParticleEffect> DRIPPING_FLUID = register("dripping_fluid", false, FluidParticleEffect::createCodec, FluidParticleEffect::createPacketCodec, FluidParticleEffect::createFactory);
    ParticleType<FluidParticleEffect> FALLING_FLUID = register("falling_fluid", false, FluidParticleEffect::createCodec, FluidParticleEffect::createPacketCodec, FluidParticleEffect::createFactory);
    ParticleType<FluidParticleEffect> FLUID_SPLASH = register("fluid_splash", false, FluidParticleEffect::createCodec, FluidParticleEffect::createPacketCodec, FluidParticleEffect::createFactory);
    ParticleType<FluidParticleEffect> FLUID_BUBBLE = register("fluid_bubble", false, FluidParticleEffect::createCodec, FluidParticleEffect::createPacketCodec, FluidParticleEffect::createFactory);

    static <T extends ParticleType<?>> T register(String name, T type) {
        return Registry.register(Registries.PARTICLE_TYPE, Psychedelicraft.id(name), type);
    }

    private static <T extends ParticleEffect> ParticleType<T> register(
            String name,
            boolean alwaysShow,
            Function<ParticleType<T>, MapCodec<T>> codecGetter,
            Function<ParticleType<T>, PacketCodec<? super PacketByteBuf, T>> packetCodecGetter,
            @SuppressWarnings("deprecation") Function<Function<ParticleType<T>, PacketCodec<? super PacketByteBuf, T>>, ParticleEffect.Factory<T>> factoryGetter
        ) {
            return Registry.register(Registries.PARTICLE_TYPE, Psychedelicraft.id(name), new ParticleType<>(alwaysShow, factoryGetter.apply(packetCodecGetter)) {
                @Override
                public Codec<T> getCodec() {
                    return codecGetter.apply(this).codec();
                }
            });
        }

    static void bootstrap() {}
}

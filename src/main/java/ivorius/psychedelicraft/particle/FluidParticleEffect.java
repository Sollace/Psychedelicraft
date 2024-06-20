package ivorius.psychedelicraft.particle;

import com.mojang.serialization.MapCodec;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record FluidParticleEffect(ParticleType<FluidParticleEffect> type, SimpleFluid fluid) implements ParticleEffect {
    public static MapCodec<FluidParticleEffect> createCodec(ParticleType<FluidParticleEffect> type) {
        return SimpleFluid.REGISTRY.getCodec().xmap(fluid -> new FluidParticleEffect(type, fluid), effect -> effect.fluid()).fieldOf("fluid");
    }

    public static PacketCodec<? super RegistryByteBuf, FluidParticleEffect> createPacketCodec(ParticleType<FluidParticleEffect> type) {
        return SimpleFluid.PACKET_CODEC.xmap(fluid -> new FluidParticleEffect(type, fluid), effect -> effect.fluid());
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}

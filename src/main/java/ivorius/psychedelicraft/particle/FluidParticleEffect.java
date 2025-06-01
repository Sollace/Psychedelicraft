package ivorius.psychedelicraft.particle;

import com.mojang.serialization.MapCodec;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record FluidParticleEffect(ParticleType<FluidParticleEffect> type, ItemFluids fluid) implements ParticleEffect {
    public static MapCodec<FluidParticleEffect> createCodec(ParticleType<FluidParticleEffect> type) {
        return ItemFluids.CODEC.xmap(fluid -> new FluidParticleEffect(type, fluid), effect -> effect.fluid()).fieldOf("fluid");
    }

    public static PacketCodec<? super RegistryByteBuf, FluidParticleEffect> createPacketCodec(ParticleType<FluidParticleEffect> type) {
        return ItemFluids.PACKET_CODEC.xmap(fluid -> new FluidParticleEffect(type, fluid), effect -> effect.fluid());
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}

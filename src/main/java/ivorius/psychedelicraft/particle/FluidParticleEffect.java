package ivorius.psychedelicraft.particle;

import java.util.function.Function;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.util.compat.PacketCodec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;

public record FluidParticleEffect(ParticleType<FluidParticleEffect> type, SimpleFluid fluid) implements ParticleEffect {
    public static MapCodec<FluidParticleEffect> createCodec(ParticleType<FluidParticleEffect> type) {
        return SimpleFluid.CODEC.xmap(fluid -> new FluidParticleEffect(type, fluid), effect -> effect.fluid()).fieldOf("fluid");
    }

    public static PacketCodec<? super PacketByteBuf, FluidParticleEffect> createPacketCodec(ParticleType<FluidParticleEffect> type) {
        return SimpleFluid.PACKET_CODEC.xmap(fluid -> new FluidParticleEffect(type, fluid), effect -> effect.fluid());
    }

    @SuppressWarnings("deprecation")
    public static ParticleEffect.Factory<FluidParticleEffect> createFactory(Function<ParticleType<FluidParticleEffect>, PacketCodec<? super PacketByteBuf, FluidParticleEffect>> codec) {
        return new Factory<>() {
            @Override
            public FluidParticleEffect read(ParticleType<FluidParticleEffect> type, StringReader reader)
                    throws CommandSyntaxException {
                reader.expect(' ');
                Identifier fluidId = new Identifier(reader.readQuotedString());
                return new FluidParticleEffect(type, SimpleFluid.REGISTRY.getOrEmpty(fluidId).orElseThrow());
            }

            @Override
            public FluidParticleEffect read(ParticleType<FluidParticleEffect> type, PacketByteBuf buf) {
                return codec.apply(type).decode(buf);
            }

        };
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }

    @Override
    public void write(PacketByteBuf buf) {
        createPacketCodec(type).encode(buf, this);
    }

    @Override
    public String asString() {
        return String.format("%s", fluid.getId().toString());
    }
}

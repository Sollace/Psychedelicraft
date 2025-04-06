package ivorius.psychedelicraft.particle;

import java.util.concurrent.atomic.AtomicReference;
import org.joml.Vector3f;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.util.compat.PacketCodec;
import ivorius.psychedelicraft.util.compat.PacketCodecs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.*;
import net.minecraft.util.dynamic.Codecs;

public class DrugDustParticleEffect extends DustParticleEffect {
    @SuppressWarnings("deprecation")
    static ParticleType<DrugDustParticleEffect> createType() {
        AtomicReference<ParticleType<DrugDustParticleEffect>> type = new AtomicReference<>();

        class Factory implements ParticleEffect.Factory<DrugDustParticleEffect> {
            private final PacketCodec<PacketByteBuf, DrugDustParticleEffect> packetCodec = PacketCodec.tuple(
                PacketCodecs.VECTOR3F, DustParticleEffect::getColor,
                PacketCodecs.FLOAT, DustParticleEffect::getScale,
                (color, scale) -> new DrugDustParticleEffect(type.get(), color, scale)
            );

            @Override
            public DrugDustParticleEffect read(ParticleType<DrugDustParticleEffect> type, StringReader reader) throws CommandSyntaxException {
                reader.expect(' ');
                float r = reader.readFloat();
                reader.expect(' ');
                float g = reader.readFloat();
                reader.expect(' ');
                float b = reader.readFloat();
                reader.expect(' ');
                float scale = reader.readFloat();
                return new DrugDustParticleEffect(type, new Vector3f(r, g, b), scale);
            }

            @Override
            public DrugDustParticleEffect read(ParticleType<DrugDustParticleEffect> type, PacketByteBuf buf) {
                return packetCodec.decode(buf);
            }
        }

        type.set(new ParticleType<>(false, new Factory()) {
            private final Codec<DrugDustParticleEffect> codec = RecordCodecBuilder.<DrugDustParticleEffect>create(instance -> instance.group(
                    Codecs.VECTOR_3F.fieldOf("color").forGetter(DustParticleEffect::getColor),
                    Codec.FLOAT.fieldOf("scale").forGetter(DustParticleEffect::getScale)
                ).apply(instance, (color, scale) -> new DrugDustParticleEffect(type.get(), color, scale)));

            @Override
            public Codec<DrugDustParticleEffect> getCodec() {
                return codec;
            }
        });

        return type.get();
    }

    private final ParticleType<DrugDustParticleEffect> type;

    public DrugDustParticleEffect(ParticleType<DrugDustParticleEffect> type, Vector3f color, float scale) {
        super(color, scale);
        this.type = type;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ParticleType<DustParticleEffect> getType() {
        return (ParticleType)type;
    }
}

package ivorius.psychedelicraft.particle;

import java.util.concurrent.atomic.AtomicReference;
import org.joml.Vector3f;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.*;
import net.minecraft.util.dynamic.Codecs;

public class DrugDustParticleEffect extends DustParticleEffect {
    static ParticleType<DrugDustParticleEffect> createType() {
        AtomicReference<ParticleType<DrugDustParticleEffect>> type = new AtomicReference<>();
        type.set(FabricParticleTypes.complex(RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codecs.VECTOR_3F.fieldOf("color").forGetter(DustParticleEffect::getColor),
                SCALE_CODEC.fieldOf("scale").forGetter(DustParticleEffect::getScale)
            ).apply(instance, (color, scale) -> new DrugDustParticleEffect(type.get(), color, scale))), PacketCodec.tuple(
                PacketCodecs.VECTOR3F, DustParticleEffect::getColor,
                PacketCodecs.FLOAT, DustParticleEffect::getScale,
                (color, scale) -> new DrugDustParticleEffect(type.get(), color, scale)
            )));
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

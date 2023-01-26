package ivorius.psychedelicraft.particle;

import org.joml.Vector3f;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.*;

public class ExhaledSmokeParticleEffect extends DustParticleEffect {
    @SuppressWarnings("deprecation")
    public static final Factory<ExhaledSmokeParticleEffect> FACTORY = new Factory<>(){
        @Override
        public ExhaledSmokeParticleEffect read(ParticleType<ExhaledSmokeParticleEffect> particleType, StringReader reader) throws CommandSyntaxException {
            Vector3f color = AbstractDustParticleEffect.readColor(reader);
            reader.expect(' ');
            float f = reader.readFloat();
            return new ExhaledSmokeParticleEffect(color, f);
        }

        @Override
        public ExhaledSmokeParticleEffect read(ParticleType<ExhaledSmokeParticleEffect> type, PacketByteBuf buffer) {
            return new ExhaledSmokeParticleEffect(AbstractDustParticleEffect.readColor(buffer), buffer.readFloat());
        }
    };

    public ExhaledSmokeParticleEffect(Vector3f color, float scale) {
        super(color, scale);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ParticleType<DustParticleEffect> getType() {
        return (ParticleType)PSParticles.EXHALED_SMOKE;
    }
}

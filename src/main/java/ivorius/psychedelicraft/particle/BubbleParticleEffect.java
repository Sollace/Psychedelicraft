package ivorius.psychedelicraft.particle;

import net.minecraft.util.math.Vec3f;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.*;

public class BubbleParticleEffect extends DustParticleEffect {
    @SuppressWarnings("deprecation")
    public static final Factory<BubbleParticleEffect> FACTORY = new Factory<>(){
        @Override
        public BubbleParticleEffect read(ParticleType<BubbleParticleEffect> particleType, StringReader reader) throws CommandSyntaxException {
            Vec3f color = AbstractDustParticleEffect.readColor(reader);
            reader.expect(' ');
            float f = reader.readFloat();
            return new BubbleParticleEffect(color, f);
        }

        @Override
        public BubbleParticleEffect read(ParticleType<BubbleParticleEffect> type, PacketByteBuf buffer) {
            return new BubbleParticleEffect(AbstractDustParticleEffect.readColor(buffer), buffer.readFloat());
        }
    };

    public BubbleParticleEffect(Vec3f color, float scale) {
        super(color, scale);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ParticleType<DustParticleEffect> getType() {
        return (ParticleType)PSParticles.BUBBLE;
    }
}

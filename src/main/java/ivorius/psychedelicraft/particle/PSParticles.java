package ivorius.psychedelicraft.particle;

import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.registry.Registry;

public interface PSParticles {
    ParticleType<ExhaledSmokeParticleEffect> EXHALED_SMOKE = register("exhaled_smoke", FabricParticleTypes.complex(ExhaledSmokeParticleEffect.FACTORY));
    ParticleType<BubbleParticleEffect> BUBBLE = register("bubble", FabricParticleTypes.complex(BubbleParticleEffect.FACTORY));

    static <T extends ParticleType<?>> T register(String name, T type) {
        return Registry.register(Registry.PARTICLE_TYPE, Psychedelicraft.id(name), type);
    }

    static void bootstrap() {}
}

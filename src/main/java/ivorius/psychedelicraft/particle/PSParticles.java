package ivorius.psychedelicraft.particle;

import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface PSParticles {
    ParticleType<ExhaledSmokeParticleEffect> EXHALED_SMOKE = register("exhaled_smoke", FabricParticleTypes.complex(ExhaledSmokeParticleEffect.FACTORY));

    static <T extends ParticleType<?>> T register(String name, T type) {
        return Registry.register(Registries.PARTICLE_TYPE, Psychedelicraft.id(name), type);
    }

    static void bootstrap() {}
}

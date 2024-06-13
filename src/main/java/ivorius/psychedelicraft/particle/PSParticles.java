package ivorius.psychedelicraft.particle;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface PSParticles {
    ParticleType<DrugDustParticleEffect> EXHALED_SMOKE = register("exhaled_smoke", DrugDustParticleEffect.createType());
    ParticleType<DrugDustParticleEffect> BUBBLE = register("bubble", DrugDustParticleEffect.createType());

    static <T extends ParticleType<?>> T register(String name, T type) {
        return Registry.register(Registries.PARTICLE_TYPE, Psychedelicraft.id(name), type);
    }

    static void bootstrap() {}
}

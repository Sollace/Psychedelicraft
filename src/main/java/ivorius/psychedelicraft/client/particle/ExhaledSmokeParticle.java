package ivorius.psychedelicraft.client.particle;

import org.joml.Vector3f;

import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.random.Random;

public class ExhaledSmokeParticle extends FireSmokeParticle {
    public ExhaledSmokeParticle(DrugDustParticleEffect effect, SpriteProvider spriteProvider, ClientWorld world,
            double x, double y, double z,
            double vX, double vY, double vZ, Random random) {
        super(world, x, y, z, vX, vY, vZ, 1, spriteProvider);
        Vector3f color = effect.getColor();
        red = color.x;
        green = color.y;
        blue = color.z;
    }
}

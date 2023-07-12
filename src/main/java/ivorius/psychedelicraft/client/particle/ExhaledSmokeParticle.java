package ivorius.psychedelicraft.client.particle;

import net.minecraft.util.math.Vec3f;

import ivorius.psychedelicraft.particle.ExhaledSmokeParticleEffect;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;

public class ExhaledSmokeParticle extends FireSmokeParticle {
    public ExhaledSmokeParticle(ExhaledSmokeParticleEffect effect, SpriteProvider spriteProvider, ClientWorld world,
            double x, double y, double z,
            double vX, double vY, double vZ) {
        super(world, x, y, z, vX, vY, vZ, 1, spriteProvider);
        Vec3f color = effect.getColor();
        red = color.getX();
        green = color.getY();
        blue = color.getZ();
    }
}

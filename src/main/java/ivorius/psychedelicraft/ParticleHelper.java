package ivorius.psychedelicraft;

import org.joml.Vector3f;

import net.minecraft.entity.Entity;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.util.math.Vec3d;

public interface ParticleHelper {

    static void spawnColoredParticle(Entity entity, float[] color, Vec3d direction, float speed, float size) {
        var c = new Vector3f(color[0], color[1], color[2]);
        Vec3d velocity = entity.getVelocity().add(direction.multiply(speed));
        entity.world.addParticle(new DustColorTransitionParticleEffect(c, c, size),
                entity.getX(), entity.getY() - 0.15F, entity.getZ(),
                velocity.x, velocity.y, velocity.z);
    }

}

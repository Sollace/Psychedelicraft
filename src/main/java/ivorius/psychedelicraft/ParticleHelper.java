package ivorius.psychedelicraft;

import java.util.function.Supplier;

import org.joml.Vector3f;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minecraft.entity.Entity;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ParticleHelper {

    static void spawnColoredParticle(Entity entity, float[] color, Vec3d direction, float speed, float size) {
        var c = new Vector3f(color[0], color[1], color[2]);
        Vec3d velocity = entity.getVelocity().add(direction.multiply(speed));
        entity.world.addParticle(new DustColorTransitionParticleEffect(c, c, size),
                entity.getX(), entity.getY() - 0.15F, entity.getZ(),
                velocity.x, velocity.y, velocity.z);
    }

    static void spawnParticles(World world, ParticleEffect effect, Supplier<Vec3d> pos, Supplier<Vec3d> vel, int count) {
        for (int i = 0; i < count; i++) {
            Vec3d position = pos.get();
            Vec3d velocity = vel.get();
            world.addParticle(effect, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
        }
    }

    static Vec3d apply(Vec3d vector, Double2DoubleFunction function) {
        return new Vec3d(
                function.applyAsDouble(vector.x),
                function.applyAsDouble(vector.y),
                function.applyAsDouble(vector.z)
        );
    }
}

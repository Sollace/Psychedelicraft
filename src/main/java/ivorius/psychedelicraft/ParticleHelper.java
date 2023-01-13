package ivorius.psychedelicraft;

import java.util.function.Supplier;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minecraft.entity.Entity;
import net.minecraft.particle.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ParticleHelper {

    static void spawnColoredParticle(Entity entity, float[] color, Vec3d direction, float speed, float size) {
        //var c = new Vector3f(color[0], color[1], color[2]);
        Vec3d velocity = entity.getVelocity().add(direction.normalize().multiply(speed));
        Vec3d pos = entity.getEyePos();
        // TODO: (Sollace) Register a custom particle type that supports setting a color
        entity.world.addParticle(ParticleTypes.SMOKE,
                pos.x, pos.y - 0.1F, pos.z,
                velocity.x, velocity.y + 0.03F, velocity.z);
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

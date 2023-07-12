package ivorius.psychedelicraft;

import java.util.function.Supplier;

import net.minecraft.util.math.Vec3f;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import ivorius.psychedelicraft.particle.ExhaledSmokeParticleEffect;
import net.minecraft.entity.Entity;
import net.minecraft.particle.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ParticleHelper {

    static void spawnColoredParticle(Entity entity, Vec3f color, Vec3d direction, float speed, float size) {
        Vec3d velocity = entity.getVelocity().add(direction.normalize().multiply(speed));
        Vec3d pos = entity.getEyePos();
        entity.world.addParticle(new ExhaledSmokeParticleEffect(color, 1),
                pos.x, pos.y - 0.1F, pos.z,
                velocity.x, velocity.y + 0.03F, velocity.z);
    }

    static void spawnParticles(World world, ParticleEffect effect, Supplier<Vec3d> pos, Supplier<Vec3d> vel, int count) {
        for (int i = 0; i < count; i++) {
            Vec3d position = pos.get();
            Vec3d velocity = vel.get();
            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(effect, position.x, position.y, position.z, 1, velocity.x, velocity.y, velocity.z, 0);
            } else {
                world.addParticle(effect, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
            }
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

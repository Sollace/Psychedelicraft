package ivorius.psychedelicraft;

import java.util.function.Supplier;

import org.joml.Vector3f;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.entity.Entity;
import net.minecraft.particle.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ParticleHelper {

    static void spawnColoredParticle(Entity entity, Vector3f color, float speed, float size) {
        spawnParticleAtFace(entity, new DrugDustParticleEffect(PSParticles.EXHALED_SMOKE, MathUtils.getArgb(color), size), speed);
    }

    static void spawnParticleAtFace(Entity entity, ParticleEffect effect, float speed) {
        Vec3d velocity = entity.getVelocity().add(entity.getRotationVec(1).normalize().multiply(speed));
        Vec3d pos = entity.getEyePos();
        entity.getWorld().addParticle(effect, pos.x, pos.y - 0.1F, pos.z, velocity.x, velocity.y + 0.03F, velocity.z);
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

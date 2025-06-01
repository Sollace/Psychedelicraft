package ivorius.psychedelicraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public interface RaytraceUtil {

    static EntityHitResult raycastEntities(Entity entity, double maxDistance) {
        double sqrtDistance = Math.sqrt(maxDistance);
        Vec3d start = entity.getEyePos();
        Vec3d rotation = entity.getRotationVec(1);
        Vec3d end = start.add(rotation.x * sqrtDistance, rotation.y * sqrtDistance, rotation.z * sqrtDistance);
        Box box = entity.getBoundingBox().stretch(end.multiply(sqrtDistance)).expand(1, 1, 1);
        return ProjectileUtil.raycast(entity, start, end, box, e -> !e.isSpectator() && e.canHit(), maxDistance);
    }
}

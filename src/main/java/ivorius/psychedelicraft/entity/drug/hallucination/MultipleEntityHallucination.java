/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.List;
import java.util.stream.IntStream;

import ivorius.psychedelicraft.PSTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MultipleEntityHallucination extends EntityHallucination<LivingEntity> {
    public List<InstancePosition> positions;

    public MultipleEntityHallucination(PlayerEntity player) {
        super(HallucinationTypeKeys.MULTIPLE_ENTITY, player, EntityHallucination.createEntity(player.getEntityWorld(), PSTags.Entities.MULTIPLE_ENTITY_HALLUCINATIONS));
        positions = IntStream.range(0, random.nextBetween(5, 15)).mapToObj(i -> {
            double distance = Math.max(2, i / 10F);
            return new InstancePosition(
                    random.nextTriangular(0, distance),
                    random.nextTriangular(0, distance),
                    random.nextTriangular(0, distance)
            );
        }).toList();
    }

    @Override
    public void update(float alpha) {
        super.update(alpha);

        if (alpha < MathHelper.EPSILON) {
            return;
        }
        positions.forEach(InstancePosition::update);
    }

    public class InstancePosition {
        private double x;
        private double y;
        private double z;

        private double lastX;
        private double lastY;
        private double lastZ;

        public InstancePosition(double x, double y, double z) {
            this.x = lastX = x;
            this.y = lastY = y;
            this.z = lastZ = z;
        }

        public Vec3d pos(float tickDelta) {
            return new Vec3d(
                    MathHelper.lerp(tickDelta, lastX, x),
                    MathHelper.lerp(tickDelta, lastY, y),
                    MathHelper.lerp(tickDelta, lastZ, z)
            );
        }

        public void update() {
            lastX = x;
            lastY = y;
            lastZ = z;
            x += random.nextTriangular(0, 0.2);
            y += random.nextTriangular(0, 0.2);
            z += random.nextTriangular(0, 0.2);
        }
    }
}

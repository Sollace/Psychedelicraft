package ivorius.psychedelicraft.entity.drug.hallucination;

import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class DriftingCamera {

    private double distance;
    private double totalRotation;

    private Vec3d prevPosition = Vec3d.ZERO;
    private Vec3d prevRotation = Vec3d.ZERO;

    private Vec3d position = Vec3d.ZERO;
    private Vec3d velocity = Vec3d.ZERO;
    private Vec3d rotation = Vec3d.ZERO;

    private double accellerationX;
    private double accellerationY;
    private double accellerationZ;

    public Vec3d getPosition() {
        return position;
    }

    public Vec3d getRotation() {
        return rotation;
    }

    public Vec3d getPrevPosition() {
        return prevPosition;
    }

    public Vec3d getPrevRotation() {
        return prevRotation;
    }

    public void update(DrugProperties properties) {
        prevPosition = position;
        prevRotation = rotation;

        float weightlessness = properties.getModifier(Drug.WEIGHTLESSNESS) * MathHelper.sin(properties.age / 100F);//

        if (weightlessness != 0) {
            PlayerEntity player = properties.asEntity();

            Vec3d entityVel = properties.asEntity().getVelocity();
            velocity = velocity.add(0, -0.03, 0);
            velocity = velocity.subtract(
                    MathHelper.clamp(entityVel.x * 0.001F, -0.2, 0.2),
                    0,
                    MathHelper.clamp(entityVel.z * 0.001F, -0.2, 0.2)
            ).add(accellerationX, accellerationY, accellerationZ);
            Random random = player.getRandom();

            if (distance > 10) {
                accellerationX = 0;
                accellerationY = (-0.5F / distance) - 0.0001F;
                accellerationZ = 0;
            } else {
                accellerationY = 0;
                if (random.nextFloat() < 0.02) {
                    accellerationX = Math.sin((random.nextFloat() - 0.5) * weightlessness * 2 * MathHelper.PI) * random.nextFloat() / 3F;
                } else if (random.nextFloat() < 0.02) {
                    accellerationZ = Math.cos((random.nextFloat() - 0.5) * weightlessness * 2 * MathHelper.PI) * random.nextFloat() / 3F;
                }
            }

            rotation = new Vec3d(
                    rotation.x % MathHelper.PI,
                    rotation.y % MathHelper.PI,
                    rotation.z % MathHelper.PI
            ).add(
                    0.001 * MathHelper.sin(properties.age / 200F),
                    0.001 * MathHelper.sin(properties.age / 300F),
                    0.001 * MathHelper.sin(properties.age / 400F)
            );
            totalRotation = rotation.lengthSquared();

            position = position.add(velocity.multiply(weightlessness));
            if (position.y > 0) {
                position = position.multiply(1, 0, 1);
            }
            distance = position.lengthSquared();
            velocity = velocity.multiply(0.999 / Math.max(distance / 10, 1));
        } else {
            velocity = Vec3d.ZERO;
            accellerationX = 0;
            accellerationZ = 0;
            if (distance > 0) {
                position = position.multiply(0.9);
                distance = position.lengthSquared();
            }
            if (totalRotation > 0) {
                rotation = rotation.multiply(0.9);
                totalRotation = rotation.lengthSquared();
            }
        }
    }
}

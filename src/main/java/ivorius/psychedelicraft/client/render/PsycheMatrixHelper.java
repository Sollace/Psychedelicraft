/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import org.joml.*;

/**
 * Created by lukas on 09.03.14.
 */
public class PsycheMatrixHelper {
    private static final Camera IDLE_CAMERA = new Camera();
    private static Matrix4f getProjectionMatrix(Camera camera) {
        Vec3d pos = camera.getPos();
        Matrix4f matrix = new Matrix4f();
        matrix.translate((float)pos.x, (float)pos.y, (float)pos.z);
        matrix.rotate(camera.getRotation());
        matrix.translate((float)-pos.x, (float)-pos.y, (float)-pos.z);
        return matrix;
    }

    public static Matrix4f getCurrentProjectionMatrix(float partialTicks) {
        return getProjectionMatrix(MinecraftClient.getInstance().gameRenderer.getCamera());

        /*MinecraftClient mc = MinecraftClient.getInstance();
        final float farPlaneDistance = mc.options.getViewDistance().getValue() * 16;
        return IvMatrixHelper.getProjectionMatrix(
                getCurrentFOV(partialTicks, true) * MathHelper.RADIANS_PER_DEGREE,
                (float) mc.getWindow().getScaledWidth() / (float) mc.getWindow().getScaledHeight(), 0.05f,
                farPlaneDistance * 2
        );*/
    }

    @Deprecated(since = "unused")
    public static Matrix4f getLookProjectionMatrix(Matrix4f projectionMatrix, Entity entity) {
        //final float roll = 0;
        /* return IvMatrixHelper.lookFrom(
         *      (float) entity.posX, (float) entity.posY,
         *      (float) entity.posZ, (entity.rotationYaw + 180.0f) * degToRad,
         *      entity.rotationPitch * degToRad, roll * degToRad, projectionMatrix, projectionMatrix);
         *
         */
        // Entity is in center implicitly when drawn

        if (entity == MinecraftClient.getInstance().player || entity == MinecraftClient.getInstance().cameraEntity) {
            return getCurrentProjectionMatrix(1);
        }

        IDLE_CAMERA.update(entity.world, entity, false, false, MinecraftClient.getInstance().getTickDelta());
        return getProjectionMatrix(IDLE_CAMERA);
/*
        return IvMatrixHelper.lookFrom(0, 0, 0,
                (entity.getYaw(1) + 180) * MathHelper.RADIANS_PER_DEGREE,
                entity.getPitch(1) * MathHelper.RADIANS_PER_DEGREE,
                roll * MathHelper.RADIANS_PER_DEGREE, projectionMatrix, projectionMatrix);*/
    }

    @Deprecated(since = "unused")
    public static Vector3f projectPoint(Entity entity, Vector3f point, float partialTicks) {
        Matrix4f projection = getLookProjectionMatrix(getCurrentProjectionMatrix(partialTicks), entity);
        return to3F(projection.transform(new Vector4f(point, 1f))).mul(1, -1, 1);
    }

    static Vector3f to3F(Vector4f vector) {
        return new Vector3f(vector.x, vector.y, vector.z);
    }

    public static Vector3f projectPointCurrentView(Vector3f point, float partialTicks) {
        return to3F(getCurrentProjectionMatrix(partialTicks).transform(new Vector4f(point, 1)));
        /*
        Minecraft mc = Minecraft.getMinecraft();
        Entity renderViewEntity = mc.renderViewEntity;

        Matrix4f transformMatrix = getCurrentProjectionMatrix(partialTicks);

        // Copied from EntityRenderer
        if (mc.gameSettings.thirdPersonView > 0) {
            float f1 = (float)renderViewEntity.getHeightOffset() - 1.62F;
            double d0 = MathHelper.lerp(renderViewEntity.prevX, renderViewEntity.getX(), partialTicks);
            double d1 = MathHelper.lerp(renderViewEntity.prevY, renderViewEntity.getY(), partialTicks) - f1;
            double d2 = MathHelper.lerp(renderViewEntity.prevZ, renderViewEntity.getZ(), partialTicks);
//          double d7 = (double)(entityRenderer.thirdPersonDistanceTemp + (entityRenderer.thirdPersonDistance - entityRenderer.thirdPersonDistanceTemp) * partialTicks);

            double d7 = 4F;
            float f6 = renderViewEntity.getYaw(partialTicks);
            float f2 = renderViewEntity.getPitch(partialTicks);

            if (mc.gameSettings.thirdPersonView == 2) {
                f2 += 180;
            }

            double d3 = -MathHelper.sin(f6 / 180F * (float) Math.PI) * MathHelper.cos(f2 / 180F * (float) Math.PI) * d7;
            double d4 = MathHelper.cos(f6 / 180F * (float) Math.PI) * MathHelper.cos(f2 / 180F * (float) Math.PI) * d7;
            double d5 = (-MathHelper.sin(f2 / 180F * (float) Math.PI)) * d7;

            for (int k = 0; k < 8; ++k)
            {
                float f3 = (k & 1) * 2 - 1;
                float f4 = (k >> 1 & 1) * 2 - 1;
                float f5 = (k >> 2 & 1) * 2 - 1;
                f3 *= 0.1F;
                f4 *= 0.1F;
                f5 *= 0.1F;
                MovingObjectPosition movingobjectposition = mc.theWorld.rayTraceBlocks(
                        new Vec3d(d0 + f3, d1 + f4, d2 + f5),
                        new Vec3d(d0 - d3 + f3 + f5, d1 - d5 + f4, d2 - d4 + f5)
                    );

                if (movingobjectposition != null) {
                    d7 = Math.min(d7, movingobjectposition.hitVec.distanceTo(new Vec3d(d0, d1, d2)));

                }
            }

            if (mc.gameSettings.thirdPersonView == 2)
            {
                Matrix4f.rotate(180, new Vector3f(0, 1, 0), transformMatrix, transformMatrix);
            }

            Matrix4f.rotate(renderViewEntity.rotationPitch - f2, new Vector3f(1, 0, 0), transformMatrix, transformMatrix);
            Matrix4f.rotate(renderViewEntity.rotationYaw - f6, new Vector3f(0, 1, 0), transformMatrix, transformMatrix);
            Matrix4f.translate(new Vector3f(0, 0, (float) (-d7)), transformMatrix, transformMatrix);
            Matrix4f.rotate(f6 - renderViewEntity.rotationYaw, new Vector3f(0, 1, 0), transformMatrix, transformMatrix);
            Matrix4f.rotate(f2 - renderViewEntity.rotationPitch, new Vector3f(1, 0, 0), transformMatrix, transformMatrix);
        }

        transformMatrix = getLookProjectionMatrix(transformMatrix, renderViewEntity);

        Vector4f clippedPoint = new Vector4f(point.x, point.y, point.z, 1.0f);
        Matrix4f.transform(transformMatrix, clippedPoint, clippedPoint);
        return new Vector3f(clippedPoint.x, -clippedPoint.y, clippedPoint.z);
        */
    }

    /**
     * Gets the current FOV
     */
    @Deprecated
    public static float getCurrentFOV(float tickDelta, boolean isWorld) {
        // TODO: (Sollace) implement fov accessor
        // return MinecraftClient.getInstance().gameRenderer.getFov(MinecraftClient.getInstance().gameRenderer.getCamera(), tickDelta, isWorld);
        return 90F;
    }
}

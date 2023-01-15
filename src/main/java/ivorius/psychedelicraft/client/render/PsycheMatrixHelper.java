/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.MathHelper;

import org.joml.*;

/**
 * Created by lukas on 09.03.14.
 * Updated by Sollace on 15 Jan 2023
 */
public class PsycheMatrixHelper {
    private static Matrix4f getProjectionMatrix(Camera camera) {
        return new Matrix4f()
            .translate(camera.getPos().toVector3f().mul(-1))
            .rotate(new Quaternionf(camera.getRotation()).invert());
    }

    public static Matrix4f getProjectionMatrix() {
        return getProjectionMatrix(MinecraftClient.getInstance().gameRenderer.getCamera());
    }

    private static Vector3f projectPointView(Camera camera, Vector3f point) {
        return to3F(getProjectionMatrix(camera).translate(point).transform(new Vector4f(point, 1)));
    }

    public static Vector3f projectPointCurrentView(Vector3f point) {
        return projectPointView(MinecraftClient.getInstance().gameRenderer.getCamera(), point);
    }

    public static Vector2f fromPolar(float angle, float distance) {
        return new Vector2f(MathHelper.sin(angle) * distance, MathHelper.cos(angle) * distance);
    }

    static Vector3f to3F(Vector4f vector) {
        return new Vector3f(vector.x, vector.y, vector.z);
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;

/**
 * Created by lukas on 09.03.14.
 * Updated by Sollace on 15 Jan 2023
 */
public class PsycheMatrixHelper {
    private static Matrix4f getProjectionMatrix(Camera camera) {
        var mat = new Matrix4f();
        var rotation = new Quaternion(camera.getRotation());
        rotation.conjugate();
        mat.multiply(rotation);
        return mat;
    }

    private static Vec3f projectPointView(Camera camera, Vec3f point) {
        var vec = new Vector4f(point);
        vec.transform(getProjectionMatrix(camera));
        return to3F(vec);
    }

    public static Vec3f projectPointCurrentView(Vec3f point) {
        return projectPointView(MinecraftClient.getInstance().gameRenderer.getCamera(), point);
    }

    public static Vector2f fromPolar(float angle, float distance) {
        return new Vector2f(MathHelper.sin(angle) * distance, MathHelper.cos(angle) * distance);
    }

    static Vec3f to3F(Vector4f vector) {
        return new Vec3f(vector.getX(), vector.getY(), vector.getZ());
    }
}

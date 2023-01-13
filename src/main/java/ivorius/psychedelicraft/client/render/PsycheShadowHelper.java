/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.render.shader.program.ShaderShadows;

/**
 * Created by lukas on 24.03.14.
 */
public class PsycheShadowHelper {
    public static void setupSunGLTransform() {
        int shadowPixels = ShaderShadows.getShadowPixels();
        RenderSystem.viewport(0, 0, shadowPixels, shadowPixels);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(getSunProjectionMatrix());
        MatrixStack modelView = RenderSystem.getModelViewStack();
        modelView.push();
        modelView.loadIdentity();
        modelView.multiplyPositionMatrix(getSunViewMatrix());
        RenderSystem.applyModelViewMatrix();
    }

    public static void restoreOriginalTransform() {
        RenderSystem.restoreProjectionMatrix();

        MatrixStack modelView = RenderSystem.getModelViewStack();
        modelView.pop();
        RenderSystem.applyModelViewMatrix();
    }

    public static Matrix4f getSunMatrix() {
        return getSunProjectionMatrix().mul(getSunViewMatrix());
    }

    public static Matrix4f getSunProjectionMatrix() {
        float farPlaneDistance = MinecraftClient.getInstance().options.getViewDistance().getValue() * 16;
        return new Matrix4f().ortho(-farPlaneDistance, farPlaneDistance, -farPlaneDistance, farPlaneDistance, getSunZNear(), getSunZFar());
    }

    public static Matrix4f getSunViewMatrix() {
        float tickDelta = MinecraftClient.getInstance().getTickDelta();
        float sunRadians = MinecraftClient.getInstance().world.getSkyAngleRadians(tickDelta);
        Matrix4f sunMatrix = new Matrix4f().identity();
        sunMatrix.translate(0, 0, -300);
        sunMatrix.rotate(new Quaternionf().rotateXYZ(-sunRadians + MathHelper.HALF_PI, MathHelper.HALF_PI, 0));
        return sunMatrix;
    }

    public static Matrix4f getInverseViewMatrix(float partialTicks) {
//        return (Matrix4f) getSunMatrix().invert();
        //Entity renderEntity = MinecraftClient.getInstance().cameraEntity;
        return PsycheMatrixHelper.getCurrentProjectionMatrix(partialTicks);
        //return PsycheMatrixHelper.getLookProjectionMatrix(PsycheMatrixHelper.getCurrentProjectionMatrix(partialTicks), renderEntity).invert();
    }

    public static float getSunZFar() {
        return (MinecraftClient.getInstance().options.getViewDistance().getValue() * 16) + 300f;
    }

    public static float getSunZNear() {
        return 10.0f;
    }

    public static float getShadowBias() {
        return 0.001f;
    }
}

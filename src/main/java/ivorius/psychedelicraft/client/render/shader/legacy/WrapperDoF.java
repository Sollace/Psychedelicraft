/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.shader.legacy.program.IvShaderInstance2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperDoF extends ShaderWrapper<WrapperDoF.ShaderDoF> {
    public WrapperDoF(String utils) {
        super(new ShaderDoF(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("depthOfField.frag"), utils);
    }

    public boolean isActive() {
        return (PsychedelicraftClient.getConfig().visual.dofFocalBlurFar > 0 || PsychedelicraftClient.getConfig().visual.dofFocalBlurNear > 0)
            && (PsychedelicraftClient.getConfig().visual.dofFocalPointNear > 0 || PsychedelicraftClient.getConfig().visual.dofFocalPointFar < getCurrentZFar());
    }

    protected float getCurrentZFar() {
        return MinecraftClient.getInstance().options.getViewDistance().getValue() * 16;
    }

    @Override
    public void setShaderValues(float tickDelta, int ticks, @Nullable Framebuffer buffer) {
        if (buffer != null && isActive()) {
            shaderInstance.depthTextureIndex = buffer.getDepthAttachment();

            shaderInstance.zNear = 0.05f;
            shaderInstance.zFar = getCurrentZFar();

            shaderInstance.focalPointNear = PsychedelicraftClient.getConfig().visual.dofFocalPointNear / shaderInstance.zFar;
            shaderInstance.focalPointFar = PsychedelicraftClient.getConfig().visual.dofFocalPointFar / shaderInstance.zFar;
            shaderInstance.focalBlurFar = PsychedelicraftClient.getConfig().visual.dofFocalBlurFar;
            shaderInstance.focalBlurNear = PsychedelicraftClient.getConfig().visual.dofFocalBlurNear;
        } else {
            shaderInstance.focalBlurFar = 0.0f;
            shaderInstance.focalBlurNear = 0.0f;
        }
    }

    @Override
    public boolean wantsDepthBuffer(float partialTicks) {
        return isActive();
    }

    public static class ShaderDoF extends IvShaderInstance2D {
        public int depthTextureIndex;

        public float zNear;
        public float zFar;

        public float focalPointNear;
        public float focalBlurNear;

        public float focalPointFar;
        public float focalBlurFar;

        public ShaderDoF(Logger logger) {
            super(logger);
        }

        @Override
        public boolean shouldApply(float ticks) {
            return depthTextureIndex > 0 && (focalBlurNear > 0 || focalBlurFar > 0);
        }

        @Override
        public void render(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
            useShader();

            RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 1, depthTextureIndex);
            setUniformInts("depthTex", 2);
            RenderSystem.activeTexture(GLStateProxy.DEFAULT_TEXTURE);
            setUniformInts("tex", 0);
            setUniformFloats("pixelSize", 1.0f / screenWidth, 1.0f / screenHeight);
            setUniformFloats("focalPointNear", focalPointNear);
            setUniformFloats("focalPointFar", focalPointFar);

            float maxDof = Math.max(focalBlurFar, focalBlurNear);

            for (int n = 0; n < MathHelper.ceil(maxDof); n++) {
                float curBlurNear = MathHelper.clamp(focalBlurNear - n, 0, 1);
                float curBlurFar = MathHelper.clamp(focalBlurFar - n, 0, 1);

                if (curBlurNear > 0.0f || curBlurFar > 0.0f) {
                    setUniformFloats("focalBlurNear", curBlurNear);
                    setUniformFloats("focalBlurFar", curBlurFar);

                    for (int i = 0; i < 2; i++) {
                        setUniformInts("vertical", i);
                        drawFullScreen(screenWidth, screenHeight, pingPong);
                    }
                }
            }

            setUniformFloats("depthRange", zNear, zFar);
            stopUsingShader();
        }
    }
}

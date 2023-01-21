/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.shader.legacy.program.IvShaderInstance2D;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperDigital implements EffectWrapper {
    private final WrapperDigitalMD digitalMD;
    private final WrapperDigitalPD digitalPD;

    public WrapperDigital(String utils) {
        digitalMD = new WrapperDigitalMD(utils);
        digitalPD = new WrapperDigitalPD(utils);
    }

    @Override
    public void update() {
        digitalMD.update();
        digitalPD.update();
    }

    @Override
    public void apply(float partialTicks, IvShaderInstance2D.PingPong pingPong, @Nullable Framebuffer depthBuffer) {
        if (depthBuffer != null) {
            digitalPD.apply(partialTicks, pingPong, depthBuffer);
        } else {
            digitalMD.apply(partialTicks, pingPong, depthBuffer);
        }
    }

    @Override
    public boolean wantsDepthBuffer(float partialTicks) {
        return digitalPD.wantsDepthBuffer(partialTicks) || digitalMD.wantsDepthBuffer(partialTicks);
    }

    public static class ShaderDigital extends IvShaderInstance2D {
        public float digital;
        public float[] maxDownscale;

        public int digitalTextTexture;

        public ShaderDigital(Logger logger) {
            super(logger);
        }

        @Override
        public boolean shouldApply(float ticks) {
            return digital > 0 && digitalTextTexture > 0;
        }

        @Override
        public void render(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
            useShader();
            uploadTextures();
            setUniformFloats("totalAlpha", 1);
            uploadUniforms(screenWidth, screenHeight);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            drawFullScreen(screenWidth, screenHeight, pingPong);
            stopUsingShader();
        }

        protected void uploadTextures() {
            RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE, digitalTextTexture);
            for (int i = 0; i < 3; i++) {
                setUniformInts("tex" + i, i);
            }
        }

        protected void uploadUniforms(int screenWidth, int screenHeight) {
            float downscale = MathUtils.mixEaseInOut(0, 0.95F, Math.min(digital * 3, 1));
            downscale += digital * 0.05f; //Bigger pixels!

            setUniformFloats("newResolution",
                    screenWidth * (1 + (maxDownscale[0] - 1) * downscale),
                    screenHeight * (1 + (maxDownscale[1] - 1) * downscale)
            );

            float textProgress = MathUtils.easeZeroToOne((digital - 0.2F) * 5);
            float binaryProgress = MathUtils.easeZeroToOne((digital - 0.8F) * 10);

            setUniformFloats("textProgress", textProgress + binaryProgress);
            setUniformFloats("maxColors", digital > 0.4F ? (Math.max(256F / ((digital - 0.4F) * 640 + 1), 2)) : -1); //Step 3, 0.2 is enough for only 2 colors
            setUniformFloats("saturation", 1 - MathUtils.easeZeroToOne((digital - 0.6F) * 5));
        }
    }

}

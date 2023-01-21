/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.shader.legacy.program.IvShaderInstance2D;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperHeatDistortion extends ShaderWrapper<WrapperHeatDistortion.ShaderHeatDistortions> {
    public Identifier heatDistortionNoiseTexture = Psychedelicraft.id("textures/environment/heat_distortion_noise.png");

    public WrapperHeatDistortion(String utils) {
        super(new ShaderHeatDistortions(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderHeatDistortion.frag"), utils);
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer) {

        if (PsychedelicraftClient.getConfig().visual.doHeatDistortion && depthBuffer != null) {
            float heatDistortion = DrugRenderer.INSTANCE.getEnvironmentalEffects().getHeatDistortion();

            shaderInstance.depthTextureIndex = depthBuffer.getDepthAttachment();
            shaderInstance.noiseTextureIndex = GLStateProxy.getTextureId(heatDistortionNoiseTexture);

            shaderInstance.strength = heatDistortion;
            shaderInstance.wobbleSpeed = 0.15f;
        } else {
            shaderInstance.strength = 0;
        }
    }

    @Override
    public boolean wantsDepthBuffer(float partialTicks) {
        return PsychedelicraftClient.getConfig().visual.doHeatDistortion && DrugRenderer.INSTANCE.getEnvironmentalEffects().getHeatDistortion() > 0;
    }

    public static class ShaderHeatDistortions extends IvShaderInstance2D {
        public float strength;
        public float wobbleSpeed;
        public int depthTextureIndex;
        public int noiseTextureIndex;

        public ShaderHeatDistortions(Logger logger) {
            super(logger);
        }

        @Override
        public boolean shouldApply(float ticks) {
            return strength > 0 && depthTextureIndex > 0 && noiseTextureIndex > 0;
        }

        @Override
        public void render(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
            useShader();

            RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 2, noiseTextureIndex);
            RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 1, depthTextureIndex);
            RenderSystem.activeTexture(GLStateProxy.DEFAULT_TEXTURE);

            for (int i = 0; i < 3; i++) {
                setUniformInts("tex" + i, i);
            }
            setUniformInts("noiseTex", 3);
            setUniformFloats("totalAlpha", 1.0f);
            setUniformFloats("ticks", ticks * wobbleSpeed);
            setUniformFloats("strength", strength);

            drawFullScreen(screenWidth, screenHeight, pingPong);

            stopUsingShader();
        }
    }
}

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
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.shader.legacy.program.IvShaderInstance2D;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperWaterOverlay extends ShaderWrapper<WrapperWaterOverlay.ShaderDistortionMap> {
    public Identifier waterDropletsDistortionTexture = Psychedelicraft.id("textures/environment/water_distortion.png");

    public WrapperWaterOverlay(String utils) {
        super(new ShaderDistortionMap(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderDistortionMap.frag"), utils);
    }

    @Override
    public void setShaderValues(float tickDelta, int ticks, @Nullable Framebuffer buffer) {
        DrugProperties drugProperties = DrugProperties.of(MinecraftClient.getInstance().player);

        if (drugProperties != null && PsychedelicraftClient.getConfig().visual.waterOverlayEnabled) {
            float waterScreenDistortion = DrugRenderer.INSTANCE.getEnvironmentalEffects().getWaterScreenDistortion();
            shaderInstance.strength = waterScreenDistortion * 0.2F;
            shaderInstance.alpha = waterScreenDistortion;
            shaderInstance.noiseTextureIndex0 = GLStateProxy.getTextureId(waterDropletsDistortionTexture);
            shaderInstance.noiseTextureIndex1 = GLStateProxy.getTextureId(waterDropletsDistortionTexture);
            shaderInstance.texTranslation0 = new float[]{0, ticks * 0.005F};
            shaderInstance.texTranslation1 = new float[]{0.5F, ticks * 0.007F};
        } else {
            shaderInstance.strength = 0;
        }
    }

    public static class ShaderDistortionMap extends IvShaderInstance2D {
        public float strength;
        public float alpha = 1;

        public int noiseTextureIndex0;
        public int noiseTextureIndex1;

        public float[] texTranslation0;
        public float[] texTranslation1;

        public ShaderDistortionMap(Logger logger) {
            super(logger);
        }

        @Override
        public boolean shouldApply(float ticks) {
            return strength > 0
                    && alpha > 0
                    && noiseTextureIndex0 > 0
                    && noiseTextureIndex1 > 0;
        }

        @Override
        public void render(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
            useShader();
            RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 1, noiseTextureIndex0);
            RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 2, noiseTextureIndex1);
            RenderSystem.activeTexture(GLStateProxy.DEFAULT_TEXTURE);
            setUniformInts("tex0", 0);
            setUniformInts("tex1", 1);
            setUniformInts("noiseTex0", 2);
            setUniformInts("noiseTex1", 3);
            setUniformFloats("totalAlpha", alpha);
            setUniformFloats("strength", strength);
            setUniformFloats("texTranslation0", texTranslation0);
            setUniformFloats("texTranslation1", texTranslation1);
            drawFullScreen(screenWidth, screenHeight, pingPong);

            stopUsingShader();
        }
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperDigitalPD extends ShaderWrapper<WrapperDigitalPD.ShaderDigitalDepth> {
    public WrapperDigitalPD(String utils) {
        super(new ShaderDigitalDepth(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderDigitalDepth.frag"), utils);
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer) {
        DrugProperties drugProperties = DrugProperties.of(MinecraftClient.getInstance().player);

        if (drugProperties != null && depthBuffer != null) {
            shaderInstance.digital = drugProperties.getDrugValue(DrugType.ZERO);
            shaderInstance.maxDownscale = drugProperties.getDigitalEffectPixelResize();
            shaderInstance.digitalTextTexture = GLStateProxy.getTextureId(WrapperDigitalMD.DIGITAL_TEXTURE);
            shaderInstance.depthTextureIndex = depthBuffer.getDepthAttachment();

            shaderInstance.zNear = 0.05f;
            shaderInstance.zFar = MinecraftClient.getInstance().options.getViewDistance().getValue() * 16;
        } else {
            shaderInstance.digital = 0;
        }
    }

    @Override
    public boolean wantsDepthBuffer(float partialTicks) {
        return DrugProperties.of(MinecraftClient.getInstance().player).getDrugValue(DrugType.ZERO) > 0;
    }

    public static class ShaderDigitalDepth extends WrapperDigitalMD.ShaderDigital {
        public int depthTextureIndex;

        public float zNear;
        public float zFar;

        public ShaderDigitalDepth(Logger logger) {
            super(logger);
        }

        @Override
        public boolean shouldApply(float ticks) {
            return depthTextureIndex > 0 && super.shouldApply(ticks);
        }

        @Override
        protected void uploadTextures() {
            RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 2, depthTextureIndex);
            setUniformInts("depthTex", 3);
            RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE, digitalTextTexture);
            setUniformInts("asciiTex", 2);
            RenderSystem.activeTexture(GLStateProxy.DEFAULT_TEXTURE);
            setUniformInts("tex", 0);
        }

        @Override
        protected void uploadUniforms(int screenWidth, int screenHeight) {
            super.uploadUniforms(screenWidth, screenHeight);
            setUniformFloats("depthRange", zNear, zFar);
        }
    }
}

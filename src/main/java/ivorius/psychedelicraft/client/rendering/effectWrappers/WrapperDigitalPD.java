/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.effectWrappers;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import ivorius.psychedelicraft.client.rendering.shaders.ShaderDigitalDepth;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperDigitalPD extends ShaderWrapper<ShaderDigitalDepth> {
    public Identifier digitalTextTexture = Psychedelicraft.id(Psychedelicraft.filePathTextures + "digitalText.png");

    public WrapperDigitalPD(String utils) {
        super(new ShaderDigitalDepth(Psychedelicraft.logger), getRL("shaderBasic.vert"), getRL("shaderDigitalDepth.frag"), utils);
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer) {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(MinecraftClient.getInstance().cameraEntity);

        if (drugProperties != null && depthBuffer != null) {
            shaderInstance.digital = drugProperties.getDrugValue("Zero");
            shaderInstance.maxDownscale = drugProperties.getDigitalEffectPixelResize();
            shaderInstance.digitalTextTexture = PSRenderStates.getTextureIndex(digitalTextTexture);
            shaderInstance.depthTextureIndex = depthBuffer.getDepthAttachment();

            shaderInstance.zNear = 0.05f;
            shaderInstance.zFar = MinecraftClient.getInstance().options.getViewDistance().getValue() * 16;
        } else {
            shaderInstance.digital = 0.0f;
        }
    }

    @Override
    public boolean wantsDepthBuffer(float partialTicks) {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(MinecraftClient.getInstance().cameraEntity);

        return drugProperties != null && drugProperties.getDrugValue("Zero") > 0.0;
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.shader.program.ShaderDigitalDepth;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;
import ivorius.psychedelicraft.entity.drugs.DrugType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperDigitalPD extends ShaderWrapper<ShaderDigitalDepth> {
    public Identifier digitalTextTexture = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "digitalText.png");

    public WrapperDigitalPD(String utils) {
        super(new ShaderDigitalDepth(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderDigitalDepth.frag"), utils);
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer) {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(MinecraftClient.getInstance().cameraEntity);

        if (drugProperties != null && depthBuffer != null) {
            shaderInstance.digital = drugProperties.getDrugValue(DrugType.ZERO);
            shaderInstance.maxDownscale = drugProperties.getDigitalEffectPixelResize();
            shaderInstance.digitalTextTexture = GLStateProxy.getTextureId(digitalTextTexture);
            shaderInstance.depthTextureIndex = depthBuffer.getDepthAttachment();

            shaderInstance.zNear = 0.05f;
            shaderInstance.zFar = MinecraftClient.getInstance().options.getViewDistance().getValue() * 16;
        } else {
            shaderInstance.digital = 0;
        }
    }

    @Override
    public boolean wantsDepthBuffer(float partialTicks) {
        return DrugProperties.of(MinecraftClient.getInstance().cameraEntity).filter(properties -> properties.getDrugValue(DrugType.ZERO) > 0).isPresent();
    }
}

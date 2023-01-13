/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.shader.program.ShaderDigital;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;
import ivorius.psychedelicraft.entity.drugs.DrugType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperDigitalMD extends ShaderWrapper<ShaderDigital> {
    public Identifier digitalTextTexture;

    public WrapperDigitalMD(String utils) {
        super(new ShaderDigital(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderDigital.frag"), utils);

        digitalTextTexture = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "digitalText.png");
    }

    @Override
    public void setShaderValues(float tickDelta, int ticks, @Nullable Framebuffer buffer) {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(MinecraftClient.getInstance().cameraEntity);

        if (drugProperties != null) {
            shaderInstance.digital = drugProperties.getDrugValue(DrugType.ZERO);
            shaderInstance.maxDownscale = drugProperties.getDigitalEffectPixelResize();
            shaderInstance.digitalTextTexture = GLStateProxy.getTextureId(digitalTextTexture);
        } else {
            shaderInstance.digital = 0.0f;
        }
    }
}

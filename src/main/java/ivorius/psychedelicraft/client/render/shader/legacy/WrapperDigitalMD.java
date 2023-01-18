/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.shader.legacy.program.ShaderDigital;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperDigitalMD extends ShaderWrapper<ShaderDigital> {
    public static final Identifier DIGITAL_TEXTURE = Psychedelicraft.id("textures/drug/zero/programming.png");

    public WrapperDigitalMD(String utils) {
        super(new ShaderDigital(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderDigital.frag"), utils);
    }

    @Override
    public void setShaderValues(float tickDelta, int ticks, @Nullable Framebuffer buffer) {
        DrugProperties drugProperties = DrugProperties.of(MinecraftClient.getInstance().player);

        if (drugProperties != null) {
            shaderInstance.digital = drugProperties.getDrugValue(DrugType.ZERO);
            shaderInstance.maxDownscale = drugProperties.getDigitalEffectPixelResize();
            shaderInstance.digitalTextTexture = GLStateProxy.getTextureId(DIGITAL_TEXTURE);
        } else {
            shaderInstance.digital = 0.0f;
        }
    }
}

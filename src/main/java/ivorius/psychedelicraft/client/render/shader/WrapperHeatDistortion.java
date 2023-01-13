/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.shader.program.ShaderHeatDistortions;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperHeatDistortion extends ShaderWrapper<ShaderHeatDistortions> {
    public Identifier heatDistortionNoiseTexture = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "heatDistortionNoise.png");

    public WrapperHeatDistortion(String utils) {
        super(new ShaderHeatDistortions(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderHeatDistortion.frag"), utils);
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer) {

        if (PsychedelicraftClient.getConfig().visual.doHeatDistortion && depthBuffer != null) {
            float heatDistortion = DrugRenderer.INSTANCE.getCurrentHeatDistortion();

            shaderInstance.depthTextureIndex = depthBuffer.getDepthAttachment();
            shaderInstance.noiseTextureIndex = GLStateProxy.getTextureId(heatDistortionNoiseTexture);

            shaderInstance.strength = heatDistortion;
            shaderInstance.wobbleSpeed = 0.15f;
        } else {
            shaderInstance.strength = 0.0f;
        }
    }

    @Override
    public boolean wantsDepthBuffer(float partialTicks) {
        return PsychedelicraftClient.getConfig().visual.doHeatDistortion && DrugRenderer.INSTANCE.getCurrentHeatDistortion() > 0;
    }
}

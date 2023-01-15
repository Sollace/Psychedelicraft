/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.shader.legacy.program.ShaderHeatDistortions;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperUnderwaterDistortion extends ShaderWrapper<ShaderHeatDistortions> {
    public Identifier heatDistortionNoiseTexture = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "heatDistortionNoise.png");

    public WrapperUnderwaterDistortion(String utils) {
        super(new ShaderHeatDistortions(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderHeatDistortion.frag"), utils);
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks, @Nullable Framebuffer buffer) {
        float strength = getStrength();

        if (PsychedelicraftClient.getConfig().visual.doWaterDistortion && buffer != null && strength > 0) {
            shaderInstance.depthTextureIndex = buffer.getDepthAttachment();
            shaderInstance.noiseTextureIndex = GLStateProxy.getTextureId(heatDistortionNoiseTexture);
            shaderInstance.strength = strength;
            shaderInstance.wobbleSpeed = 0.03f;
        } else {
            shaderInstance.strength = 0;
        }
    }

    private float getStrength() {
        return DrugRenderer.INSTANCE.getEnvironmentalEffects().getWaterDistortion();
    }

    @Override
    public boolean wantsDepthBuffer(float partialTicks) {
        return PsychedelicraftClient.getConfig().visual.doWaterDistortion && getStrength() > 0;
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.program.ShaderBlurNoise;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

import java.util.Random;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperBlurNoise extends ShaderWrapper<ShaderBlurNoise> {
    public WrapperBlurNoise(String utils) {
        super(new ShaderBlurNoise(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderBlurNoise.frag"), utils);
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer) {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(MinecraftClient.getInstance().cameraEntity);

        if (drugProperties != null) {
            shaderInstance.strength = drugProperties.getDrugValue("Power") * 0.6f;
            shaderInstance.seed = new Random((long) ((ticks + partialTicks) * 1000.0)).nextFloat() * 9.0f + 1.0f;
        } else {
            shaderInstance.strength = 0.0f;
        }
    }
}

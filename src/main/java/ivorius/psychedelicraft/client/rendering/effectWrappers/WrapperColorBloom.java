/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.effectWrappers;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.shaders.ShaderColorBloom;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperColorBloom extends ShaderWrapper<ShaderColorBloom> {
    public WrapperColorBloom(String utils) {
        super(new ShaderColorBloom(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderColoredBloom.frag"), utils);
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer) {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(MinecraftClient.getInstance().cameraEntity);

        shaderInstance.coloredBloom = new float[]{1f, 1f, 1f, 0f};
        if (drugProperties != null) {
            drugProperties.hallucinationManager.applyColorBloom(drugProperties, shaderInstance.coloredBloom, partialTicks);
        }
    }
}

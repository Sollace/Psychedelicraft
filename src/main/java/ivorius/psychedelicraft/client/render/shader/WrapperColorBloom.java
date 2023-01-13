/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.program.ShaderColorBloom;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;
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
            drugProperties.getHallucinations().applyColorBloom(drugProperties, shaderInstance.coloredBloom, partialTicks);
        }
    }
}

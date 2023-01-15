/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.legacy.program.ShaderColorBloom;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
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
        shaderInstance.coloredBloom = DrugProperties.of(MinecraftClient.getInstance().player).getHallucinations().getColorBloom(partialTicks);
    }
}

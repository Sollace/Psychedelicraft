/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.legacy.program.ShaderRadialBlur;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperRadialBlur extends ShaderWrapper<ShaderRadialBlur> {
    public WrapperRadialBlur(String utils) {
        super(new ShaderRadialBlur(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderRadialBlur.frag"), utils);
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer) {
        DrugProperties drugProperties = DrugProperties.of(MinecraftClient.getInstance().player);

        // TODO: (Sollace) Radial blur was never implemented
        if (drugProperties != null) {
            shaderInstance.radialBlur = 0.0f;
        } else {
            shaderInstance.radialBlur = 0.0f;
        }
    }
}

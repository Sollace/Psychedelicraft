/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.effectWrappers;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.shaders.ShaderRadialBlur;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperRadialBlur extends ShaderWrapper<ShaderRadialBlur> {
    public WrapperRadialBlur(String utils) {
        super(new ShaderRadialBlur(Psychedelicraft.logger), getRL("shaderBasic.vert"), getRL("shaderRadialBlur.frag"), utils);
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer) {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(MinecraftClient.getInstance().cameraEntity);

        // TODO: (Sollace) Radial blur was never implemented
        if (drugProperties != null) {
            shaderInstance.radialBlur = 0.0f;
        } else {
            shaderInstance.radialBlur = 0.0f;
        }
    }
}

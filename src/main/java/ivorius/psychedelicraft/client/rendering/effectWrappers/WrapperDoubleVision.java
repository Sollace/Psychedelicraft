/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.effectWrappers;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.shaders.ShaderDoubleVision;
import ivorius.psychedelicraft.entities.drugs.Drug;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperDoubleVision extends ShaderWrapper<ShaderDoubleVision> {
    public WrapperDoubleVision(String utils) {
        super(new ShaderDoubleVision(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderDoubleVision.frag"), utils);
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer) {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(MinecraftClient.getInstance().cameraEntity);

        shaderInstance.doubleVision = 0.0f;

        if (drugProperties != null) {
            for (Drug drug : drugProperties.getAllDrugs())
                shaderInstance.doubleVision += (1.0f - shaderInstance.doubleVision) * drug.doubleVision();

            shaderInstance.doubleVisionDistance = MathHelper.sin((ticks + partialTicks) / 20.0f) * 0.05f * shaderInstance.doubleVision;
        }
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.effectWrappers;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.shaders.ShaderSimpleEffects;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperSimpleEffects extends ShaderWrapper<ShaderSimpleEffects> {
    public WrapperSimpleEffects(String utils) {
        super(new ShaderSimpleEffects(Psychedelicraft.logger), getRL("shaderBasic.vert"), getRL("shaderSimpleEffects.frag"), utils);
    }

    @Override
    public void setShaderValues(float tickDelta, int ticks, @Nullable Framebuffer buffer) {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(MinecraftClient.getInstance().cameraEntity);

        if (drugProperties != null) {
            shaderInstance.quickColorRotation = drugProperties.hallucinationManager.getQuickColorRotation(drugProperties, tickDelta);
            shaderInstance.slowColorRotation = drugProperties.hallucinationManager.getSlowColorRotation(drugProperties, tickDelta);
            shaderInstance.desaturation = drugProperties.hallucinationManager.getDesaturation(drugProperties, tickDelta);
            shaderInstance.colorIntensification = drugProperties.hallucinationManager.getColorIntensification(drugProperties, tickDelta);
        } else {
            shaderInstance.slowColorRotation = 0;
            shaderInstance.quickColorRotation = 0;
            shaderInstance.desaturation = 0;
            shaderInstance.colorIntensification = 0;
        }
    }
}

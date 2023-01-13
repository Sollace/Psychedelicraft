/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.program.ShaderSimpleEffects;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperSimpleEffects extends ShaderWrapper<ShaderSimpleEffects> {
    public WrapperSimpleEffects(String utils) {
        super(new ShaderSimpleEffects(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderSimpleEffects.frag"), utils);
    }

    @Override
    public void setShaderValues(float tickDelta, int ticks, @Nullable Framebuffer buffer) {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(MinecraftClient.getInstance().cameraEntity);

        if (drugProperties != null) {
            shaderInstance.quickColorRotation = drugProperties.getHallucinations().getQuickColorRotation(drugProperties, tickDelta);
            shaderInstance.slowColorRotation = drugProperties.getHallucinations().getSlowColorRotation(drugProperties, tickDelta);
            shaderInstance.desaturation = drugProperties.getHallucinations().getDesaturation(drugProperties, tickDelta);
            shaderInstance.colorIntensification = drugProperties.getHallucinations().getColorIntensification(drugProperties, tickDelta);
        } else {
            shaderInstance.slowColorRotation = 0;
            shaderInstance.quickColorRotation = 0;
            shaderInstance.desaturation = 0;
            shaderInstance.colorIntensification = 0;
        }
    }
}

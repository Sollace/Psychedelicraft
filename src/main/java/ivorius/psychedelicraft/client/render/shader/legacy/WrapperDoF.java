/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.shader.legacy.program.ShaderDoF;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperDoF extends ShaderWrapper<ShaderDoF> {
    public WrapperDoF(String utils) {
        super(new ShaderDoF(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("depthOfField.frag"), utils);
    }

    public boolean isActive() {
        return (PsychedelicraftClient.getConfig().visual.dofFocalBlurFar > 0 || PsychedelicraftClient.getConfig().visual.dofFocalBlurNear > 0)
            && (PsychedelicraftClient.getConfig().visual.dofFocalPointNear > 0 || PsychedelicraftClient.getConfig().visual.dofFocalPointFar < getCurrentZFar());
    }

    protected float getCurrentZFar() {
        return MinecraftClient.getInstance().options.getViewDistance().getValue() * 16;
    }

    @Override
    public void setShaderValues(float tickDelta, int ticks, @Nullable Framebuffer buffer) {
        if (buffer != null && isActive()) {
            shaderInstance.depthTextureIndex = buffer.getDepthAttachment();

            shaderInstance.zNear = 0.05f;
            shaderInstance.zFar = getCurrentZFar();

            shaderInstance.focalPointNear = PsychedelicraftClient.getConfig().visual.dofFocalPointNear / shaderInstance.zFar;
            shaderInstance.focalPointFar = PsychedelicraftClient.getConfig().visual.dofFocalPointFar / shaderInstance.zFar;
            shaderInstance.focalBlurFar = PsychedelicraftClient.getConfig().visual.dofFocalBlurFar;
            shaderInstance.focalBlurNear = PsychedelicraftClient.getConfig().visual.dofFocalBlurNear;
        } else {
            shaderInstance.focalBlurFar = 0.0f;
            shaderInstance.focalBlurNear = 0.0f;
        }
    }

    @Override
    public boolean wantsDepthBuffer(float partialTicks) {
        return isActive();
    }
}

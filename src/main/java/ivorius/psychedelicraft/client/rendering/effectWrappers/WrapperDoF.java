/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.effectWrappers;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.ClientProxy;
import ivorius.psychedelicraft.client.rendering.shaders.ShaderDoF;
import ivorius.psychedelicraft.config.PSConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperDoF extends ShaderWrapper<ShaderDoF> {
    public WrapperDoF(String utils) {
        super(new ShaderDoF(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderDof.frag"), utils);
    }

    public boolean isActive() {
        return (PSConfig.<ClientProxy.Config>getInstance().visual.dofFocalBlurFar > 0 || PSConfig.<ClientProxy.Config>getInstance().visual.dofFocalBlurNear > 0)
            && (PSConfig.<ClientProxy.Config>getInstance().visual.dofFocalPointNear > 0 || PSConfig.<ClientProxy.Config>getInstance().visual.dofFocalPointFar < getCurrentZFar());
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

            shaderInstance.focalPointNear = PSConfig.<ClientProxy.Config>getInstance().visual.dofFocalPointNear / shaderInstance.zFar;
            shaderInstance.focalPointFar = PSConfig.<ClientProxy.Config>getInstance().visual.dofFocalPointFar / shaderInstance.zFar;
            shaderInstance.focalBlurFar = PSConfig.<ClientProxy.Config>getInstance().visual.dofFocalBlurFar;
            shaderInstance.focalBlurNear = PSConfig.<ClientProxy.Config>getInstance().visual.dofFocalBlurNear;
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

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.effectWrappers;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.ScreenEffect;
import ivorius.psychedelicraft.client.rendering.shaders.IvShaderInstance2D;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 26.04.14.
 */
@Deprecated(since = "Needs replacement")
public abstract class ShaderWrapper<ShaderInstance extends IvShaderInstance2D> implements EffectWrapper {
    public final ShaderInstance shaderInstance;

    public final Identifier vertexShaderFile;
    public final Identifier fragmentShaderFile;

    public final String utils;

    public ShaderWrapper(ShaderInstance shaderInstance, Identifier vertexShaderFile, Identifier fragmentShaderFile, String utils) {
        this.shaderInstance = shaderInstance;
        this.vertexShaderFile = vertexShaderFile;
        this.fragmentShaderFile = fragmentShaderFile;
        this.utils = utils;
    }

    public static Identifier getRL(String shaderFile) {
        return Psychedelicraft.id(Psychedelicraft.filePathShaders + shaderFile);
    }

    @Override
    public void apply(float partialTicks, ScreenEffect.PingPong pingPong, Framebuffer depthBuffer) {
        if (PSRenderStates.shader2DEnabled) {
            MinecraftClient mc = MinecraftClient.getInstance();
            int ticks = mc.cameraEntity.age;
            setShaderValues(partialTicks, ticks, depthBuffer);

            if (shaderInstance.shouldApply(ticks + partialTicks)) {
                shaderInstance.apply(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), ticks + partialTicks, pingPong);
            }
        }
    }

    public abstract void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer);
}

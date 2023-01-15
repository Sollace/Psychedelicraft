/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.shader.legacy.program.IvShaderInstance2D;
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
        return Psychedelicraft.id(Psychedelicraft.SHADERS_PATH + shaderFile);
    }

    @Override
    public void apply(float partialTicks, IvShaderInstance2D.PingPong pingPong, Framebuffer depthBuffer) {
        if (PsychedelicraftClient.getConfig().visual.shader2DEnabled) {
            MinecraftClient mc = MinecraftClient.getInstance();
            int ticks = mc.player.age;
            setShaderValues(partialTicks, ticks, depthBuffer);

            if (shaderInstance.shouldApply(ticks + partialTicks)) {
                shaderInstance.render(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), ticks + partialTicks, pingPong);
            }
        }
    }

    public abstract void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer);
}

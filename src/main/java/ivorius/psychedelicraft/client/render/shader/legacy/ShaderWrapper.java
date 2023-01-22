/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 26.04.14.
 */
@Deprecated(since = "Needs replacement")
public abstract class ShaderWrapper<ShaderInstance extends IvShaderInstance2D> implements EffectWrapper {
    public final ShaderInstance shaderInstance;

    public ShaderWrapper(ShaderInstance shaderInstance, Identifier vertexShaderFile, Identifier fragmentShaderFile, String utils) {
        this.shaderInstance = shaderInstance;
    }

    public static Identifier getRL(String shaderFile) {
        return Psychedelicraft.id(Psychedelicraft.SHADERS_PATH + shaderFile);
    }

    @Override
    public void apply(float partialTicks, IvShaderInstance2D.PingPong pingPong, Framebuffer depthBuffer) {

    }

    public abstract void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer);
}

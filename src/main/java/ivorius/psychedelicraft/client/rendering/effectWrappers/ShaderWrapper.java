/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.effectWrappers;

import ivorius.ivtoolkit.rendering.IvDepthBuffer;
import ivorius.ivtoolkit.rendering.IvOpenGLTexturePingPong;
import ivorius.ivtoolkit.rendering.IvShaderInstance2D;
import ivorius.ivtoolkit.rendering.IvShaderInstanceMC;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/**
 * Created by lukas on 26.04.14.
 */
@Deprecated(since = "Needs replacement")
public abstract class ShaderWrapper<ShaderInstance extends IvShaderInstance2D> implements EffectWrapper
{
    public ShaderInstance shaderInstance;

    public ResourceLocation vertexShaderFile;
    public ResourceLocation fragmentShaderFile;

    public String utils;

    public ShaderWrapper(ShaderInstance shaderInstance, ResourceLocation vertexShaderFile, ResourceLocation fragmentShaderFile, String utils)
    {
        this.shaderInstance = shaderInstance;
        this.vertexShaderFile = vertexShaderFile;
        this.fragmentShaderFile = fragmentShaderFile;
        this.utils = utils;
    }

    public static ResourceLocation getRL(String shaderFile)
    {
        return new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathShaders + shaderFile);
    }

    @Override
    public void alloc()
    {
        if (PSRenderStates.shader2DEnabled)
        {
            IvShaderInstanceMC.trySettingUpShader(shaderInstance, vertexShaderFile, fragmentShaderFile, utils);
        }
    }

    @Override
    public void dealloc()
    {
        shaderInstance.deleteShader();
    }

    @Override
    public void apply(float partialTicks, IvOpenGLTexturePingPong pingPong, IvDepthBuffer depthBuffer)
    {
        if (PSRenderStates.shader2DEnabled)
        {
            Minecraft mc = Minecraft.getMinecraft();
            int ticks = mc.renderViewEntity.ticksExisted;
            setShaderValues(partialTicks, ticks, depthBuffer);

            if (shaderInstance.shouldApply(ticks + partialTicks))
            {
                shaderInstance.apply(mc.displayWidth, mc.displayHeight, ticks + partialTicks, pingPong);
            }
        }
    }

    public abstract void setShaderValues(float partialTicks, int ticks, IvDepthBuffer depthBuffer);
}

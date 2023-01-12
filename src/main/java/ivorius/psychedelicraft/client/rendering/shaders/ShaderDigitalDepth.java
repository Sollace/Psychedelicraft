/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.shaders;

import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.rendering.GLStateProxy;

/**
 * Created by lukas on 18.02.14.
 */
@Deprecated
public class ShaderDigitalDepth extends ShaderDigital {
    public int depthTextureIndex;

    public float zNear;
    public float zFar;

    public ShaderDigitalDepth(Logger logger) {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks) {
        return depthTextureIndex > 0 && super.shouldApply(ticks);
    }

    @Override
    protected void uploadTextures() {
        RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 2, depthTextureIndex);
        setUniformInts("depthTex", 3);
        RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE, digitalTextTexture);
        setUniformInts("asciiTex", 2);
        RenderSystem.activeTexture(GLStateProxy.DEFAULT_TEXTURE);
        setUniformInts("tex", 0);
    }


    @Override
    protected void uploadUniforms(int screenWidth, int screenHeight) {
        super.uploadUniforms(screenWidth, screenHeight);
        setUniformFloats("depthRange", zNear, zFar);
    }
}

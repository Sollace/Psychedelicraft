/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.program;

import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.render.GLStateProxy;

/**
 * Created by lukas on 18.02.14.
 */
@Deprecated
public class ShaderDistortionMap extends IvShaderInstance2D {
    public float strength;
    public float alpha = 1;

    public int noiseTextureIndex0;
    public int noiseTextureIndex1;

    public float[] texTranslation0;
    public float[] texTranslation1;

    public ShaderDistortionMap(Logger logger) {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks) {
        return strength > 0
                && alpha > 0
                && noiseTextureIndex0 > 0
                && noiseTextureIndex1 > 0;
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        useShader();
        RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 1, noiseTextureIndex0);
        RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 2, noiseTextureIndex1);
        RenderSystem.activeTexture(GLStateProxy.DEFAULT_TEXTURE);
        setUniformInts("tex0", 0);
        setUniformInts("tex1", 1);
        setUniformInts("noiseTex0", 2);
        setUniformInts("noiseTex1", 3);
        setUniformFloats("totalAlpha", alpha);
        setUniformFloats("strength", strength);
        setUniformFloats("texTranslation0", texTranslation0);
        setUniformFloats("texTranslation1", texTranslation1);
        drawFullScreen(screenWidth, screenHeight, pingPong);

        stopUsingShader();
    }
}

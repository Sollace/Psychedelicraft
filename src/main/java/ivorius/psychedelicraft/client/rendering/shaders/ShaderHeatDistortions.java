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
public class ShaderHeatDistortions extends IvShaderInstance2D {
    public float strength;
    public float wobbleSpeed;
    public int depthTextureIndex;
    public int noiseTextureIndex;

    public ShaderHeatDistortions(Logger logger) {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks) {
        return strength > 0 && depthTextureIndex > 0 && noiseTextureIndex > 0;
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        useShader();

        RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 2, noiseTextureIndex);
        RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 1, depthTextureIndex);
        RenderSystem.activeTexture(GLStateProxy.DEFAULT_TEXTURE);

        for (int i = 0; i < 3; i++) {
            setUniformInts("tex" + i, i);
        }
        setUniformInts("noiseTex", 3);

        setUniformFloats("totalAlpha", 1.0f);
        setUniformFloats("ticks", ticks * wobbleSpeed);
        setUniformFloats("strength", strength);

        drawFullScreen(screenWidth, screenHeight, pingPong);

        stopUsingShader();
    }
}

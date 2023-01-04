/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.shaders;

import org.apache.logging.log4j.Logger;

/**
 * Created by lukas on 18.02.14.
 */
public class ShaderBlurNoise extends IvShaderInstance2D
{
    public float strength;
    public float seed;

    public ShaderBlurNoise(Logger logger)
    {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks)
    {
        return (strength > 0.0f) && super.shouldApply(ticks);
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong)
    {
        useShader();

        for (int i = 0; i < 1; i++)
        {
            setUniformInts("tex" + i, i);
        }

        setUniformFloats("pixelSize", 1.0f / screenWidth, 1.0f / screenHeight);
        setUniformFloats("strength", strength);
        setUniformFloats("seed", seed);
        setUniformFloats("totalAlpha", 1.0f);

        drawFullScreen(screenWidth, screenHeight, pingPong);

        stopUsingShader();
    }
}

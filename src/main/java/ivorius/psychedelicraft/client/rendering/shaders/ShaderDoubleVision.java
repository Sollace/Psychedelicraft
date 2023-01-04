/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.shaders;

import org.apache.logging.log4j.Logger;

/**
 * Created by lukas on 18.02.14.
 */
public class ShaderDoubleVision extends IvShaderInstance2D
{
    public float doubleVision;
    public float doubleVisionDistance;

    public ShaderDoubleVision(Logger logger)
    {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks)
    {
        return doubleVision > 0.0f && super.shouldApply(ticks);
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong)
    {
        useShader();

        for (int i = 0; i < 1; i++)
        {
            setUniformInts("tex" + i, i);
        }

        setUniformFloats("totalAlpha", doubleVision);
        setUniformFloats("distance", doubleVisionDistance);
        setUniformFloats("stretch", 1.0f + doubleVision);

        drawFullScreen(screenWidth, screenHeight, pingPong);

        stopUsingShader();
    }
}

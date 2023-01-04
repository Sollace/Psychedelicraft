/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.shaders;

import org.apache.logging.log4j.Logger;

import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 18.02.14.
 */
public class ShaderRadialBlur extends IvShaderInstance2D
{
    public float radialBlur;

    public ShaderRadialBlur(Logger logger)
    {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks)
    {
        return radialBlur > 0.0f && super.shouldApply(ticks);
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong)
    {
        useShader();

        for (int i = 0; i < 1; i++)
        {
            setUniformInts("tex" + i, i);
        }

        setUniformFloats("pixelSize", 1.0f / screenWidth * (radialBlur * 1.5f + 1.0f), 1.0f / screenHeight * (radialBlur * 1.5f + 1.0f));

        for (int n = 0; n < MathHelper.floor(radialBlur) + 1; n++)
        {
            float activeBlur = radialBlur - n;
            if (activeBlur > 1.0f)
            {
                activeBlur = 1.0f;
            }

            if (activeBlur > 0.0f)
            {
                setUniformFloats("totalAlpha", activeBlur);

                drawFullScreen(screenWidth, screenHeight, pingPong);
            }
        }

        stopUsingShader();
    }
}

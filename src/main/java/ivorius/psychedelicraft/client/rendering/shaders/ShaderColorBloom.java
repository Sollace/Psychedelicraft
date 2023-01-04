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
public class ShaderColorBloom extends IvShaderInstance2D
{
    public float[] coloredBloom;

    public ShaderColorBloom(Logger logger)
    {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks)
    {
        return coloredBloom[3] > 0.0f && super.shouldApply(ticks);
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong)
    {
        useShader();

        for (int i = 0; i < 1; i++)
            setUniformInts("tex" + i, i);

        setUniformFloats("pixelSize", 1.0f / screenWidth, 1.0f / screenHeight);
        setUniformFloats("bloomColor", coloredBloom[0], coloredBloom[1], coloredBloom[2]);

        for (int n = 0; n < MathHelper.ceil(coloredBloom[3]); n++)
        {
            float activeBloom = coloredBloom[3] - n;
            if (activeBloom > 1.0f)
                activeBloom = 1.0f;
            setUniformFloats("totalAlpha", activeBloom);

            for (int i = 0; i < 2; i++)
            {
                setUniformInts("vertical", i);
                drawFullScreen(screenWidth, screenHeight, pingPong);
            }
        }

        stopUsingShader();
    }
}

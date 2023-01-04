/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.shaders;

import org.apache.logging.log4j.Logger;

import ivorius.psychedelicraft.client.rendering.ScreenEffect;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 18.02.14.
 */
public class ShaderBloom extends IvShaderInstance2D
{
    public float bloom;

    public ShaderBloom(Logger logger)
    {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks)
    {
        return bloom > 0.0f && super.shouldApply(ticks);
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, ScreenEffect.PingPong pingPong)
    {
        useShader();

        for (int i = 0; i < 1; i++)
        {
            setUniformInts("tex" + i, i);
        }

        setUniformFloats("pixelSize", 1.0f / screenWidth * 2.0f, 1.0f / screenHeight * 2.0f);

        for (int n = 0; n < MathHelper.ceil(bloom); n++)
        {
            float activeBloom = bloom - n;
            if (activeBloom > 1.0f)
            {
                activeBloom = 1.0f;
            }
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

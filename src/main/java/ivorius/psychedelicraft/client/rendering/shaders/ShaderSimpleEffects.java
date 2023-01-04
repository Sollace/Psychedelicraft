/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.shaders;

import org.apache.logging.log4j.Logger;

import static org.lwjgl.opengl.GL11.glColor3f;

/**
 * Created by lukas on 18.02.14.
 */
public class ShaderSimpleEffects extends IvShaderInstance2D
{
    public float slowColorRotation;
    public float quickColorRotation;
    public float colorIntensification;
    public float desaturation;

    public ShaderSimpleEffects(Logger logger)
    {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks)
    {
        return (slowColorRotation > 0.0f || quickColorRotation > 0.0f
                || colorIntensification > 0.0f || desaturation > 0.0f) && super.shouldApply(ticks);
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong)
    {
        useShader();

        setUniformInts("tex0", 0);

        setUniformFloats("totalAlpha", 1.0f);

        setUniformFloats("slowColorRotation", slowColorRotation);
        setUniformFloats("quickColorRotation", quickColorRotation);
        setUniformFloats("colorIntensification", colorIntensification);
        setUniformFloats("desaturation", desaturation);

        glColor3f(1.0f, 1.0f, 1.0f);
        drawFullScreen(screenWidth, screenHeight, pingPong);

        stopUsingShader();
    }
}

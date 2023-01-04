/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;

import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by lukas on 21.02.14.
 */
public class EffectMotionBlur implements ScreenEffect
{
    public int[] motionBlurCacheTextures;
    public boolean[] motionBlurCacheTexturesInitialized;
    public float sampleFrequency = 0.5f;

    public int motionBlurCacheTextureIndex;
    private float previousTicks;

    public float motionBlur;

    private int currentTexturesWidth = -1;
    private int currentTexturesHeight = -1;

    @Override
    public boolean shouldApply(float ticks)
    {
        return true;
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong)
    {
        if (motionBlur > 0.0f)
        {
            if (screenWidth != currentTexturesWidth || screenHeight != currentTexturesHeight)
            {
                setUp(screenWidth, screenHeight, 30);
            }

            if (previousTicks > ticks)
            {
                previousTicks = ticks;
            }
            else if (previousTicks + sampleFrequency * motionBlurCacheTextures.length < ticks)
            {
                previousTicks = ticks - sampleFrequency * motionBlurCacheTextures.length;
            }

            while (previousTicks + sampleFrequency <= ticks)
            {
                motionBlurCacheTextureIndex++;
                motionBlurCacheTextureIndex %= motionBlurCacheTextures.length;

                glBindTexture(GL_TEXTURE_2D, motionBlurCacheTextures[motionBlurCacheTextureIndex]);
                glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 0, 0, screenWidth, screenHeight, 0);
                motionBlurCacheTexturesInitialized[motionBlurCacheTextureIndex] = true;

                previousTicks += sampleFrequency;
            }

            pingPong.pingPong();
            IvRenderHelper.drawRectFullScreen(screenWidth, screenHeight);

            glDisable(GL_ALPHA_TEST);
            glEnable(GL_BLEND);
            GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            for (int i = 0; i < motionBlurCacheTextures.length; i++)
            {
                int index = (i + motionBlurCacheTextureIndex) % motionBlurCacheTextures.length;

                if (motionBlurCacheTexturesInitialized[index])
                {
                    float alpha = i * 0.02f * motionBlur;
                    if (alpha > 0.1f)
                    {
                        alpha = 0.1f;
                    }

                    if (alpha > 0.0f)
                    {
                        glColor4f(1.0f, 1.0f, 1.0f, alpha);
                        glBindTexture(GL_TEXTURE_2D, motionBlurCacheTextures[index]);
                        IvRenderHelper.drawRectFullScreen(screenWidth, screenHeight);
                    }
                }
            }
            glDisable(GL_BLEND);
            glEnable(GL_ALPHA_TEST);
        }
        else
        {
            if (motionBlurCacheTexturesInitialized != null)
            {
                motionBlurCacheTextureIndex++;
                motionBlurCacheTextureIndex %= motionBlurCacheTextures.length;
                motionBlurCacheTexturesInitialized[motionBlurCacheTextureIndex] = false;
            }
        }
    }

    @Override
    public void destruct()
    {
        destructMotionBlur();
    }

    public void setUp(int width, int height, int samples)
    {
        destructMotionBlur();

        motionBlurCacheTextures = new int[samples];
        motionBlurCacheTexturesInitialized = new boolean[motionBlurCacheTextures.length];

        for (int i = 0; i < motionBlurCacheTextures.length; i++)
        {
            motionBlurCacheTextures[i] = IvOpenGLHelper.genStandardTexture();
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        }

        currentTexturesWidth = width;
        currentTexturesHeight = height;
    }

    public void destructMotionBlur()
    {
        if (motionBlurCacheTextures != null)
        {
            for (int i = 0; i < motionBlurCacheTextures.length; i++)
            {
                if (motionBlurCacheTextures[i] > 0)
                {
                    glDeleteTextures(motionBlurCacheTextures[i]);
                    motionBlurCacheTextures[i] = 0;
                }
            }
        }

        motionBlurCacheTextureIndex = 0;
        currentTexturesWidth = -1;
        currentTexturesHeight = -1;
    }
}

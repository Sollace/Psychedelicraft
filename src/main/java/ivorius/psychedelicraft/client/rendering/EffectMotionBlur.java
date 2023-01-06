/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Created by lukas on 21.02.14.
 */
public class EffectMotionBlur implements ScreenEffect {
    public int[] motionBlurCacheTextures;
    public boolean[] motionBlurCacheTexturesInitialized;
    public float sampleFrequency = 0.5f;

    public int motionBlurCacheTextureIndex;
    private float previousTicks;

    public float motionBlur;

    private int currentTexturesWidth = -1;
    private int currentTexturesHeight = -1;

    @Override
    public boolean shouldApply(float ticks) {
        return true;
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        if (motionBlur > 0) {
            if (screenWidth != currentTexturesWidth || screenHeight != currentTexturesHeight) {
                setUp(screenWidth, screenHeight, 30);
            }

            if (previousTicks > ticks) {
                previousTicks = ticks;
            } else if (previousTicks + sampleFrequency * motionBlurCacheTextures.length < ticks) {
                previousTicks = ticks - sampleFrequency * motionBlurCacheTextures.length;
            }

            while (previousTicks + sampleFrequency <= ticks) {
                motionBlurCacheTextureIndex++;
                motionBlurCacheTextureIndex %= motionBlurCacheTextures.length;

                RenderSystem.bindTexture(motionBlurCacheTextures[motionBlurCacheTextureIndex]);
                GLStateProxy.copyTexture(0, 0, 0, 0, screenWidth, screenHeight);
                motionBlurCacheTexturesInitialized[motionBlurCacheTextureIndex] = true;

                previousTicks += sampleFrequency;
            }

            if (pingPong != null) {
                pingPong.pingPong();
                MCColorHelper.drawScreen(screenWidth, screenHeight);
            }

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
            for (int i = 0; i < motionBlurCacheTextures.length; i++)
            {
                int index = (i + motionBlurCacheTextureIndex) % motionBlurCacheTextures.length;

                if (motionBlurCacheTexturesInitialized[index])
                {
                    float alpha = Math.min(1, i * 0.02f * motionBlur);

                    if (alpha > 0.0f) {
                        GL11.glColor4f(1, 1, 1, alpha);
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, motionBlurCacheTextures[index]);
                        MCColorHelper.drawScreen(screenWidth, screenHeight);
                    }
                }
            }
            RenderSystem.disableBlend();
        } else {
            if (motionBlurCacheTexturesInitialized != null) {
                motionBlurCacheTextureIndex++;
                motionBlurCacheTextureIndex %= motionBlurCacheTextures.length;
                motionBlurCacheTexturesInitialized[motionBlurCacheTextureIndex] = false;
            }
        }
    }

    @Override
    public void destruct() {
        if (motionBlurCacheTextures != null) {
            for (int i = 0; i < motionBlurCacheTextures.length; i++) {
                if (motionBlurCacheTextures[i] > 0) {
                    GL11.glDeleteTextures(motionBlurCacheTextures[i]);
                    motionBlurCacheTextures[i] = 0;
                }
            }
        }

        motionBlurCacheTextureIndex = 0;
        currentTexturesWidth = -1;
        currentTexturesHeight = -1;
    }

    public void setUp(int width, int height, int samples) {
        destruct();

        motionBlurCacheTextures = new int[samples];
        motionBlurCacheTexturesInitialized = new boolean[motionBlurCacheTextures.length];

        for (int i = 0; i < motionBlurCacheTextures.length; i++) {
            motionBlurCacheTextures[i] = TextureUtil.generateTextureId();
            GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);
        }

        currentTexturesWidth = width;
        currentTexturesHeight = height;
    }
}

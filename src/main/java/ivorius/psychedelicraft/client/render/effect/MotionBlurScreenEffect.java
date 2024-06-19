/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import java.util.*;
import java.util.stream.IntStream;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;

/**
 * Created by lukas on 21.02.14.
 * Updated by Sollace on 15 Jan 2023
 */
public class MotionBlurScreenEffect implements ScreenEffect {
    private static final int MAX_SAMPLES = 30;
    private static final float SAMPLE_FREQUENCY = 0.5f;

    private float previousTicks;
    private int currentSample;
    private GlTextureSet textures;

    public float motionBlur;

    @Override
    public void update(float tickDelta) {
        motionBlur = PsychedelicraftClient.getConfig().visual.doMotionBlur && MinecraftClient.getInstance().player != null
                ? DrugProperties.of(MinecraftClient.getInstance().player).getModifier(Drug.MOTION_BLUR)
                : 0;
    }

    @Override
    public void render(DrawContext context, Window window, float tickDelta) {

        if (MinecraftClient.getInstance().player == null) {
            return;
        }

        int screenWidth = window.getScaledWidth();
        int screenHeight = window.getScaledHeight();

        if (motionBlur > 0) {
            if (textures != null && textures.sizeChanged(screenWidth, screenHeight)) {
                close();
            }

            if (textures == null) {
                textures = new GlTextureSet(MAX_SAMPLES, screenWidth, screenHeight);
            }

            tickDelta += MinecraftClient.getInstance().player.age;

            if (previousTicks > tickDelta) {
                previousTicks = tickDelta;
            } else if (previousTicks + SAMPLE_FREQUENCY * MAX_SAMPLES < tickDelta) {
                previousTicks = tickDelta - SAMPLE_FREQUENCY * MAX_SAMPLES;
            }

            while (previousTicks + SAMPLE_FREQUENCY <= tickDelta) {
                currentSample++;
                currentSample %= MAX_SAMPLES;
                textures.getTexture(currentSample).sample();
                previousTicks += SAMPLE_FREQUENCY;
            }

            textures.drawToScreen(context, currentSample);
        } else if (textures != null) {
            currentSample++;
            currentSample %= MAX_SAMPLES;
            textures.getTexture(currentSample).reset();
        }
    }

    @Override
    public void close() {
        if (textures != null) {
            textures.close();
            textures = null;
        }
    }

    private class GlTextureSet implements AutoCloseable {
        private final int width;
        private final int height;
        private final List<GlTexture> textures;

        public GlTextureSet(int samples, int width, int height) {
            this.width = width;
            this.height = height;
            textures = IntStream.range(1, samples + 1)
                    .mapToObj(i -> new GlTexture(i, width, height))
                    .toList();
        }

        public GlTexture getTexture(int sample) {
            return textures.get(sample);
        }

        public boolean sizeChanged(int width, int height) {
            return this.width != width || this.height != height;
        }

        public void drawToScreen(DrawContext context, int currentSample) {
            for (int i = 0; i < textures.size(); i++) {
                textures.get((i + currentSample) % textures.size()).drawToScreen(context);
            }
        }

        @Override
        public void close() {
            textures.forEach(GlTexture::close);
        }
    }

    private class GlTexture implements AutoCloseable {

        private final SimpleFramebuffer output;

        private final int sample;

        private boolean prepared;

        public GlTexture(int sample, int width, int height) {
            this.sample = sample;
            output = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
            output.viewportWidth = width;
            output.viewportHeight = height;
        }

        public void sample() {
            Framebuffer input = MinecraftClient.getInstance().getFramebuffer();
            // TODO: https://en.wikibooks.org/wiki/OpenGL_Programming/Motion_Blur

            GlStateManager._glBindFramebuffer(GlConst.GL_READ_FRAMEBUFFER, input.fbo);
            GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, output.fbo);
            GlStateManager._glBlitFrameBuffer(0, 0, input.textureWidth, input.textureHeight, 0, 0, output.textureWidth, output.textureHeight, GL11.GL_COLOR_BUFFER_BIT, GlConst.GL_NEAREST);
            GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);

            input.beginWrite(true);

            prepared = true;
        }

        public void reset() {
            prepared = false;
        }

        public void drawToScreen(DrawContext context) {
            float alpha = Math.min(1, sample * 0.002f * motionBlur);

            if (prepared && alpha > 0) {
                RenderUtil.drawBuffer(output, 1, 1, 1, alpha);
            }
        }

        @Override
        public void close() {
            output.delete();
        }
    }
}

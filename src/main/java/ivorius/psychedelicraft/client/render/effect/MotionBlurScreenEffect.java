/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import java.util.*;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.GuiQuads;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.ColorHelper;

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
        motionBlur = PsychedelicraftClient.getConfig().doMotionBlur.get() && MinecraftClient.getInstance().player != null
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
        private final List<SampleTexture> textures;

        public GlTextureSet(int samples, int width, int height) {
            this.width = width;
            this.height = height;
            textures = IntStream.range(1, samples + 1).mapToObj(SampleTexture::new).toList();
        }

        public SampleTexture getTexture(int sample) {
            return textures.get(sample);
        }

        public boolean sizeChanged(int width, int height) {
            return this.width != width || this.height != height;
        }

        public void drawToScreen(DrawContext context, int currentSample) {
            for (int i = 0; i < textures.size(); i++) {
                textures.get((i + currentSample) % textures.size()).drawToScreen(context, width, height);
            }
        }

        @Override
        public void close() {
            textures.forEach(SampleTexture::close);
        }
    }

    private class SampleTexture implements AutoCloseable {
        private final int sample;

        @Nullable
        private GpuTexture texture;
        @Nullable
        private GpuTextureView view;

        private boolean prepared;

        public SampleTexture(int sample) {
            this.sample = sample;
        }

        public void sample() {
            Framebuffer input = MinecraftClient.getInstance().getFramebuffer();
            if (texture != null && (texture.getWidth(0) != input.textureWidth || texture.getHeight(0) != input.textureHeight)) {
                close();
            }
            if (texture == null) {
                texture = RenderSystem.getDevice().createTexture(() -> "PS_MotionBlurFrame" + sample, GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, TextureFormat.RGBA8, input.textureWidth, input.textureHeight, 1, 1);
                view = RenderSystem.getDevice().createTextureView(texture);
            }
            RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(input.getColorAttachment(), texture, 0, 0, 0, 0, 0, input.textureWidth, input.textureHeight);
            prepared = true;
        }

        public void reset() {
            close();
        }

        public void drawToScreen(DrawContext context, int width, int height) {
            float alpha = Math.min(1, sample * 0.008F * motionBlur);

            if (prepared && alpha > 0) {
                GuiQuads.builder()
                    .quad(0, 0, width, height, 0, 1, 1, 0, ColorHelper.fromFloats(alpha, 1, 1, 1))
                    .draw(context, RenderPipelines.GUI_TEXTURED, TextureSetup.of(view, RenderSystem.getSamplerCache().get(FilterMode.LINEAR)));
            }
        }

        @Override
        public void close() {
            prepared = false;
            if (view != null) {
                view.close();
                view = null;
            }
            if (texture != null) {
                texture.close();
                texture = null;
            }
        }
    }
}

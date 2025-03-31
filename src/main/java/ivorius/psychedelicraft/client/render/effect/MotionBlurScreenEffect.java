/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import java.util.*;
import java.util.stream.IntStream;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;

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
        private final List<GlTexture> textures;

        public GlTextureSet(int samples, int width, int height) {
            this.width = width;
            this.height = height;
            textures = IntStream.range(1, samples + 1)
                    .mapToObj(i -> new GlTexture(Psychedelicraft.id("motion_blur_" + i), i))
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
            textures.forEach(texture -> {
                MinecraftClient.getInstance().getTextureManager().destroyTexture(texture.id);
            });
        }
    }

    private class GlTexture extends AbstractTexture implements AutoCloseable {

        private final Identifier id;
        private final int sample;

        private int width;
        private int height;

        private boolean prepared;

        public GlTexture(Identifier id, int sample) {
            this.id = id;
            this.sample = sample;
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, this);
        }

        public void sample() {
            Framebuffer input = MinecraftClient.getInstance().getFramebuffer();
            if (width != input.textureWidth || height != input.textureHeight) {
                close();
                width = input.textureWidth;
                height = input.textureHeight;
            }
            if (glTexture == null || glTexture.isClosed()) {
                glTexture = RenderSystem.getDevice().createTexture(() -> "PS_MotionBlurFrame" + sample + this.hashCode(), TextureFormat.RGBA8, input.textureWidth, input.textureHeight, 1);
                width = input.textureWidth;
                height = input.textureHeight;
            }
            CommandEncoder inCommand = RenderSystem.getDevice().createCommandEncoder();
            inCommand.copyTextureToTexture(input.getColorAttachment(), glTexture, 0, 0, 0, 0, 0, input.textureWidth, input.textureHeight);
            inCommand.copyTextureToTexture(input.getColorAttachment(), glTexture, 0, 0, 0, 0, 0, input.textureWidth, input.textureHeight);
            prepared = true;
        }

        public void reset() {
            prepared = false;
            glTexture.close();
        }

        public void drawToScreen(DrawContext context) {
            float alpha = Math.min(1, sample * 0.008F * motionBlur);

            if (prepared && alpha > 0) {
                RenderUtil.drawTexture(context, id, width, height, 1, 1, 1, alpha);
            }
        }
    }
}

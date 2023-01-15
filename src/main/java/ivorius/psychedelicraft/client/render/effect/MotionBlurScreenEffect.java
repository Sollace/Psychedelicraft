/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import java.util.*;
import java.util.stream.IntStream;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

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
        DrugProperties drugProperties = DrugProperties.of(MinecraftClient.getInstance().player);

        if (PsychedelicraftClient.getConfig().visual.doMotionBlur && drugProperties != null) {
            motionBlur = drugProperties.getModifier(Drug.MOTION_BLUR);
        } else {
            motionBlur = 0;
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        if (motionBlur > 0) {
            if (textures != null && textures.sizeChanged(screenWidth, screenHeight)) {
                close();
            }

            if (textures == null) {
                textures = new GlTextureSet(screenWidth, screenHeight, MAX_SAMPLES);
            }

            if (previousTicks > ticks) {
                previousTicks = ticks;
            } else if (previousTicks + SAMPLE_FREQUENCY * MAX_SAMPLES < ticks) {
                previousTicks = ticks - SAMPLE_FREQUENCY * MAX_SAMPLES;
            }

            while (previousTicks + SAMPLE_FREQUENCY <= ticks) {
                currentSample++;
                currentSample %= MAX_SAMPLES;
                textures.getTexture(currentSample).sample();
                previousTicks += SAMPLE_FREQUENCY;
            }

            if (pingPong != null) {
                pingPong.pingPong();
                ScreenEffect.drawScreen(screenWidth, screenHeight);
            }

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
            textures.drawToScreen(currentSample);
            RenderSystem.disableBlend();
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
            textures = IntStream.range(1, samples).mapToObj(i -> new GlTexture(i, width, height)).toList();
        }

        public GlTexture getTexture(int sample) {
            return textures.get(sample);
        }

        public boolean sizeChanged(int width, int height) {
            return this.width != width || this.height != height;
        }

        public void drawToScreen(int currentSample) {
            for (int i = 0; i < textures.size(); i++) {
                int sampleIndex = (i + currentSample) % textures.size();

                textures.get(sampleIndex).drawToScreen();
            }
        }

        @Override
        public void close() {
            textures.forEach(GlTexture::close);
        }
    }

    private class GlTexture implements AutoCloseable {
        private int id;

        private final int sample;
        private final int width;
        private final int height;

        private boolean prepared;

        public GlTexture(int sample, int width, int height) {
            this.sample = sample;
            this.width = width;
            this.height = height;
            id = TextureUtil.generateTextureId();
            TextureUtil.prepareImage(id, width, height);
        }

        public void sample() {
            RenderSystem.bindTexture(id);
            GLStateProxy.copyTexture(0, 0, 0, 0, width, height);
            prepared = true;
        }

        public void reset() {
            prepared = false;
        }

        public void drawToScreen() {
            float alpha = Math.min(1, sample * 0.02f * motionBlur);

            if (prepared && alpha > 0) {
                RenderSystem.setShaderColor(1, 1, 1, alpha);
                RenderSystem.bindTexture(id);
                ScreenEffect.drawScreen(width, height);
            }
        }

        @Override
        public void close() {
            TextureUtil.releaseTextureId(id);
            id = -1;
        }
    }
}

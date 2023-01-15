/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import ivorius.psychedelicraft.client.render.effect.*;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.hallucination.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;

import com.mojang.blaze3d.systems.RenderSystem;

import javax.annotation.ParametersAreNonnullByDefault;

import org.joml.*;
import java.lang.Math;

/**
 * Created by lukas on 17.02.14.
 */
public class DrugRenderer {
    public static final DrugRenderer INSTANCE = new DrugRenderer();

    static final int SCREEN_Z_OFFSET = -90;

    public static void drawOverlay(MatrixStack matrices, float alpha,
            int width, int height,
            Identifier texture,
            float u0, float v0,
            float u1, float v1, int offset) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        RenderSystem.setShaderTexture(0, texture);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        float x0 = -offset;
        float y0 = -offset;
        float x1 = width - x0;
        float y1 = height - y0;

        buffer.vertex(positionMatrix, x0, y1, SCREEN_Z_OFFSET).texture(u0, v1).next();
        buffer.vertex(positionMatrix, x1, y1, SCREEN_Z_OFFSET).texture(u1, v1).next();
        buffer.vertex(positionMatrix, x1, y0, SCREEN_Z_OFFSET).texture(u1, v0).next();
        buffer.vertex(positionMatrix, x0, y0, SCREEN_Z_OFFSET).texture(u0, v0).next();
        Tessellator.getInstance().draw();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Deprecated
    public static void bindTexture(Identifier resourceLocation) {
        RenderSystem.setShaderTexture(0, resourceLocation);
    }

    private final EnvironmentalScreenEffect environmentalEffects = new EnvironmentalScreenEffect();
    private final ScreenEffect screenEffects = CompoundScreenEffect.of(
            new LensFlareScreenEffect(),
            new WarmthOverlayScreenEffect(),
            new AlcoholOverlayScreenEffect(),
            new PowerOverlayScreenEffect(),
            environmentalEffects,
            new MotionBlurScreenEffect()
    );

    public ScreenEffect getScreenEffects() {
        return screenEffects;
    }

    public EnvironmentalScreenEffect getEnvironmentalEffects() {
        return environmentalEffects;
    }

    public void update(DrugProperties drugProperties, LivingEntity entity) {
        screenEffects.update(MinecraftClient.getInstance().getTickDelta());
    }

    @ParametersAreNonnullByDefault
    public void distortScreen(MatrixStack matrices, float tickDelta) {
        DrugProperties properties = DrugProperties.of(MinecraftClient.getInstance().player);
        if (properties == null) {
            return;
        }

        float wobblyness = Math.min(1, properties.getModifier(Drug.VIEW_WOBBLYNESS));

        int frame = properties.asEntity().age;
        float tick = frame + tickDelta;

        if (wobblyness > 0) {
            float f4 = MathHelper.square(5F / (wobblyness * wobblyness + 5F) - wobblyness * 0.04F);

            float sin1 = MathHelper.sin(tick / 150 * MathHelper.PI);
            float sin2 = MathHelper.sin(tick / 170 * MathHelper.PI);
            float sin3 = MathHelper.sin(tick / 190 * MathHelper.PI);

            float yz = tick * 3F * MathHelper.RADIANS_PER_DEGREE;
            Quaternionf rotation = new Quaternionf().rotateXYZ(0, yz, yz);
            matrices.multiply(rotation);
            matrices.scale(
                    1F / (f4 + (wobblyness * sin1) / 2),
                    1F / (f4 + (wobblyness * sin2) / 2),
                    1F / (f4 + (wobblyness * sin3) / 2)
            );
            matrices.multiply(rotation.invert());
        }

        matrices.translate(
                DrugEffectInterpreter.getCameraShiftX(properties, tick),
                DrugEffectInterpreter.getCameraShiftY(properties, tick),
                0
        );
    }

    public void distortHand(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        int rendererUpdateCount = client.inGameHud.getTicks();
        float partialTicks = client.getTickDelta();

        DrugProperties.of((Entity)client.player).ifPresent(drugProperties -> {
            float shiftX = DrugEffectInterpreter.getHandShiftX(drugProperties, rendererUpdateCount + partialTicks);
            float shiftY = DrugEffectInterpreter.getHandShiftY(drugProperties, rendererUpdateCount + partialTicks);
            matrices.translate(shiftX, shiftY, 0);
        });
    }

    public void onRenderOverlay(MatrixStack matrices, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        getScreenEffects().render(matrices,
                client.getBufferBuilders().getEntityVertexConsumers(),
                window.getScaledWidth(), window.getScaledHeight(), tickDelta, null);
    }

    public void renderAllHallucinations(MatrixStack matrices, VertexConsumerProvider vertices, Camera camera, float tickDelta, DrugProperties drugProperties) {
        HallucinationManager hallucinations = drugProperties.getHallucinations();
        float alpha = MathHelper.clamp(hallucinations.getHallucinationStrength(tickDelta) * 15, 0, 1);
        for (Hallucination h : hallucinations.getEntities()) {
            h.render(matrices, vertices, camera, tickDelta, alpha);
        }
    }

}

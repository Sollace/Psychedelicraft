/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.effect.*;
import ivorius.psychedelicraft.client.render.shader.PostEffectRenderer;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.hallucination.Hallucination;
import ivorius.psychedelicraft.entity.drug.hallucination.HallucinationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

import java.lang.Math;

import org.joml.Quaternionf;

/**
 * Created by lukas on 17.02.14.
 */
public class DrugRenderer {
    public static final DrugRenderer INSTANCE = new DrugRenderer();

    private final EnvironmentalScreenEffect environmentalEffects = new EnvironmentalScreenEffect();
    private final ScreenEffect screenEffects = CompoundScreenEffect.of(
            new LensFlareScreenEffect(),
            new WarmthOverlayScreenEffect(),
            new AlcoholOverlayScreenEffect(),
            new PowerOverlayScreenEffect(),
            environmentalEffects,
            new MotionBlurScreenEffect()
    );

    private final PostEffectRenderer postEffects = new PostEffectRenderer();

    private float screenBackgroundBlur;

    public ScreenEffect getScreenEffects() {
        return screenEffects;
    }

    public PostEffectRenderer getPostEffects() {
        return postEffects;
    }

    public EnvironmentalScreenEffect getEnvironmentalEffects() {
        return environmentalEffects;
    }

    public float getMenuBlur() {
        return PsychedelicraftClient.getConfig().visual.pauseMenuBlur * screenBackgroundBlur * screenBackgroundBlur * screenBackgroundBlur;
    }

    public void update(DrugProperties drugProperties, LivingEntity entity) {
        if (MinecraftClient.getInstance().isPaused()) {
            screenBackgroundBlur = Math.min(1, screenBackgroundBlur + 0.25F);
        } else {
            screenBackgroundBlur = Math.max(0, screenBackgroundBlur - 0.25F);
        }

        screenEffects.update(MinecraftClient.getInstance().getTickDelta());
    }

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

    // TODO: Hook required
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

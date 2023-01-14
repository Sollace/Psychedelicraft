/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.effect.EffectLensFlare;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.hallucination.DrugHallucination;
import ivorius.psychedelicraft.entity.drug.hallucination.HallucinationManager;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import org.joml.Quaternionf;

import com.mojang.blaze3d.systems.RenderSystem;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by lukas on 17.02.14.
 */
public class DrugRenderer {
    public static final DrugRenderer INSTANCE = new DrugRenderer();

    public static void renderOverlay(float alpha, int width, int height, Identifier texture, float u0, float v0, float u1, float v1, int offset) {
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        RenderSystem.setShaderTexture(0, texture);
        Tessellator var8 = Tessellator.getInstance();
        BufferBuilder buffer = var8.getBuffer();
        buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(-offset, height + offset, -90.0D).texture(u0, v1).next();
        buffer.vertex(width + offset, height + offset, -90.0D).texture(u1, v1).next();
        buffer.vertex(width + offset, -offset, -90.0D).texture(u1, v0).next();
        buffer.vertex(-offset, -offset, -90.0D).texture(u0, v0).next();
        var8.draw();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static void bindTexture(Identifier resourceLocation) {
        MinecraftClient.getInstance().getTextureManager().bindTexture(resourceLocation);
    }

    public Identifier hurtOverlay = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "hurt_overlay.png");

    public float experiencedHealth = 5F;

    public int timeScreenWet;
    public boolean wasInWater;
    public boolean wasInRain;

    public float currentHeat;

    public final EffectLensFlare effectLensFlare = new EffectLensFlare();

    public void update(DrugProperties drugProperties, LivingEntity entity) {
        if (PsychedelicraftClient.getConfig().visual.hurtOverlayEnabled) {
            experiencedHealth = MathUtils.nearValue(experiencedHealth, entity.getHealth(), 0.01f, 0.01f);
        }

        if (PsychedelicraftClient.getConfig().visual.sunFlareIntensity > 0) {
            effectLensFlare.updateLensFlares();
        }

        wasInWater = entity.world.getFluidState(new BlockPos(entity.getEyePos())).isIn(FluidTags.WATER);
        // TODO: (Sollace) The year is 2023. Can the client handle rain? I think it can now
        //wasInRain = player.worldObj.getRainStrength(1.0f) > 0.0f && player.worldObj.getPrecipitationHeight(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY)) <= player.posY; //Client can't handle rain

        if (PsychedelicraftClient.getConfig().visual.waterOverlayEnabled) {
            timeScreenWet--;

            if (wasInWater) {
                timeScreenWet += 20;
            }
            if (wasInRain) {
                timeScreenWet += 4;
            }

            timeScreenWet = MathHelper.clamp(timeScreenWet, 0, 100);
        }

        BlockPos pos = entity.getBlockPos();
        float newHeat = entity.world.getBiome(pos).value().getTemperature();

        this.currentHeat = MathUtils.nearValue(currentHeat, newHeat, 0.01f, 0.01f);
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

            float yz = tick * 3F;
            matrices.multiply(new Quaternionf().rotateXYZ(0, yz, yz));
            matrices.scale(
                    1F / (f4 + (wobblyness * sin1) / 2),
                    1F / (f4 + (wobblyness * sin2) / 2),
                    1F / (f4 + (wobblyness * sin3) / 2)
            );
            matrices.multiply(new Quaternionf().rotateXYZ(0, -yz, -yz));
        }

        matrices.translate(
                DrugEffectInterpreter.getCameraShiftX(properties, tick),
                DrugEffectInterpreter.getCameraShiftY(properties, tick),
                0
        );
    }

    public void distortHand(MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int rendererUpdateCount = mc.inGameHud.getTicks();
        float partialTicks = mc.getTickDelta();

        DrugProperties.of((Entity)mc.player).ifPresent(drugProperties -> {
            float shiftX = DrugEffectInterpreter.getHandShiftX(drugProperties, rendererUpdateCount + partialTicks);
            float shiftY = DrugEffectInterpreter.getHandShiftY(drugProperties, rendererUpdateCount + partialTicks);
            matrices.translate(shiftX, shiftY, 0);
        });
    }

    public void renderOverlaysBeforeShaders(MatrixStack matrices, float partialTicks, LivingEntity entity, int updateCounter, int width, int height, DrugProperties drugProperties) {
        effectLensFlare.sunFlareIntensity = PsychedelicraftClient.getConfig().visual.sunFlareIntensity;

        if (effectLensFlare.shouldApply(updateCounter + partialTicks)) {
            effectLensFlare.apply(width, height, partialTicks, null);
        }
    }

    public void onRenderOverlay(MatrixStack matrices, float tickDelta) {
        DrugProperties.of((Entity)MinecraftClient.getInstance().player).ifPresent(properties -> {
            renderOverlays(matrices, tickDelta, properties);
        });
    }

    public void renderOverlays(MatrixStack matrices, float partialTicks, DrugProperties drugProperties) {
        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();

        MinecraftClient client = MinecraftClient.getInstance();

        PlayerEntity entity = drugProperties.asEntity();

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        for (Drug drug : drugProperties.getAllDrugs()) {
            drug.drawOverlays(matrices, partialTicks, entity, entity.age, width, height, drugProperties);
        }

        if (PsychedelicraftClient.getConfig().visual.hurtOverlayEnabled && entity.hurtTime > 0 || experiencedHealth < 5F) {
            float p1 = (float) entity.hurtTime / (float) entity.maxHurtTime;
            float p2 = +(5f - experiencedHealth) / 6f;

            float p = p1 > 0.0F ? p1 : 0.0f + p2 > 0.0F ? p2 : 0.0F;
            renderOverlay(p, width, height, hurtOverlay, 0.0F, 0.0F, 1.0F, 1.0F, (int) ((1.0F - p) * 40F));
        }

        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    public void renderAllHallucinations(float tickDelta, DrugProperties drugProperties) {
        HallucinationManager hallucinations = drugProperties.getHallucinations();
        for (DrugHallucination h : hallucinations.getEntities()) {
            h.render(tickDelta, MathHelper.clamp(hallucinations.getHallucinationStrength(tickDelta) * 15, 0, 1));
        }
    }

    public float getCurrentHeatDistortion() {
        return wasInWater ? 0 : MathHelper.clamp(((currentHeat - 1) * 0.0015f), 0, 0.01F);
    }

    public float getCurrentWaterDistortion() {
        return wasInWater ? 0.025F : 0;
    }

    public float getCurrentWaterScreenDistortion() {
        return timeScreenWet > 0 && !wasInWater ? Math.min(1, timeScreenWet / 80F) : 0;
    }
}

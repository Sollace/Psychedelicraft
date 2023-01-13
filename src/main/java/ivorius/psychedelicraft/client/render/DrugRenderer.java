/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.effect.EffectLensFlare;
import ivorius.psychedelicraft.entity.drugs.*;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by lukas on 17.02.14.
 */
public class DrugRenderer implements IDrugRenderer {
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

    public EffectLensFlare effectLensFlare;

    public DrugRenderer() {
        effectLensFlare = new EffectLensFlare();
        effectLensFlare.sunFlareSizes = new float[]{0.15f, 0.24f, 0.12f, 0.036f, 0.06f, 0.048f, 0.006f, 0.012f, 0.5f, 0.09f, 0.036f, 0.09f, 0.06f, 0.05f, 0.6f};
        effectLensFlare.sunFlareInfluences = new float[]{-1.3f, -2.0f, 0.2f, 0.4f, 0.25f, -0.25f, -0.7f, -1.0f, 1.0f, 1.4f, -1.31f, -1.2f, -1.5f, -1.55f, -3.0f};
        effectLensFlare.sunBlindnessTexture = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "sun_blindness.png");
        effectLensFlare.sunFlareTextures = new Identifier[effectLensFlare.sunFlareSizes.length];
        for (int i = 0; i < effectLensFlare.sunFlareTextures.length; i++) {
            effectLensFlare.sunFlareTextures[i] = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "flare" + i + ".png");
        }
    }

    @Override
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
    @Override
    public void distortScreen(float partialTicks, LivingEntity entity, int rendererUpdateCount, DrugProperties drugProperties) {
        float wobblyness = 0.0f;
        for (Drug drug : drugProperties.getAllDrugs()) {
            wobblyness += drug.viewWobblyness();
        }

        if (wobblyness > 0.0F) {
            if (wobblyness > 1.0F) {
                wobblyness = 1.0F;
            }

            float f4 = 5F / (wobblyness * wobblyness + 5F) - wobblyness * 0.04F;
            f4 *= f4;

            float sin1 = MathHelper.sin(((rendererUpdateCount + partialTicks) / 150 * (float) Math.PI));
            float sin2 = MathHelper.sin(((rendererUpdateCount + partialTicks) / 170 * (float) Math.PI));
            float sin3 = MathHelper.sin(((rendererUpdateCount + partialTicks) / 190 * (float) Math.PI));

            GL11.glRotatef((rendererUpdateCount + partialTicks) * 3F, 0.0F, 1.0F, 1.0F);
            GL11.glScalef(1.0F / (f4 + (wobblyness * sin1) / 2), 1.0F / (f4 + (wobblyness * sin2) / 2), 1.0F / (f4 + (wobblyness * sin3) / 2));
            GL11.glRotatef(-(rendererUpdateCount + partialTicks) * 3F, 0.0F, 1.0F, 1.0F);
        }

        float shiftX = DrugEffectInterpreter.getCameraShiftX(drugProperties, rendererUpdateCount + partialTicks);
        float shiftY = DrugEffectInterpreter.getCameraShiftY(drugProperties, rendererUpdateCount + partialTicks);
        GL11.glTranslatef(shiftX, shiftY, 0.0f);
    }

    @Override
    public void renderOverlaysBeforeShaders(MatrixStack matrices, float partialTicks, LivingEntity entity, int updateCounter, int width, int height, DrugProperties drugProperties) {
        effectLensFlare.sunFlareIntensity = PsychedelicraftClient.getConfig().visual.sunFlareIntensity;

        if (effectLensFlare.shouldApply(updateCounter + partialTicks)) {
            effectLensFlare.apply(width, height, partialTicks, null);
        }
    }

    @Override
    public void renderOverlaysAfterShaders(MatrixStack matrices, float partialTicks, LivingEntity entity, int updateCounter, int width, int height, DrugProperties drugProperties) {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();

        for (Drug drug : drugProperties.getAllDrugs()) {
            drug.drawOverlays(matrices, partialTicks, entity, updateCounter, width, height, drugProperties);
        }

        if (PsychedelicraftClient.getConfig().visual.hurtOverlayEnabled && entity.hurtTime > 0 || experiencedHealth < 5F) {
            float p1 = (float) entity.hurtTime / (float) entity.maxHurtTime;
            float p2 = +(5f - experiencedHealth) / 6f;

            float p = p1 > 0.0F ? p1 : 0.0f + p2 > 0.0F ? p2 : 0.0F;
            renderOverlay(p, width, height, hurtOverlay, 0.0F, 0.0F, 1.0F, 1.0F, (int) ((1.0F - p) * 40F));
        }

        RenderSystem.enableDepthTest();
    }

    @Override
    public void renderAllHallucinations(float par1, DrugProperties drugProperties) {
        DrugHallucinationManager hallucinations = drugProperties.getHallucinations();
        for (DrugHallucination h : hallucinations.getHallucinations()) {
            h.render(par1, MathHelper.clamp(hallucinations.getHallucinationStrength(drugProperties, par1) * 15, 0, 1));
        }
    }

    @Override
    public float getCurrentHeatDistortion() {
        return wasInWater ? 0 : MathHelper.clamp(((currentHeat - 1) * 0.0015f), 0, 0.01F);
    }

    @Override
    public float getCurrentWaterDistortion() {
        return wasInWater ? 0.025F : 0;
    }

    @Override
    public float getCurrentWaterScreenDistortion() {
        return timeScreenWet > 0 && !wasInWater ? Math.min(1, timeScreenWet / 80F) : 0;
    }
}

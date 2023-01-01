/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import ivorius.psychedelicraft.entities.drugs.Drug;
import ivorius.psychedelicraft.entities.drugs.DrugHallucination;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by lukas on 17.02.14.
 */
public class DrugRenderer implements IDrugRenderer
{
    public static void renderOverlay(float alpha, int width, int height, Identifier texture, float x0, float y0, float x1, float y1, int offset)
    {
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        bindTexture(texture);
        Tessellator var8 = Tessellator.getInstance();
        BufferBuilder buffer = var8.getBuffer();
        buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(-offset, height + offset, -90.0D).texture(x0, y1).next();
        buffer.vertex(width + offset, height + offset, -90.0D).texture(x1, y1).next();
        buffer.vertex(width + offset, -offset, -90.0D).texture(x1, y0).next();
        buffer.vertex(-offset, -offset, -90.0D).texture(x0, y0).next();
        var8.draw();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static void bindTexture(Identifier resourceLocation) {
        MinecraftClient.getInstance().getTextureManager().bindTexture(resourceLocation);
    }

    public Identifier hurtOverlay;

    public float experiencedHealth = 5F;

    public int timeScreenWet;
    public boolean wasInWater;
    public boolean wasInRain;

    public float currentHeat;

    public EffectLensFlare effectLensFlare;

    public DrugRenderer()
    {
        hurtOverlay = Psychedelicraft.id(Psychedelicraft.filePathTextures + "hurtOverlay.png");

        effectLensFlare = new EffectLensFlare();
        effectLensFlare.sunFlareSizes = new float[]{0.15f, 0.24f, 0.12f, 0.036f, 0.06f, 0.048f, 0.006f, 0.012f, 0.5f, 0.09f, 0.036f, 0.09f, 0.06f, 0.05f, 0.6f};
        effectLensFlare.sunFlareInfluences = new float[]{-1.3f, -2.0f, 0.2f, 0.4f, 0.25f, -0.25f, -0.7f, -1.0f, 1.0f, 1.4f, -1.31f, -1.2f, -1.5f, -1.55f, -3.0f};
        effectLensFlare.sunBlindnessTexture = Psychedelicraft.id(Psychedelicraft.filePathTextures + "sunBlindness.png");
        effectLensFlare.sunFlareTextures = new Identifier[effectLensFlare.sunFlareSizes.length];
        for (int i = 0; i < effectLensFlare.sunFlareTextures.length; i++)
        {
            effectLensFlare.sunFlareTextures[i] = Psychedelicraft.id(Psychedelicraft.filePathTextures + "flare" + i + ".png");
        }
    }

    @Override
    public void update(DrugProperties drugProperties, LivingEntity entity)
    {
        if (DrugProperties.hurtOverlayEnabled)
        {
            experiencedHealth = IvMathHelper.nearValue(experiencedHealth, entity.getHealth(), 0.01f, 0.01f);
        }

        if (PSRenderStates.sunFlareIntensity > 0.0f)
        {
            effectLensFlare.updateLensFlares();
        }

        Block bID = ActiveRenderInfo.getBlockAtEntityViewpoint(entity.world, entity, 1.0F);
        wasInWater = bID.getMaterial() == Material.WATER;
        //wasInRain = player.worldObj.getRainStrength(1.0f) > 0.0f && player.worldObj.getPrecipitationHeight(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY)) <= player.posY; //Client can't handle rain

        if (DrugProperties.waterOverlayEnabled)
        {
            timeScreenWet--;

            if (wasInWater)
            {
                timeScreenWet += 20;
            }
            if (wasInRain)
            {
                timeScreenWet += 4;
            }

            timeScreenWet = MathHelper.clamp(timeScreenWet, 0, 100);
        }

        BlockPos pos = entity.getBlockPos();
        float newHeat = entity.world.getBiome(pos).value().getTemperature();

        this.currentHeat = IvMathHelper.nearValue(currentHeat, newHeat, 0.01f, 0.01f);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void distortScreen(float partialTicks, LivingEntity entity, int rendererUpdateCount, DrugProperties drugProperties)
    {
        float wobblyness = 0.0f;
        for (Drug drug : drugProperties.getAllDrugs())
            wobblyness += drug.viewWobblyness();

        if (wobblyness > 0.0F)
        {
            if (wobblyness > 1.0F)
                wobblyness = 1.0F;

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
    public void renderOverlaysBeforeShaders(float partialTicks, LivingEntity entity, int updateCounter, int width, int height, DrugProperties drugProperties)
    {
        effectLensFlare.sunFlareIntensity = PSRenderStates.sunFlareIntensity;

        if (effectLensFlare.shouldApply(updateCounter + partialTicks))
        {
            effectLensFlare.renderLensFlares(width, height, partialTicks);
        }
    }

    @Override
    public void renderOverlaysAfterShaders(float partialTicks, LivingEntity entity, int updateCounter, int width, int height, DrugProperties drugProperties)
    {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();

        for (Drug drug : drugProperties.getAllDrugs())
            drug.drawOverlays(partialTicks, entity, updateCounter, width, height, drugProperties);

        if (DrugProperties.hurtOverlayEnabled)
        {
            if (entity.hurtTime > 0 || experiencedHealth < 5F)
            {
                float p1 = (float) entity.hurtTime / (float) entity.maxHurtTime;
                float p2 = +(5f - experiencedHealth) / 6f;

                float p = p1 > 0.0F ? p1 : 0.0f + p2 > 0.0F ? p2 : 0.0F;
                renderOverlay(p, width, height, hurtOverlay, 0.0F, 0.0F, 1.0F, 1.0F, (int) ((1.0F - p) * 40F));
            }
        }

        RenderSystem.enableDepthTest();
    }

    @Override
    public void renderAllHallucinations(float par1, DrugProperties drugProperties)
    {
        for (DrugHallucination h : drugProperties.hallucinationManager.entities)
        {
            h.render(par1, MathHelper.clamp(drugProperties.hallucinationManager.getHallucinationStrength(drugProperties, par1) * 15.0f, 0.0f, 1.0f));
        }
    }

    @Override
    public float getCurrentHeatDistortion()
    {
        if (wasInWater)
            return 0.0f;

        return MathHelper.clamp(((currentHeat - 1.0f) * 0.0015f), 0.0f, 0.01f);
    }

    @Override
    public float getCurrentWaterDistortion()
    {
        return wasInWater ? 0.025f : 0.0f;
    }

    @Override
    public float getCurrentWaterScreenDistortion()
    {
        if (timeScreenWet > 0 && !wasInWater)
        {
            float p = timeScreenWet / 80f;
            if (p > 1.0F)
            {
                p = 1.0F;
            }

            return p;
        }

        return 0.0f;
    }
}

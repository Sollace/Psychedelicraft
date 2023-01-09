/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities.drugs.effects;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Created by lukas on 01.11.14.
 */
public class DrugWarmth extends DrugSimple {
    private static final Identifier COFFEE_OVERLAY = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "coffeeOverlayBlend.png");

    public DrugWarmth(double decSpeed, double decSpeedPlus) {
        super(decSpeed, decSpeedPlus, true);
    }

    @Override
    public float bloomHallucinationStrength() {
        return (float)getActiveValue() * 0.5F;
    }

    @Override
    public float superSaturationHallucinationStrength() {
        return (float)getActiveValue() * 0.1F;
    }

    @Override
    public void drawOverlays(MatrixStack matrices, float partialTicks, LivingEntity entity, int updateCounter, int width, int height, DrugProperties drugProperties) {
        float warmth = (float)getActiveValue();
        if (warmth > 0) {
            renderWarmthOverlay(matrices, warmth * 0.5F, width, height, updateCounter);
        }
    }

    private void renderWarmthOverlay(MatrixStack matrices, float alpha, int width, int height, int ticks) {
        RenderSystem.setShaderTexture(0, COFFEE_OVERLAY);
        Tessellator var8 = Tessellator.getInstance();
        BufferBuilder buffer = var8.getBuffer();

        for (int i = 0; i < 3; i++) {
            int steps = 30;

            float prevXL = -1;
            float prevXR = -1;
            float prevY = -1;
            boolean init = false;

            for (int y = 0; y < steps; y++) {
                float prog = (float) (steps - y) / (float) steps;

                RenderSystem.setShaderColor(1.0f, 0.5f + prog * 0.3f, 0.35f + prog * 0.1f, alpha - prog * 0.4f);

                int segWidth = width / 9;

                int xM = (int) (segWidth * (i * 3 + 1.5f));

                float xShift = MathHelper.sin((float) y / (float) steps * 5.0f + ticks / 10.0f);
                float mXL = xM - segWidth + xShift * segWidth * 0.25f;
                float mXR = xM + segWidth + xShift * segWidth * 0.25f;
                float mY = (float) y / (float) steps * height / 7 * 5 + height / 7;

                if (init) {
                    buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    buffer.vertex(mXL, mY, -90.0D).texture(0, (float) y / (float) steps).next();
                    buffer.vertex(mXR, mY, -90.0D).texture(1, (float) y / (float) steps).next();
                    buffer.vertex(prevXR, prevY, -90.0D).texture(1, (float) (y - 1) / (float) steps).next();
                    buffer.vertex(prevXL, prevY, -90.0D).texture(0, (float) (y - 1) / (float) steps).next();
                    var8.draw();
                } else {
                    init = true;
                }

                prevY = mY;
                prevXL = mXL;
                prevXR = mXR;
            }
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}

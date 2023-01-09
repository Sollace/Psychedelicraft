/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import ivorius.psychedelicraft.entities.EntityRealityRift;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Random;

/**
 * Created by lukas on 03.03.14.
 */
public class RealityRiftEntityRenderer extends EntityRenderer<EntityRealityRift> {
    public static final Identifier[] zeroScreenTexture;
    public static final Identifier zeroCenterTexture = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "zeroCenter.png");

    static {
        zeroScreenTexture = new Identifier[8];
        for (int i = 0; i < zeroScreenTexture.length; i++)
        {
            zeroScreenTexture[i] = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "zeroScreen" + i + ".png");
        }
    }

    public RealityRiftEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(EntityRealityRift entity) {
        return zeroCenterTexture;
    }

    @Override
    public void render(EntityRealityRift entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.translate(0, entity.getHeight() * 0.5, 0);

        float visualRiftSize = entity.visualRiftSize < 0.01f
                ? (entity.visualRiftSize * 10.0f)
                : (0.1f + (entity.visualRiftSize - 0.01f) * 0.1f);

        matrices.scale(visualRiftSize, visualRiftSize, visualRiftSize);

        float critStatus = entity.getCriticalStatus();
        renderRift(matrices, vertices, tickDelta, entity.age + tickDelta + (critStatus * critStatus * 3000));

        // TODO: (Sollace) I think this is meant to be a particle...?
        //matrices.scale(5F, 5F, 5F);
        //RenderSystem.enableBlend();
//        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        //RenderSystem.defaultBlendFunc();
        //bindTexture(zeroCenterTexture);
        //IvRenderHelper.renderParticle(Tessellator.instance, tickDelta, 1.0f);
        //RenderSystem.disableBlend();
        matrices.pop();
    }

    public void renderRift(MatrixStack matrices, VertexConsumerProvider vertices, float partialTicks, float ticks) {
        float pixelsX = 140 / 2f;
        float pixelsY = 224 / 2f;

        PSRenderStates.setUseScreenTexCoords(true);
        PSRenderStates.setPixelSize(1.0f / pixelsX, -1.0f / pixelsY);

        int textureChosen = MathHelper.floor(ticks * 0.5f);
        Random rng = new Random(textureChosen);
        GL11.glTexCoord2f(rng.nextInt(10) * 0.1f * pixelsX, rng.nextInt(8) * 0.125f * pixelsY);

        renderLightsScreen(matrices, vertices.getBuffer(RenderLayer.getEntityTranslucent(zeroScreenTexture[textureChosen % 8])), ticks, 1, 0xffffffff, 20);

        PSRenderStates.setScreenSizeDefault();
        PSRenderStates.setUseScreenTexCoords(false);
    }

    public static void renderLightsScreen(MatrixStack matrices, VertexConsumer vertices, float ticks, float alpha, int color, int number) {
        // RenderHelper.disableStandardItemLighting();

        Random random = new Random(432L);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        RenderSystem.enableBlend();
//        GL11.glEnable(GL11.GL_CULL_FACE);
        RenderSystem.depthMask(false);

        matrices.push();

        float width = 2.5F;
        float rotation = ticks / 200F;

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        for (int i = 0; i < number; ++i) {
            float xLogFunc = (((float) i / number * 28493.0f + ticks) / 10F) % 20F;
            if (xLogFunc > 10) {
                xLogFunc = 20 - xLogFunc;
            }

            float lightAlpha = 1F / (1 + (float) Math.pow(2.71828f, -0.8F * xLogFunc) * ((1F / 0.01F) - 1));

            if (lightAlpha > 0.01F) {
                matrices.multiply(new Quaternionf().rotateXYZ(
                        random.nextFloat() * 360,
                        random.nextFloat() * 360,
                        random.nextFloat() * 360
                ));
                matrices.multiply(new Quaternionf().rotateXYZ(
                        random.nextFloat() * 360,
                        random.nextFloat() * 360,
                        random.nextFloat() * 360 + rotation * 90
                ));

                float var8 = random.nextFloat() * 20 + 5;
                float var9 = random.nextFloat() * 2 + 1;
                vertices.vertex(0, 0, 0).color(1F, 1F, 1F, alpha * lightAlpha);
                vertices.vertex(positionMatrix, -width * var9, var8, -0.5F * var9).color(1F, 1F, 1F, 1F).next();
                vertices.vertex(positionMatrix,  width * var9, var8, -0.5F * var9).color(1F, 1F, 1F, 1F).next();
                vertices.vertex(positionMatrix,             0, var8,         var9).color(1F, 1F, 1F, 1F).next();
                vertices.vertex(positionMatrix, -width * var9, var8, -0.5F * var9).color(1F, 1F, 1F, 1F).next();
            }
        }

        matrices.pop();

        RenderSystem.depthMask(true);
//        GL11.glDisable(GL11.GL_CULL_FACE);
        RenderSystem.disableBlend();
        GL11.glShadeModel(GL11.GL_FLAT);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        // RenderHelper.enableStandardItemLighting();
    }
}

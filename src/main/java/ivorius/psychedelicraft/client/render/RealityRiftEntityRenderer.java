/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.EntityRealityRift;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import org.joml.*;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Random;
import java.lang.Math;

/**
 * Created by lukas on 03.03.14.
 */
public class RealityRiftEntityRenderer extends EntityRenderer<EntityRealityRift> {
    public static final Identifier CENTER_TEXTURE = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "zero_center.png");
    private static final Random RANDOM = new Random(432L);

    public RealityRiftEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(EntityRealityRift entity) {
        return CENTER_TEXTURE;
    }

    @Override
    public void render(EntityRealityRift entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.translate(0, entity.getHeight() * 0.5, 0);

        float visualRiftSize = entity.visualRiftSize < 0.01f
                ? (entity.visualRiftSize * 10.0f)
                : (0.1f + (entity.visualRiftSize - 0.01f) * 0.1f);

        matrices.scale(visualRiftSize, visualRiftSize, visualRiftSize);

        float instability = entity.getInstability();
        renderRift(matrices, vertices, tickDelta, entity.age + tickDelta + (instability * instability * 3000));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        VertexConsumer consumer = vertices.getBuffer(RenderLayer.getEntityTranslucentEmissive(CENTER_TEXTURE));
        Vector4f vector = new Vector4f(0, 0, 0, 1);

        matrices.push();
        matrices.scale(5F, 5F, 5F);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        float size = 1;

        light = 0;

        Quaternionf cameraRotation = MinecraftClient.getInstance().gameRenderer.getCamera().getRotation();
        matrices.multiply(cameraRotation);
        matrices.translate(-size * 0.5F, -size * 0.5F, 0);

        vector.set(0, 0, 0, 1);
        Vector4f pos = positionMatrix.transform(vector);
        consumer.vertex(pos.x, pos.y, pos.z, 1, 1, 1, 1, 0, 0, light, 0, 1, 1, 1);

        vector.set(size, 0, 0, 1);
        pos = positionMatrix.transform(vector);
        consumer.vertex(pos.x, pos.y, pos.z, 1, 1, 1, 1, 1, 0, light, 0, 1, 1, 1);

        vector.set(size, size, 0, 1);
        pos = positionMatrix.transform(vector);
        consumer.vertex(pos.x, pos.y, pos.z, 1, 1, 1, 1, 1, 1, light, 0, 1, 1, 1);

        vector.set(0, size, 0, 1);
        pos = positionMatrix.transform(vector);
        consumer.vertex(pos.x, pos.y, pos.z, 1, 1, 1, 1, 0, 1, light, 0, 1, 1, 1);

        matrices.pop();

        RenderSystem.disableBlend();

        matrices.pop();
    }

    public void renderRift(MatrixStack matrices, VertexConsumerProvider vertices, float partialTicks, float ticks) {
     // TODO: (Sollace) Can't call it directly
     //   GL11.glShadeModel(GL11.GL_SMOOTH);
        ZeroScreen.render(ticks, (texture, u, v) -> {
            renderLightsScreen(matrices, vertices.getBuffer(RenderLayer.getEntityTranslucentEmissive(texture)), u, v, ticks, 1, 0xffffffff, 20);
        });
      // TODO: (Sollace) Can't call it directly
      //  GL11.glShadeModel(GL11.GL_FLAT);
    }



    public static void renderLightsScreen(MatrixStack matrices, VertexConsumer vertices, float u, float v, float ticks, float alpha, int color, int number) {
        RANDOM.setSeed(432L);
        matrices.push();

        float width = 2.5F;
        float rotation = ticks / 200F;

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        int light = 0x0;

        Vector4f vector = new Vector4f(0, 0, 0, 1);

        for (int i = 0; i < number; ++i) {
            float xLogFunc = (((float) i / number * 28493.0f + ticks) / 10F) % 20F;
            if (xLogFunc > 10) {
                xLogFunc = 20 - xLogFunc;
            }

            float lightAlpha = 1F / (1 + (float) Math.pow(2.71828f, -0.8F * xLogFunc) * ((1F / 0.01F) - 1));

            if (lightAlpha > 0.01F) {
                matrices.multiply(new Quaternionf().rotateXYZ(
                        RANDOM.nextFloat() * MathHelper.TAU,
                        RANDOM.nextFloat() * MathHelper.TAU,
                        RANDOM.nextFloat() * MathHelper.TAU
                ));
                matrices.multiply(new Quaternionf().rotateXYZ(
                        RANDOM.nextFloat() * MathHelper.TAU,
                        RANDOM.nextFloat() * MathHelper.TAU,
                        RANDOM.nextFloat() * MathHelper.TAU + rotation * MathHelper.HALF_PI * 0.5F
                ));

                float var8 = RANDOM.nextFloat() * 20 + 5;
                float var9 = RANDOM.nextFloat() * 2 + 1;

                vector.set(0, 0, 0, 1);
                Vector4f pos = positionMatrix.transform(vector);
                float centerAlpha = alpha * lightAlpha;

                vertices.vertex(pos.x, pos.y, pos.z, 1, 1, 1, centerAlpha, 0, 0, light, 0, 1, 1, 1);

                vector.set(-width * var9, var8, -0.5F * var9, 1);
                pos = positionMatrix.transform(vector);
                vertices.vertex(pos.x, pos.y, pos.z, 1, 1, 1, 0, 1, 0, light, 0, 1, 1, 1);

                vector.set(width * var9, var8, -0.5F * var9, 1);
                pos = positionMatrix.transform(vector);
                vertices.vertex(pos.x, pos.y, pos.z, 1, 1, 1, 0, 0, 1, light, 0, 1, 1, 1);

                vector.set(0, var8, var9, 1);
                pos = positionMatrix.transform(vector);
                vertices.vertex(pos.x, pos.y, pos.z, 1, 1, 1, 0, 1, 1, light, 0, 1, 1, 1);

                vector.set(-width * var9, var8, -0.5F * var9, 1);
                pos = positionMatrix.transform(vector);
                vertices.vertex(pos.x, pos.y, pos.z, 1, 1, 1, 0, 1, 1, light, 0, 1, 1, 1);
            }
        }

        matrices.pop();

    }
}

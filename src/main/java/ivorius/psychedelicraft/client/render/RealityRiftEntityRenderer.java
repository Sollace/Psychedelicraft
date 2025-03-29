/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.RealityRiftEntity;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import org.joml.*;

import java.util.Random;
import java.lang.Math;

/**
 * Created by lukas on 03.03.14.
 */
public class RealityRiftEntityRenderer extends EntityRenderer<RealityRiftEntity, RealityRiftEntityRenderer.State> {
    public static final Identifier CENTER_TEXTURE = Psychedelicraft.id("textures/entity/reality_rift/zero_center.png");
    private static final Random RANDOM = new Random(432L);

    public RealityRiftEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(RealityRiftEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        float size = entity.getRiftSize(tickDelta);
        float instability = entity.getInstability();
        state.visualRiftSize = size < 0.01F ? (size * 10)
                : (0.1F + 0.1F * (size - 0.01F));
        state.instability = state.age + tickDelta + (instability * instability * 3000);
    }

    @Override
    protected Box getBoundingBox(RealityRiftEntity entity) {
        return entity.getBoundingBox().expand(20);
    }

    @Override
    public void render(State state, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.translate(0, state.height * 0.5, 0);

        matrices.scale(state.visualRiftSize, state.visualRiftSize, state.visualRiftSize);

        renderRift(matrices, vertices, state.instability);

        VertexConsumer consumer = vertices.getBuffer(RenderLayer.getEntityTranslucentEmissiveNoOutline(CENTER_TEXTURE));
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
        consumer.vertex(pos.x, pos.y, pos.z, Colors.WHITE, 0, 0, light, 0, 1, 1, 1);

        vector.set(size, 0, 0, 1);
        pos = positionMatrix.transform(vector);
        consumer.vertex(pos.x, pos.y, pos.z, Colors.WHITE, 1, 0, light, 0, 1, 1, 1);

        vector.set(size, size, 0, 1);
        pos = positionMatrix.transform(vector);
        consumer.vertex(pos.x, pos.y, pos.z, Colors.WHITE, 1, 1, light, 0, 1, 1, 1);

        vector.set(0, size, 0, 1);
        pos = positionMatrix.transform(vector);
        consumer.vertex(pos.x, pos.y, pos.z, Colors.WHITE, 0, 1, light, 0, 1, 1, 1);

        matrices.pop();

        matrices.pop();
    }

    public void renderRift(MatrixStack matrices, VertexConsumerProvider vertices, float age) {
        ZeroScreen.render(age, (layer, u, v) -> {
            renderLightsScreen(matrices, vertices.getBuffer(layer), u, v, age, 1, 0xffffffff, 20);
        });
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

                int opaque = MathUtils.withAlpha(Colors.WHITE, alpha * lightAlpha);
                int transparent = MathUtils.withAlpha(Colors.WHITE, 0);

                vertices.vertex(pos.x, pos.y, pos.z, opaque, 0, 0, light, 0, 1, 1, 1);

                vector.set(-width * var9, var8, -0.5F * var9, 1);
                pos = positionMatrix.transform(vector);
                vertices.vertex(pos.x, pos.y, pos.z, transparent, 1, 0, light, 0, 1, 1, 1);

                vector.set(width * var9, var8, -0.5F * var9, 1);
                pos = positionMatrix.transform(vector);
                vertices.vertex(pos.x, pos.y, pos.z, transparent, 0, 1, light, 0, 1, 1, 1);

                vector.set(0, var8, var9, 1);
                pos = positionMatrix.transform(vector);
                vertices.vertex(pos.x, pos.y, pos.z, transparent, 1, 1, light, 0, 1, 1, 1);

                vector.set(-width * var9, var8, -0.5F * var9, 1);
                pos = positionMatrix.transform(vector);
                vertices.vertex(pos.x, pos.y, pos.z, transparent, 1, 1, light, 0, 1, 1, 1);
            }
        }

        matrices.pop();

    }


    static class State extends EntityRenderState {
        public float visualRiftSize;
        public float instability;
    }
}

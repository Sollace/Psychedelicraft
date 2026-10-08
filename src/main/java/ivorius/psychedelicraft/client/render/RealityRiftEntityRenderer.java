/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.RealityRiftEntity;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import java.lang.Math;

import org.joml.Quaternionf;

/**
 * Created by lukas on 03.03.14.
 */
public class RealityRiftEntityRenderer extends EntityRenderer<RealityRiftEntity, RealityRiftEntityRenderer.State> {
    public static final Identifier CENTER_TEXTURE = Psychedelicraft.id("textures/entity/reality_rift/zero_center.png");

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
        state.visualRiftSize = size < 0.01F ? (size * 10) : (0.1F + 0.1F * (size - 0.01F));
        state.instability = state.age + (MathHelper.square(instability) * 3000);
    }

    @Override
    protected Box getBoundingBox(RealityRiftEntity entity) {
        return entity.getBoundingBox().expand(20);
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        matrices.translate(0, state.height * 0.5, 0);
        matrices.scale(state.visualRiftSize, state.visualRiftSize, state.visualRiftSize);

        renderRiftBeams(matrices, queue, state.instability, 1, Colors.WHITE, 20);

        OrderedRenderCommandQueue.Custom custom = (transform, buffer) -> {
            RenderUtil.vertex(buffer, transform, 0, 0, 0, 0, 0, state.light, OverlayTexture.DEFAULT_UV);
            RenderUtil.vertex(buffer, transform, 1, 0, 0, 1, 0, state.light, OverlayTexture.DEFAULT_UV);
            RenderUtil.vertex(buffer, transform, 1, 1, 0, 1, 1, state.light, OverlayTexture.DEFAULT_UV);
            RenderUtil.vertex(buffer, transform, 0, 1, 0, 0, 1, state.light, OverlayTexture.DEFAULT_UV);
        };

        matrices.push();
        float openingSize = 5 * MathHelper.square(1 + state.visualRiftSize);
        matrices.scale(openingSize, openingSize, openingSize);
        matrices.multiply(cameraState.orientation);
        matrices.translate(-0.5F, -0.5F, 0);
        queue.submitCustom(matrices, ZeroScreen.translucentCutout(CENTER_TEXTURE), custom);
        matrices.pop();
        matrices.pop();
    }

    public static void renderRiftBeams(MatrixStack matrices, OrderedRenderCommandQueue queue, float ticks, float alpha, int color, int number) {
        matrices.push();
        queue.submitCustom(matrices, ZeroScreen.ZERO_SCREEN, (transform, buffer) -> {
            Random random = Random.create(432L);
            float width = 2.5F;
            float rotation = ticks / 200F;

            int light = LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE;
            int transparent = MathUtils.withAlpha(Colors.WHITE, 0);

            Quaternionf beamRotation = new Quaternionf();

            for (int i = 0; i < number; ++i) {
                float xLogFunc = (((float) i / number * 28493.0f + ticks) / 10F) % 20F;
                if (xLogFunc > 10) {
                    xLogFunc = 20 - xLogFunc;
                }

                float lightAlpha = 1F / (1 + (float) Math.pow(2.71828f, -0.8F * xLogFunc) * ((1F / 0.01F) - 1));

                if (lightAlpha > 0.01F) {
                    beamRotation.rotationXYZ(
                            random.nextFloat() * MathHelper.TAU,
                            random.nextFloat() * MathHelper.TAU,
                            random.nextFloat() * MathHelper.TAU
                    ).rotateXYZ(
                            random.nextFloat() * MathHelper.TAU,
                            random.nextFloat() * MathHelper.TAU,
                            random.nextFloat() * MathHelper.TAU + rotation * MathHelper.HALF_PI * 0.5F
                    );
                    transform.rotate(beamRotation);

                    float var8 = random.nextFloat() * 20 + 5;
                    float var9 = random.nextFloat() * 2 + 1;

                    int opaque = MathUtils.withAlpha(Colors.WHITE, alpha * lightAlpha);

                    RenderUtil.vertex(buffer, transform, 0, 0, 0, opaque, 0, 0, light, OverlayTexture.DEFAULT_UV);
                    RenderUtil.vertex(buffer, transform, -width * var9, var8, -0.5F * var9, transparent, 0, 0, light, OverlayTexture.DEFAULT_UV);
                    RenderUtil.vertex(buffer, transform, width * var9, var8, -0.5F * var9, transparent, 0, 0, light, OverlayTexture.DEFAULT_UV);
                    RenderUtil.vertex(buffer, transform, 0, var8, var9, transparent, 0, 0, light, OverlayTexture.DEFAULT_UV);
                    RenderUtil.vertex(buffer, transform, -width * var9, var8, -0.5F * var9, transparent, 0, 0, light, OverlayTexture.DEFAULT_UV);
                }
            }
        });
        matrices.pop();
    }


    static class State extends EntityRenderState {
        public float visualRiftSize;
        public float instability;
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.hallucinations;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import net.minecraft.util.math.*;

import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.common.util.render.RenderLayerUtil;

import ivorius.psychedelicraft.client.render.command.EffectRenderCommandQueue;
import ivorius.psychedelicraft.client.render.command.OrderedEffectRenderCommandQueue;
import ivorius.psychedelicraft.entity.drug.hallucination.EntityHallucination;

public abstract class AbstractEntityHallucinationRenderer<
    T extends EntityHallucination<?>,
    S extends AbstractEntityHallucinationRenderer.State> implements HallucinationRenderer<T, S> {
    private static final UnaryOperator<@Nullable RenderLayer> TRANSPARENCY_LAYER_TRANSFORM = originalLayer -> RenderLayerUtil.getTexture(originalLayer).map(RenderLayers::entityTranslucent).orElse(null);

    @Override
    public void updateRenderState(T hallucination, S state, float tickDelta, float alpha, CameraRenderState cameraState) {
        HallucinationRenderer.super.updateRenderState(hallucination, state, tickDelta, alpha, cameraState);
        state.color = ColorHelper.fromFloats(state.alpha, hallucination.color[0], hallucination.color[1], hallucination.color[2]);
        state.entity = updateEntityRenderState(hallucination, hallucination.getEntity(), tickDelta);
        state.entity.baseScale = hallucination.scale;
        state.visible = !MathHelper.approximatelyEquals(state.alpha, 0);
        state.cameraState = cameraState;
    }

    protected LivingEntityRenderState updateEntityRenderState(T hallucination, LivingEntity entity, float tickDelta) {
        var state = (LivingEntityRenderState)MinecraftClient.getInstance().getEntityRenderDispatcher().getAndUpdateRenderState(entity, tickDelta);
        state.baseScale = hallucination.scale;
        return state;
    }

    @Override
    public void render(S state, MatrixStack matrices, OrderedRenderCommandQueue queue) {
        if (state.visible && state.entity != null) {
            MinecraftClient.getInstance().getEntityRenderDispatcher().render(state.entity, state.cameraState,
                    state.entity.x - state.cameraState.pos.getX(),
                    state.entity.y - state.cameraState.pos.getY(),
                    state.entity.z - state.cameraState.pos.getZ(), matrices, OrderedEffectRenderCommandQueue.of(
                            queue,
                            new EffectRenderCommandQueue.Customisations(state.color, TRANSPARENCY_LAYER_TRANSFORM)
            ));
        }
    }

    public static class State extends HallucinationRenderState {
        public CameraRenderState cameraState;
        @Nullable
        public LivingEntityRenderState entity;

        public boolean visible;

        public int color;
    }
}

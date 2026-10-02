package ivorius.psychedelicraft.client.render.blocks;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.client.render.QueuedVertexConsumers;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * Block entity renderer that keeps the pre-1.21.9 immediate rendering style.
 * The render state just carries the block entity through to the render call.
 */
@Deprecated
public interface SimpleBlockEntityRenderer<T extends BlockEntity> extends BlockEntityRenderer<T, SimpleBlockEntityRenderer.State<T>> {
    @Deprecated
    @Override
    default State<T> createRenderState() {
        return new State<>();
    }

    @Deprecated
    @Override
    default void updateRenderState(T entity, State<T> state, float tickDelta, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumblingOverlay);
        state.entity = entity;
        state.tickDelta = tickDelta;
    }

    @Deprecated
    @Override
    default void render(State<T> state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        QueuedVertexConsumers.submit(queue, vertices -> render(state.entity, state.tickDelta, matrices, vertices, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, cameraState.pos));
    }

    @Deprecated
    void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay, Vec3d cameraPos);

    @Deprecated
    class State<T extends BlockEntity> extends BlockEntityRenderState {
        public T entity;
        public float tickDelta;
    }
}

package ivorius.psychedelicraft.client.render.hallucinations;

import ivorius.psychedelicraft.entity.drug.hallucination.Hallucination;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;

public interface HallucinationRenderer<T extends Hallucination, S extends HallucinationRenderState> {

    S createRenderState();

    default void updateRenderState(T hallucination, S state, float tickDelta, float alpha, CameraRenderState cameraState) {
        state.type = hallucination.getType();
        state.alpha = alpha * hallucination.getAlpha(tickDelta);
    }

    default S createRenderState(T hallucination, float tickDelta, float alpha, CameraRenderState cameraState) {
        S state = createRenderState();
        updateRenderState(hallucination, state, tickDelta, alpha, cameraState);
        return state;
    }

    void render(S state, MatrixStack matrices, OrderedRenderCommandQueue queue);

    default boolean shouldRender(T hallucination, float alpha, float tickDelta) {
        return alpha * hallucination.getAlpha(tickDelta) > 0;
    }
}

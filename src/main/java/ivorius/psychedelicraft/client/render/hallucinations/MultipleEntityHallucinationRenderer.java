/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.hallucinations;

import java.util.List;

import ivorius.psychedelicraft.entity.drug.hallucination.MultipleEntityHallucination;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class MultipleEntityHallucinationRenderer extends AbstractEntityHallucinationRenderer<MultipleEntityHallucination, MultipleEntityHallucinationRenderer.State> {

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(MultipleEntityHallucination hallucination, State state, float tickDelta, float alpha, CameraRenderState cameraState) {
        super.updateRenderState(hallucination, state, tickDelta, alpha, cameraState);
        state.positions = hallucination.positions.stream().map(pos -> pos.pos(tickDelta).multiply(state.entity.width)).toList();
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue) {
        if (state.visible && state.entity != null) {
            state.positions.forEach(pos -> {
                matrices.push();
                matrices.translate(pos);
                super.render(state, matrices, queue);
                matrices.pop();
            });
        }
    }

    public static class State extends AbstractEntityHallucinationRenderer.State {
        List<Vec3d> positions = List.of();
    }
}

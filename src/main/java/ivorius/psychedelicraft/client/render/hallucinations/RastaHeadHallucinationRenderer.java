/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.hallucinations;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.RastaHeadModel;
import ivorius.psychedelicraft.entity.drug.hallucination.RastaHeadHallucination;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

public class RastaHeadHallucinationRenderer implements HallucinationRenderer<RastaHeadHallucination, RastaHeadHallucinationRenderer.State> {
    private static final Identifier TEXTURE = Psychedelicraft.id("textures/drug/cannabis/rasta_head_hallucination.png");

    private final RastaHeadModel modelRastaHead = new RastaHeadModel();

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(RastaHeadHallucination hallucination, State state, float tickDelta, float alpha, CameraRenderState cameraState) {
        HallucinationRenderer.super.updateRenderState(hallucination, state, tickDelta, alpha, cameraState);
        state.position = hallucination.getPosition(tickDelta).subtract(cameraState.pos);
        state.pitch = hallucination.getPitch(tickDelta);
        state.yaw = 180 + hallucination.getYaw(tickDelta);
        state.scale = hallucination.scale;
        state.light = LightmapTextureManager.pack(hallucination.getLight(), 0);
        state.alpha = alpha;
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue) {
        matrices.push();
        matrices.translate(state.position);
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180));
        queue.submitModel(modelRastaHead, state, matrices, modelRastaHead.getLayer(TEXTURE), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, ColorHelper.getWhite(state.alpha), null, 0, null);
        matrices.pop();
    }

    public static class State extends HallucinationRenderState {
        public Vec3d position = Vec3d.ZERO;
        public float scale;
        public float pitch;
        public float yaw;
        public int light;
    }
}

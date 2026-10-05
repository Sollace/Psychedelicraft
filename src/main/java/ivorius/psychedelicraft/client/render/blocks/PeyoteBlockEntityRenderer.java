/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PeyoteBlock;
import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.client.render.RenderUtil;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.*;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

public class PeyoteBlockEntityRenderer implements BlockEntityRenderer<PeyoteBlockEntity, PeyoteBlockEntityRenderer.State> {
    private static final Identifier[] TEXTURES = IntStream.range(0, 4)
            .mapToObj(i -> Psychedelicraft.id("textures/entity/peyote/peyote_stage" + i + ".png"))
            .toArray(Identifier[]::new);

    private final ModelPart[] models = {
            PeyoteModel.stage0().createModel(),
            PeyoteModel.stage1().createModel(),
            PeyoteModel.stage2().createModel(),
            PeyoteModel.stage3().createModel()
    };

    public PeyoteBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(PeyoteBlockEntity entity, State state, float tickDelta, Vec3d cameraPos, @Nullable CrumblingOverlayCommand crumbling) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumbling);
        Random rng = RenderUtil.random(entity.getCachedState().getRenderingSeed(entity.getPos()));
        state.offset = entity.getCachedState().getModelOffset(entity.getPos()).add(0.5, 1.5, 0.5);
        state.age = entity.getCachedState().get(PeyoteBlock.AGE) % 4;
        state.rotation = rng.nextInt(4) * 180;
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        matrices.translate(state.offset);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.rotation));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        queue.submitModelPart(models[state.age], matrices, RenderLayers.entityCutout(TEXTURES[state.age]), state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, null);
        matrices.pop();
    }

    public static class State extends BlockEntityRenderState {
        public int age;
        public float rotation;
        public Vec3d offset = Vec3d.ZERO;
    }
}

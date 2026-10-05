/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.entity.DistilleryBlockEntity;
import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.client.render.FluidBoxRenderState;
import ivorius.psychedelicraft.client.render.FluidBoxRenderState.FluidAppearance;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 5 Jan 2023
 *
 * Renders fluid inside the flask
 */
public class FlaskBlockEntityRenderer<T extends FlaskBlockEntity> implements BlockEntityRenderer<T, FlaskBlockEntityRenderer.State> {

    public FlaskBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(T entity, State state, float tickDelta, Vec3d cameraPos, @Nullable CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumblingOverlay);
        state.yOffset = !(entity instanceof DistilleryBlockEntity) ? -1 : 0;
        Resovoir tank = entity.getPrimaryTank();
        ItemFluids stack = tank.getContents();
        state.fillPercentage = MathHelper.clamp((float)stack.amount() / tank.getCapacity(), 0, 1);
        state.firstLevelHeight = Math.min(state.fillPercentage * 2, 2);
        state.secondLevelHeight = Math.min(Math.max(state.fillPercentage - 0.5F, 0) * 2, 2);
        state.fluids = stack.isEmpty() ? null : FluidAppearance.of(stack);
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        matrices.translate(0.5F, 0, 0.5F);
        matrices.scale(state.scale, state.scale, state.scale);

        if (state.firstLevelHeight > 0 && state.fluids != null) {
            // lower
            var fluidBox = FluidBoxRenderState.builder()
                    .texture(state.fluids)
                    .light(state.lightmapCoordinates)
                    .crumbling(state.crumblingOverlay)
                    .face(-1, 0, -2, 2, state.firstLevelHeight, 1, Direction.NORTH, Direction.UP)
                    .face(-1, 0,  1, 2, state.firstLevelHeight, 1, Direction.SOUTH, Direction.UP)
                    .face( 1, 0, -1, 1, state.firstLevelHeight, 2, Direction.EAST, Direction.UP)
                    .face(-2, 0, -1, 1, state.firstLevelHeight, 2, Direction.WEST, Direction.UP);
            if (state.secondLevelHeight > 0) {
                // upper
                fluidBox
                    .face(-1, state.yOffset + 4.5F, -1.5F, 2, state.yOffset + state.secondLevelHeight, 0.5F, Direction.NORTH, Direction.UP)
                    .face(-1, state.yOffset + 4.5F,  1, 2, state.yOffset + state.secondLevelHeight, 0.5F, Direction.SOUTH, Direction.UP)

                    .face( 1, state.yOffset + 4.5F, -1, 0.5F, state.yOffset + state.secondLevelHeight, 2, Direction.EAST, Direction.UP)
                    .face(-1.5F, state.yOffset + 4.5F, -1, 0.5F, state.yOffset + state.secondLevelHeight, 2, Direction.WEST, Direction.UP);
            }

            queue.submitCustom(matrices, RenderLayers.entityTranslucent(state.fluids.texture()), fluidBox.build());
        }

        matrices.pop();
    }

    public static class State extends BlockEntityRenderState {
        public float yOffset;
        public float scale = 1/8F - 0.001F;
        public float fillPercentage;
        public float firstLevelHeight;
        public float secondLevelHeight;
        @Nullable
        public FluidAppearance fluids;
    }

}

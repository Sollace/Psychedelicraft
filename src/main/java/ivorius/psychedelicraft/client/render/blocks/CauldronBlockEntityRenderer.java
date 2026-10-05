/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.FluidCauldronBlock;
import ivorius.psychedelicraft.client.render.FluidBoxRenderState;
import ivorius.psychedelicraft.client.render.FluidBoxRenderState.FluidAppearance;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;

/**
 * Renders fluid in the cauldron
 */
public class CauldronBlockEntityRenderer implements BlockEntityRenderer<FluidCauldronBlock.Data, CauldronBlockEntityRenderer.State> {
    private static final float UNIT = 0.0625F;
    private static final float INSET = UNIT * 2;
    // Cauldron model doesn't follow a linear progression, yaaaaaaay
    private static final float[] FLUID_HEIGHTS = { 0, 6.16F, 9.85F, 13.54F };
    public CauldronBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(FluidCauldronBlock.Data entity, State state, float tickDelta, Vec3d cameraPos, @Nullable CrumblingOverlayCommand crumbling) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumbling);
        state.fillPercentage = UNIT * FLUID_HEIGHTS[entity.getCachedState().get(LeveledCauldronBlock.LEVEL)];
        state.fluid = FluidAppearance.of(entity.getFluid());
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.fillPercentage > 0) {
            queue.submitCustom(matrices, RenderLayers.entityTranslucent(state.fluid.texture()), FluidBoxRenderState.builder()
                    .light(state.lightmapCoordinates)
                    .crumbling(state.crumblingOverlay)
                    .texture(state.fluid)
                    .face(INSET, UNIT * 4, INSET, 1 - INSET * 2, state.fillPercentage * (1 - INSET * 1.5F), 1 - INSET * 2, Direction.UP)
                    .build());
        }
    }

    public static class State extends BlockEntityRenderState {
        public float fillPercentage;
        public FluidAppearance fluid = FluidAppearance.EMPTY;
    }

}

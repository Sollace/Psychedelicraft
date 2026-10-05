/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.block.BarrelBlock;
import ivorius.psychedelicraft.block.entity.BarrelBlockEntity;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class BarrelBlockEntityRenderer implements BlockEntityRenderer<BarrelBlockEntity, BarrelBlockEntityRenderer.State> {
    private final BarrelModel model;

    public BarrelBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        model = new BarrelModel(BarrelModel.getTexturedModelData().createModel());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(BarrelBlockEntity entity, State state, float tickDelta, Vec3d cameraPos, @Nullable CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumblingOverlay);
        state.rotation = entity.getCachedState().get(BarrelBlock.FACING).getHorizontalQuarterTurns();
        Resovoir tank = entity.getPrimaryTank();
        ItemFluids stack = tank.getContents();
        state.symbol = null;
        if (!stack.isEmpty()) {
            Identifier symbol = stack.fluid().getSymbol(stack);
            if (MinecraftClient.getInstance().getResourceManager().getResource(symbol).isPresent()) {
                state.symbol = symbol;
            }
        }
        state.axis = entity.getCachedState().get(BarrelBlock.FACING).getAxis();
        state.hasTap = entity.getCachedState().get(BarrelBlock.TAPPED);
        state.tapRotation = entity.getTapRotation(tickDelta);
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        matrices.translate(0.5F, 0, 0.5F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 - 90 * state.rotation));

        queue.submitModel(model, state, matrices, model.getLayer(getBarrelTexture(state.blockState)), state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0, state.crumblingOverlay);

        if (state.symbol != null) {
            matrices.translate(0, 0.5, 0);
            if (state.axis == Axis.Y) {
                matrices.multiplyPositionMatrix(RotationAxis.POSITIVE_X.rotationDegrees(90).get(new Matrix4f()));
                matrices.translate(0, -0.1, 0);
            }

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));

            queue.submitCustom(matrices, model.getLayer(state.symbol), RenderUtil.customWithCrumbling((transform, buffer) -> {
                float barrelZ = -0.4376F + 0.06F;
                float iconSize = 0.5F;
                for (int i = 0; i < 2; i++) {
                    RenderUtil.vertex(buffer, transform, -iconSize, -iconSize, barrelZ, 1, 1, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV);
                    RenderUtil.vertex(buffer, transform, -iconSize,  iconSize, barrelZ, 1, 0, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV);
                    RenderUtil.vertex(buffer, transform,  iconSize,  iconSize, barrelZ, 0, 0, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV);
                    RenderUtil.vertex(buffer, transform,  iconSize, -iconSize, barrelZ, 0, 1, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV);
                    transform.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                }
            }, state.crumblingOverlay));
        }

        matrices.pop();
    }

    @SuppressWarnings("deprecation")
    public static Identifier getBarrelTexture(BlockState state) {
        return state.getBlock().getRegistryEntry().registryKey().getValue().withPath(p -> "textures/entity/barrel/" + p + ".png");
    }

    public static class State extends BlockEntityRenderState {
        public float rotation;
        @Nullable
        public Identifier symbol;
        public Axis axis = Axis.X;

        public boolean hasTap;
        public float tapRotation;
    }

}

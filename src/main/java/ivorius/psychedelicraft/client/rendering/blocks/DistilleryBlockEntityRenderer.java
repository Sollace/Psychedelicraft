/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.blocks;

import ivorius.psychedelicraft.block.entity.DistilleryBlockEntity;
import ivorius.psychedelicraft.blocks.DistilleryBlock;
import ivorius.psychedelicraft.client.rendering.FluidBoxRenderer;
import ivorius.psychedelicraft.fluids.Resovoir;
import ivorius.psychedelicraft.fluids.SimpleFluid;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;

/**
 * Created by lukas on 25.10.14.
 */
public class DistilleryBlockEntityRenderer implements BlockEntityRenderer<DistilleryBlockEntity> {

    public DistilleryBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(DistilleryBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5F, 0.502F, 0.5F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getCachedState().get(DistilleryBlock.FACING).asRotation() + 180));

        Resovoir tank = entity.getTank(Direction.UP);
        SimpleFluid fluid = tank.getFluidType();

        if (!fluid.isEmpty()) {
            float fluidHeight = 2.8f * MathHelper.clamp((float) tank.getLevel() / (float) tank.getCapacity(), 0, 1);

            FluidBoxRenderer fluidRenderer = FluidBoxRenderer.getInstance();
            fluidRenderer.setScale(1F / 16F);
            fluidRenderer.prepare(tank);
            fluidRenderer.renderFluid(-1.9F, -8, -3.9F, 3.8F, fluidHeight, 0.9F, Direction.NORTH, Direction.UP);
            fluidRenderer.renderFluid(-1.9F, -8,     3, 3.8F, fluidHeight, 0.9F, Direction.SOUTH, Direction.UP);
            fluidRenderer.renderFluid(-3.9F, -8, -1.9F, 0.9F, fluidHeight, 3.8F, Direction.WEST, Direction.UP);
            fluidRenderer.renderFluid(    3, -8, -1.9F, 0.9F, fluidHeight, 3.8F, Direction.EAST, Direction.UP);
            fluidRenderer.renderFluid(   -3, -8, -3, 6,       fluidHeight, 6,    Direction.UP);
            fluidRenderer.cleanUp();
        }

        matrices.pop();
    }
}

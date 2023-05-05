/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 5 Jan 2023
 *
 * Renders fluid inside the flask
 */
public class FlaskBlockEntityRenderer<T extends FlaskBlockEntity> implements BlockEntityRenderer<T> {

    public FlaskBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5F, 0, 0.5F);

        float scale = 1/8F - 0.001F;
        matrices.scale(scale, scale, scale);


        Resovoir tank = entity.getTank(Direction.UP);
        SimpleFluid fluid = tank.getFluidType();

        if (!fluid.isEmpty()) {
            float fluidHeight = MathHelper.clamp((float) tank.getLevel() / (float) tank.getCapacity(), 0, 1);

            FluidBoxRenderer fluidRenderer = FluidBoxRenderer.getInstance()
                    .texture(vertices, tank)
                    .light(light).overlay(overlay)
                    .position(matrices);
            fluidRenderer.draw(-1, 0, -2, 2, fluidHeight, 1, Direction.NORTH, Direction.UP);
            fluidRenderer.draw(-1, 0,  1, 2, fluidHeight, 1, Direction.SOUTH, Direction.UP);
            fluidRenderer.draw(-2, 0, -1, 1, fluidHeight, 2, Direction.WEST, Direction.UP);
            fluidRenderer.draw( 1, 0, -1, 1, fluidHeight, 2, Direction.EAST, Direction.UP);
            fluidRenderer.draw(-1, 0, -1, 2, fluidHeight, 2, Direction.UP);
        }

        matrices.pop();
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.fluid.*;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.*;

/**
 * Renders fluid in the mash tub, or the solid contents
 */
public class MashTubBlockEntityRenderer implements BlockEntityRenderer<MashTubBlockEntity> {
    public MashTubBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(MashTubBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        Resovoir tank = entity.getTank(Direction.UP);
        SimpleFluid fluid = tank.getFluidType();

        FluidBoxRenderer.getInstance().scale(1).light(light).overlay(overlay).position(matrices);

        if (!fluid.isEmpty()) {
            float fillPercentage = MathHelper.clamp((float)tank.getLevel() / tank.getCapacity(), 0, 2);

            float fluidHeight = 0.3F + fillPercentage * 0.6F;

            FluidBoxRenderer.getInstance().texture(vertices, tank)
                .draw(-0.5F, 0, -0.5F, 2, fluidHeight, 2, Direction.UP);
        } else if (!entity.solidContents.isEmpty() && entity.solidContents.getItem() instanceof BlockItem) {
            FluidBoxRenderer.getInstance().texture(vertices, entity.solidContents)
                .draw(-0.5F, 0, -0.5F, 2, 0.5F, 2, Direction.UP);
        }
    }
}

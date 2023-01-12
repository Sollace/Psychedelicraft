/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.blocks;

import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.blocks.MashTubBlock;
import ivorius.psychedelicraft.client.rendering.FluidBoxRenderer;
import ivorius.psychedelicraft.fluids.*;
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
    public static final float MODEL_SIZE = MashTubBlock.SIZE / 16F;
    public static final float MODEL_BORDER_WIDTH = MashTubBlock.BORDER_SIZE / 16F;
    public static final float MODEL_HEIGHT = (MashTubBlock.HEIGHT - 4) / 16F;

    public MashTubBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(MashTubBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        //if (!tileEntity.isParent()) return;

        matrices.push();
        //IvMultiBlockRenderHelper.transformFor(tileEntity, x, y, z);
        matrices.translate(0, 0.002F, 0);

        Resovoir tank = entity.getTank(Direction.UP);
        SimpleFluid fluid = tank.getFluidType();

        FluidBoxRenderer.getInstance().scale(1).light(light).overlay(overlay).position(matrices);

        if (!fluid.isEmpty()) {
            float fluidHeight = (MODEL_HEIGHT - MODEL_BORDER_WIDTH - 1F / 16F) * MathHelper.clamp((float) tank.getLevel() / (float) tank.getCapacity(), 0, 1);

            FluidBoxRenderer.getInstance().texture(vertices, tank)
                .draw(-MODEL_SIZE, -.5f + MODEL_BORDER_WIDTH, -MODEL_SIZE, MODEL_SIZE * 2, fluidHeight, MODEL_SIZE * 2, Direction.UP);
        } else if (!entity.solidContents.isEmpty() && entity.solidContents.getItem() instanceof BlockItem) {
            float fluidHeight = (MODEL_HEIGHT - MODEL_BORDER_WIDTH - 1.0f / 16.0f);

            FluidBoxRenderer.getInstance().texture(vertices, entity.solidContents)
                .draw(-MODEL_SIZE, -.5f + MODEL_BORDER_WIDTH, -MODEL_SIZE, MODEL_SIZE * 2, fluidHeight, MODEL_SIZE * 2, Direction.UP);
        }

        matrices.pop();
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.blocks;

import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.blocks.BlockMashTub;
import ivorius.psychedelicraft.client.rendering.FluidBoxRenderer;
import ivorius.psychedelicraft.fluids.Resovoir;
import ivorius.psychedelicraft.fluids.SimpleFluid;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.*;

/**
 * Renders fluid in the mash tub, or the solid contents
 */
public class TileEntityRendererMashTub implements BlockEntityRenderer<MashTubBlockEntity> {
    public static final float MODEL_SIZE = BlockMashTub.SIZE / 16F;
    public static final float MODEL_BORDER_WIDTH = BlockMashTub.BORDER_SIZE / 16F;
    public static final float MODEL_HEIGHT = (BlockMashTub.HEIGHT - 4) / 16F;

    public TileEntityRendererMashTub(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(MashTubBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        //if (!tileEntity.isParent()) return;

        matrices.push();
        //IvMultiBlockRenderHelper.transformFor(tileEntity, x, y, z);
        matrices.translate(0, 0.002F, 0);

        Resovoir tank = entity.getTank(Direction.UP);
        SimpleFluid fluid = tank.getFluidType();
        FluidBoxRenderer fluidRenderer = FluidBoxRenderer.getInstance();

        if (!fluid.isEmpty()) {
            float fluidHeight = (MODEL_HEIGHT - MODEL_BORDER_WIDTH - 1F / 16F) * MathHelper.clamp((float) tank.getLevel() / (float) tank.getCapacity(), 0, 1);

            fluidRenderer.setScale(1);
            fluidRenderer.prepare(tank);
            fluidRenderer.renderFluid(-MODEL_SIZE, -.5f + MODEL_BORDER_WIDTH, -MODEL_SIZE, MODEL_SIZE * 2, fluidHeight, MODEL_SIZE * 2, Direction.UP);
            fluidRenderer.cleanUp();
        } else if (!entity.solidContents.isEmpty() && entity.solidContents.getItem() instanceof BlockItem) {
            float fluidHeight = (MODEL_HEIGHT - MODEL_BORDER_WIDTH - 1.0f / 16.0f);

            fluidRenderer.setScale(1);
            fluidRenderer.prepare(entity.solidContents);
            fluidRenderer.renderFluid(-MODEL_SIZE, -.5f + MODEL_BORDER_WIDTH, -MODEL_SIZE, MODEL_SIZE * 2, fluidHeight, MODEL_SIZE * 2, Direction.UP);
            fluidRenderer.cleanUp();
        }

        matrices.pop();
    }
}

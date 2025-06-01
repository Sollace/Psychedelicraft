/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.block.FluidCauldronBlock;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;

/**
 * Renders fluid in the cauldron
 */
public class CauldronBlockEntityRenderer implements BlockEntityRenderer<FluidCauldronBlock.Data> {
    public CauldronBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(FluidCauldronBlock.Data entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        int level = entity.getCachedState().get(LeveledCauldronBlock.LEVEL);
        if (level > 0) {
            float unit = 0.0625F;
            float inset = unit * 2;
            // Cauldron model doesn't follow a linear progression, yaaaaaaay
            float fillPercentage = level == 1 ? unit * 6.16F : level == 2 ? unit * 9.85F : unit * 13.54F;
            FluidBoxRenderer.getInstance().scale(1).light(light).overlay(overlay).position(matrices)
                .texture(vertices, entity.getFluid())
                .draw(inset, unit * 4, inset, 1 - inset * 2, fillPercentage * (1 - inset * 1.5F), 1 - inset * 2, Direction.UP);
        }
    }
}

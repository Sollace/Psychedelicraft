/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

import ivorius.psychedelicraft.block.entity.DryingTableBlockEntity;

import java.util.Random;

/**
 * Renders items on top of the drying table
 *
 * Updated by Sollace on 5 Jan 2023
 */
public class DryingTableBlockEntityRenderer implements BlockEntityRenderer<DryingTableBlockEntity> {
    public DryingTableBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(DryingTableBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        matrices.push();
        matrices.translate(0, 0.75f, 0);

        long seed = entity.getPos().asLong() + 1;
        Random random = new Random(seed);

        for (int i = 0; i < entity.size(); i++) {
            boolean result = i == 0;
            ItemStack stack = entity.getStack(i);

            if (stack.isEmpty()) {
                continue;
            }

            float positionX = result ? 0.5F : (0.35F + random.nextFloat() * 0.3F);
            float positionZ = result ? 0.5F : (0.35F + random.nextFloat() * 0.3F);
            float rotation = random.nextFloat() * 360.0f;

            matrices.push();
            matrices.translate(positionX, i / 500F, positionZ);

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
            matrices.scale(0.5f, 0.5f, 0.5f);
            if (result) {
                matrices.scale(1.5F, 1.5F, 1.5F);
            }
            matrices.translate(0, 0, -0.2F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(-50));
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, Mode.FIXED, light, overlay, matrices, vertices, (int)seed);

            matrices.pop();
        }

        matrices.pop();
    }
}

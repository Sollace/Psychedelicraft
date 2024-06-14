/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import java.util.Random;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.util.math.*;

/**
 * Renders fluid in the mash tub, or the solid contents
 */
public class MashTubBlockEntityRenderer implements BlockEntityRenderer<MashTubBlockEntity> {
    public MashTubBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(MashTubBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        Resovoir tank = entity.getPrimaryTank();
        ItemFluids stack = tank.getContents();

        float fluidHeight = 0.1F;

        FluidBoxRenderer.getInstance().scale(1).light(light).overlay(overlay).position(matrices);

        if (!stack.isEmpty()) {
            float fillPercentage = MathHelper.clamp((float)stack.amount() / tank.getCapacity(), 0, 2);

            fluidHeight = 0.3F + fillPercentage * 0.6F;

            FluidBoxRenderer.getInstance()
                .texture(vertices, stack)
                .draw(-0.5F, 0, -0.5F, 2, fluidHeight, 2, Direction.UP);
        } else if (!entity.solidContents.isEmpty() && entity.solidContents.getItem() instanceof BlockItem) {
            FluidBoxRenderer.getInstance()
                .texture(vertices, entity.solidContents)
                .draw(-0.5F, 0, -0.5F, 2, 0.5F, 2, Direction.UP);
        }

        matrices.push();
        matrices.translate(0, 0.75f, 0);

        Object2IntMap<Item> ingredients = entity.getSuppliedIngredients();

        long seed = entity.getPos().asLong() + 1;
        Random random = new Random(seed);

        for (Item item : ingredients.keySet()) {
            for (int c = 0; c < ingredients.getInt(item); c++) {
                float positionX = 0.5F + (random.nextFloat() - 0.5F) * 1.5F;
                float positionZ = 0.5F + (random.nextFloat() - 0.5F) * 1.5F;
                float rotation = random.nextFloat() * 360.0f;

                matrices.push();
                matrices.translate(positionX, fluidHeight / 16F - 0.02F, positionZ);

                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
                matrices.scale(0.3F, 0.3F, 0.3F);

                int singleDifference = c * 5;
                float bob = MathHelper.sin((ShaderContext.ticks() + singleDifference) / 8F) * 0.2F;
                float spin = MathHelper.cos((ShaderContext.ticks() + singleDifference) / 8F) * 0.12F;

                matrices.translate(0, bob, -0.2F);
                matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(-50 * spin));
                matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((ShaderContext.ticks() + c) % 360));
                MinecraftClient.getInstance().getItemRenderer().renderItem(item.getDefaultStack(), ModelTransformationMode.FIXED, light, overlay, matrices, vertices, entity.getWorld(), (int)seed);

                matrices.pop();
            }
        }

        matrices.pop();

    }
}

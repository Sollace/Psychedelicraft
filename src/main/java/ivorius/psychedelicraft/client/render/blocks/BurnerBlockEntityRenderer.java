/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class BurnerBlockEntityRenderer implements BlockEntityRenderer<BurnerBlockEntity> {
    public BurnerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(BurnerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        ItemStack container = entity.getContainer();

        if (!container.isEmpty()) {
            matrices.push();

            if (entity.getTemperature() >= 99) {
                float ticks = MinecraftClient.getInstance().player.age + tickDelta;
                float amplitude = 5;//1.65F;
                matrices.translate(0.5, 0, 0.5);

                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(ticks) * amplitude));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(ticks + 9 + MathHelper.sin(ticks)) * amplitude));
                matrices.translate(-0.5, 0, -0.5);
            }
            matrices.translate(0, 0.12, 0);

            PlacedDrinksModelProvider.INSTANCE.renderEmptyDrink("burner", container, matrices, vertices, light, overlay);
            matrices.pop();
        }
    }
}

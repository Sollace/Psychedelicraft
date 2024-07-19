/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.contents.LargeContents;
import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider;
import ivorius.psychedelicraft.fluid.Processable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class BurnerBlockEntityRenderer implements BlockEntityRenderer<BurnerBlockEntity> {
    public BurnerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(BurnerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        if (entity.getContents() instanceof Processable.Context contents && !entity.getContainer().isEmpty()) {
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

            PlacedDrinksModelProvider.INSTANCE.renderEmptyDrink("burner", entity.getContainer(), matrices, vertices, light, overlay);

            if (contents instanceof LargeContents largeContents) {
                var itemRenderer = MinecraftClient.getInstance().getItemRenderer();

                float itemScale = 0.35F;

                matrices.push();
                matrices.translate(0.5, 0, 0.5);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
                matrices.scale(itemScale, itemScale, itemScale);

                Random rng = Random.create(entity.getPos().asLong());

                float y = 0;

                for (var i : largeContents.getIngredients().getCounts().object2IntEntrySet()) {
                    ItemStack stack = i.getKey().getDefaultStack();
                    for (int j = 0; j < i.getIntValue(); j++) {
                        matrices.push();

                        matrices.translate((rng.nextFloat() - 0.5F) * 0.5F, (rng.nextFloat() - 0.5F) * 0.8F, -0.05 + y);
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((rng.nextFloat() * 360) - 180));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((rng.nextFloat() * 360) - 180));
                        y -= 0.1F;

                        itemRenderer.renderItem(stack, ModelTransformationMode.FIXED, light, overlay, matrices, vertices, entity.getWorld(), 0);
                        matrices.pop();
                    }
                }

                matrices.pop();
            }

            matrices.pop();
        }
    }
}

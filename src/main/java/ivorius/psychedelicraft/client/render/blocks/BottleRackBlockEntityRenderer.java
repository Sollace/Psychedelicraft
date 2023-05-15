package ivorius.psychedelicraft.client.render.blocks;

import java.util.Random;

import ivorius.psychedelicraft.block.BottleRackBlock;
import ivorius.psychedelicraft.block.entity.BottleRackBlockEntity;
import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider;
import ivorius.psychedelicraft.client.render.RenderUtil;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.RotationAxis;

/**
 * Created by lukas on 16.11.14.
 * Updated by Sollace on 6 Jan 2023
 */
public class BottleRackBlockEntityRenderer implements BlockEntityRenderer<BottleRackBlockEntity> {
    public BottleRackBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(BottleRackBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5F, 0.5F, 0.5F);
        Direction direction = entity.getCachedState().get(BottleRackBlock.FACING);
        if (direction.getAxis() == Axis.X) {
            direction = direction.getOpposite();
        }
        float facing = direction.asRotation() + 90;

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(facing));
        matrices.translate(0.14F, -0.55F, -0.8F);
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(-90));

        Random rng = RenderUtil.random(entity.getPos().asLong());

        final float spacing = 0.3F;

        for (int i = 0; i < entity.size(); i++) {
            ItemStack bottle = entity.getStack(i);
            float rot = rng.nextFloat() - 0.5F;
            if (!bottle.isEmpty()) {
                matrices.push();
                matrices.translate((1 - (i / 3)) * spacing, 0, (i % 3) * spacing);
                float rotPoint = 1F;
                matrices.translate(0, rotPoint, rotPoint * -1.2F);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rot * 4));
                matrices.translate(0, -rotPoint, -rotPoint * -1.2F);

                PlacedDrinksModelProvider.INSTANCE.renderDrink(bottle, matrices, vertices, light, overlay);

                matrices.pop();
            }
        }
        matrices.pop();
    }
}

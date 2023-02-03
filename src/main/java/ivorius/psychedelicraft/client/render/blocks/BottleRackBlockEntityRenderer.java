package ivorius.psychedelicraft.client.render.blocks;

import java.util.Random;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.BottleRackBlock;
import ivorius.psychedelicraft.block.entity.BottleRackBlockEntity;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.fluid.FluidContainer;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.RotationAxis;

/**
 * Created by lukas on 16.11.14.
 * Updated by Sollace on 6 Jan 2023
 */
public class BottleRackBlockEntityRenderer implements BlockEntityRenderer<BottleRackBlockEntity> {
    public static final Identifier TEXTURE = Psychedelicraft.id("textures/entity/bottle.png");

    // TODO: (Sollace) Add bottles as a placeable block
    private final ModelPart bottleModel = getBottleModelData().createModel();

    private static TexturedModelData getBottleModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("bottle_0", ModelPartBuilder.create()
                    .uv(0, 3).cuboid(-3.125F, -3.3F, 3.375F, 3, 6, 3, Dilation.NONE)
                    .uv(0, 0).cuboid(-2.125F, -5.55F, 4.375F, 1, 2, 1, Dilation.NONE), ModelTransform.pivot(2, 21, -5));
        return TexturedModelData.of(data, 32, 32);
    }

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
        matrices.translate(-0.5F, -0.5F, -0.5F);
        matrices.translate(-0.7F, 0.8F, 0.21F);
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(90));

        long seed = entity.getPos().asLong();
        Random rng = new Random(seed);

        final float spacing = 0.3F;

        for (int i = 0; i < entity.size(); i++) {
            ItemStack bottle = entity.getStack(i);
            float rot = rng.nextFloat() - 0.5F;
            if (!bottle.isEmpty()) {
                int fluidColor = -1;
                int dyeColor = -1;
                if (bottle.getItem() instanceof FluidContainer container) {
                    SimpleFluid fluid = container.getFluid(bottle);
                    fluidColor = fluid.getTranslucentColor(bottle);
                }
                if (bottle.getItem() instanceof DyeableItem dyeable) {
                    dyeColor = dyeable.getColor(bottle);
                }

                int color = fluidColor == -1 || dyeColor == -1 ? (fluidColor == -1 ? dyeColor : fluidColor) : MathUtils.mixColors(fluidColor, dyeColor, 0.5F);
                if (color != -1) {
                    RenderUtil.setColor(color, false);
                }

                matrices.push();
                matrices.translate((i / 3) * spacing, 0, (i % 3) * spacing);
                float rotPoint = 1F;
                matrices.translate(0, rotPoint, rotPoint * -1.2F);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rot * 4));
                matrices.translate(0, -rotPoint, -rotPoint * -1.2F);

                bottleModel.render(matrices, vertices.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE)), light, overlay, MathUtils.r(color), MathUtils.g(color), MathUtils.b(color), 1);

                matrices.pop();
            }
        }
        matrices.pop();
    }
}

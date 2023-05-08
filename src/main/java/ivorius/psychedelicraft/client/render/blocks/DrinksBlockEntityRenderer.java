/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.block.PlacedDrinksBlock;
import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer.FluidAppearance;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;

public class DrinksBlockEntityRenderer implements BlockEntityRenderer<PlacedDrinksBlock.Data> {
    private static final VoxelShape FILLED_SLOT_RAY_TRACE_SHAPE = Block.createCuboidShape(-2, 0, -2, 2, 4, 2);
    private static final VoxelShape EMPTY_SLOT_RAY_TRACE_SHAPE = Block.createCuboidShape(-2, 0, -2, 2, 0.01, 2);

    private static final Random RNG = Random.create();
    private static final long SEED = 42L;

    public DrinksBlockEntityRenderer(BlockEntityRendererFactory.Context context) { }

    @Override
    public void render(PlacedDrinksBlock.Data entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        entity.forEachDrink((y, drink) -> {
            PlacedDrinksModelProvider.Entry geometry = PlacedDrinksModelProvider.INSTANCE.get(drink.stack().getItem()).orElse(PlacedDrinksModelProvider.Entry.DEFAULT);
            matrices.push();
            matrices.translate(drink.x(), y, drink.z());
            matrices.translate(0.5F, 0, 0.5F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(drink.rotation()));
            matrices.translate(-0.5F, 0, -0.5F);
            renderDrinkModel(drink.stack(), matrices, vertices, light, overlay, 0xFFFFFF, PlacedDrinksModelProvider.getGroundModelId(drink.stack().getItem()));

            FluidContainer container = FluidContainer.of(drink.stack());
            float fillPercentage = container.getFillPercentage(drink.stack());
            if (fillPercentage > 0.01) {
                float origin = geometry.fluidOrigin() / 16F;
                matrices.translate(0, origin, 0);
                matrices.scale(1, fillPercentage, 1);
                matrices.translate(0, -origin, 0);
                int color = FluidAppearance.getItemColor(container.getFluid(drink.stack()), drink.stack());
                renderDrinkModel(drink.stack(), matrices, vertices, light, overlay, color, PlacedDrinksModelProvider.getGroundModelFluidId(drink.stack().getItem()));
            }
            matrices.pop();

            return geometry.height();
        });

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult)client.crosshairTarget;
            if (hit.getBlockPos().equals(entity.getPos())) {
                PlacedDrinksBlock.Data.getHitPos(hit).ifPresent(pos -> {
                    WorldRenderer.drawShapeOutline(matrices, vertices.getBuffer(RenderLayer.getLines()), entity.hasDrink(pos) ? FILLED_SLOT_RAY_TRACE_SHAPE : EMPTY_SLOT_RAY_TRACE_SHAPE, pos.getX() / 16F, 0, pos.getZ() / 16F, 0, 0, 0, 1);
                    RenderSystem.setShaderColor(0, 0, 0, 1);
                });
            }
        }
    }

    private static void renderDrinkModel(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay, int color, ModelIdentifier modelId) {
        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();

        BakedModel model = renderer.getModels().getModelManager().getModel(modelId);

        boolean solid = !(stack.getItem() instanceof BlockItem bi) || !(bi.getBlock() instanceof TransparentBlock) && !(bi.getBlock() instanceof StainedGlassPaneBlock);
        RenderLayer renderLayer = RenderLayers.getItemLayer(stack, solid);

        renderBakedItemModel(model, matrices, vertices.getBuffer(renderLayer), light, overlay, color);
    }

    private static void renderBakedItemModel(BakedModel model, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        for (Direction direction : Direction.values()) {
            RNG.setSeed(SEED);
            renderBakedItemQuads(matrices, vertices, model.getQuads(null, direction, RNG), light, overlay, color);
        }
        RNG.setSeed(SEED);
        renderBakedItemQuads(matrices, vertices, model.getQuads(null, null, RNG), light, overlay, color);
    }

    private static void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, int light, int overlay, int color) {
        MatrixStack.Entry entry = matrices.peek();
        for (BakedQuad bakedQuad : quads) {
            vertices.quad(entry, bakedQuad, MathUtils.r(color), MathUtils.g(color), MathUtils.b(color), light, overlay);
        }
    }

    public static TexturedModelData getBottleModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("bottle_0", ModelPartBuilder.create()
                    .uv(0, 3).cuboid(-3.125F, -3.3F, 3.375F, 3, 6, 3, Dilation.NONE)
                    .uv(0, 0).cuboid(-2.125F, -5.55F, 4.375F, 1, 2, 1, Dilation.NONE), ModelTransform.pivot(2, 21, -5));
        return TexturedModelData.of(data, 32, 32);
    }

}

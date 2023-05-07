/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.block.PlacedDrinksBlock;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
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

    public DrinksBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(PlacedDrinksBlock.Data entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        entity.forEachDrink(drink -> {
            matrices.push();
            matrices.translate(drink.x(), drink.y(), drink.z());
            matrices.translate(0.5F, 0, 0.5F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(drink.rotation()));
            matrices.translate(-0.5F, 0, -0.5F);
            renderDrinkModel(drink.stack(), matrices, vertices, light, overlay);
            matrices.pop();
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

    private static void renderDrinkModel(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();

        BakedModel model = renderer.getModels().getModelManager().getModel(getGroundModelId(stack.getItem()));

        boolean bl22 = !(stack.getItem() instanceof BlockItem bi) || !(bi.getBlock() instanceof TransparentBlock) && !(bi.getBlock() instanceof StainedGlassPaneBlock);
        RenderLayer renderLayer = RenderLayers.getItemLayer(stack, bl22);

        renderBakedItemModel(model, stack, matrices, vertices.getBuffer(renderLayer), light, overlay);
    }

    private static void renderBakedItemModel(BakedModel model, ItemStack stack, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        for (Direction direction : Direction.values()) {
            RNG.setSeed(SEED);
            renderBakedItemQuads(matrices, vertices, model.getQuads(null, direction, RNG), stack, light, overlay);
        }
        RNG.setSeed(SEED);
        renderBakedItemQuads(matrices, vertices, model.getQuads(null, null, RNG), stack, light, overlay);
    }

    private static void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, ItemStack stack, int light, int overlay) {
        MatrixStack.Entry entry = matrices.peek();
        for (BakedQuad bakedQuad : quads) {
            vertices.quad(entry, bakedQuad, 1, 1, 1, light, overlay);
        }
    }

    public static ModelIdentifier getGroundModelId(Item item) {
        return new ModelIdentifier(Registries.ITEM.getId(item).withPath(p -> p + "_on_ground"), "inventory");
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

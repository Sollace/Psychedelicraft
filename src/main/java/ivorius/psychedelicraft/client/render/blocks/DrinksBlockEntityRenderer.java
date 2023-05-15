/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.block.PlacedDrinksBlock;
import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.shape.VoxelShape;

public class DrinksBlockEntityRenderer implements BlockEntityRenderer<PlacedDrinksBlock.Data> {
    private static final VoxelShape FILLED_SLOT_RAY_TRACE_SHAPE = Block.createCuboidShape(-2, 0, -2, 2, 4, 2);
    private static final VoxelShape EMPTY_SLOT_RAY_TRACE_SHAPE = Block.createCuboidShape(-2, 0, -2, 2, 0.01, 2);

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
            PlacedDrinksModelProvider.INSTANCE.renderDrink(drink.stack(), matrices, vertices, light, overlay);
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
}

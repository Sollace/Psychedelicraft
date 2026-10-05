/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import ivorius.psychedelicraft.block.entity.DryingTableBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

/**
 * Renders items on top of the drying table
 *
 * Updated by Sollace on 5 Jan 2023
 */
public class DryingTableBlockEntityRenderer implements BlockEntityRenderer<DryingTableBlockEntity, DryingTableBlockEntityRenderer.State> {
    private final ItemModelManager itemRenderer;

    public DryingTableBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        itemRenderer = context.itemModelManager();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(DryingTableBlockEntity entity, State state, float tickDelta, Vec3d cameraPos, @Nullable CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumblingOverlay);
        long seed = entity.getPos().asLong() + 1;
        Random random = new Random(seed);

        state.items = new ArrayList<>();

        for (int i = 0; i < entity.size(); i++) {
            boolean result = i == 0;
            ItemStack stack = entity.getStack(i);

            if (!stack.isEmpty()) {
                float positionX = result ? 0.5F : (0.35F + random.nextFloat() * 0.3F);
                float positionZ = result ? 0.5F : (0.35F + random.nextFloat() * 0.3F);
                float rotation = random.nextFloat() * 360.0f;

                var item = new State.Item(
                        new ItemRenderState(),
                        positionX, i / 500F, positionZ, rotation,
                        result ? 0.75F : 0.5F
                );
                state.items.add(item);
                itemRenderer.clearAndUpdate(item.item, stack, ItemDisplayContext.FIXED, entity.getWorld(), null, (int)seed);
            }
        }
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        matrices.translate(0, 0.75f, 0);

        state.items.forEach(item -> {
            matrices.push();
            matrices.translate(item.x(), item.y(), item.z());
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(item.rotation()));
            matrices.scale(item.scale(), item.scale(), item.scale());
            matrices.translate(0, 0, -0.2F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(-50));
            item.item().render(matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);

            matrices.pop();
        });

        matrices.pop();
    }

    public static class State extends BlockEntityRenderState {
        public List<Item> items = List.of();

        public record Item(ItemRenderState item, float x, float y, float z, float rotation, float scale) {}
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import ivorius.psychedelicraft.block.PlacedDrinksBlock;
import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider;
import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider.PlacedDrinkRenderState;
import ivorius.psychedelicraft.client.render.RenderUtil;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class DrinksBlockEntityRenderer implements BlockEntityRenderer<PlacedDrinksBlock.Data, DrinksBlockEntityRenderer.State> {
    private static final VoxelShape FILLED_SLOT_RAY_TRACE_SHAPE = Block.createCuboidShape(-2, 0, -2, 2, 4, 2);
    private static final VoxelShape EMPTY_SLOT_RAY_TRACE_SHAPE = Block.createCuboidShape(-2, 0, -2, 2, 0.01, 2);

    private final MinecraftClient client = MinecraftClient.getInstance();

    public DrinksBlockEntityRenderer(BlockEntityRendererFactory.Context context) { }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(PlacedDrinksBlock.Data entity, State state, float tickDelta, Vec3d cameraPos, @Nullable CrumblingOverlayCommand crumbling) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumbling);
        state.drinks = new ArrayList<>();
        entity.forEachDrink((y, drink) -> {
            state.drinks.add(new State.Drink(
                    PlacedDrinksModelProvider.INSTANCE.updateRenderState("ground", drink.stack(), entity.getWorld(), new PlacedDrinksModelProvider.PlacedDrinkRenderState()),
                    new Vec3d(drink.x(), y, drink.z()),
                    drink.rotation()
            ));
            return PlacedDrinksModelProvider.INSTANCE.get("ground", drink.stack().getItem()).orElse(PlacedDrinksModelProvider.Entry.DEFAULT).height();
        });
        state.focusedPos = client.player != null && client.crosshairTarget instanceof BlockHitResult bhit && bhit.getBlockPos().equals(entity.getPos())
                ? PlacedDrinksBlock.Data.getHitPos(bhit).orElse(null)
                : null;
        state.focusedItem = state.focusedPos != null && entity.hasDrink(state.focusedPos);
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        state.drinks.forEach(drink -> {
            matrices.push();
            matrices.translate(drink.position());
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(drink.rotation()), 0.5F, 0, 0.5F);
            drink.item().render(matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        });

        if (state.focusedPos != null) {
            matrices.push();
            matrices.translate(state.focusedPos.getX() / 16F, 0, state.focusedPos.getZ() / 16F);
            RenderUtil.submitOutline(matrices, queue,
                    state.focusedItem ? FILLED_SLOT_RAY_TRACE_SHAPE : EMPTY_SLOT_RAY_TRACE_SHAPE,
                    RenderUtil.OUTLINE_COLOR
            );
            matrices.pop();
        }
    }

    public static class State extends BlockEntityRenderState {
        public List<Drink> drinks = List.of();
        public boolean focusedItem;
        @Nullable
        public BlockPos focusedPos;

        public record Drink(PlacedDrinkRenderState item, Vec3d position, float rotation) {}
    }
}

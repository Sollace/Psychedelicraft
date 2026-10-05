package ivorius.psychedelicraft.client.render.blocks;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.BottleRackBlock;
import ivorius.psychedelicraft.block.entity.BottleRackBlockEntity;
import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider;
import ivorius.psychedelicraft.client.render.RenderUtil;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

/**
 * Created by lukas on 16.11.14.
 * Updated by Sollace on 6 Jan 2023
 */
public class BottleRackBlockEntityRenderer implements BlockEntityRenderer<BottleRackBlockEntity, BottleRackBlockEntityRenderer.State> {
    public BottleRackBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(BottleRackBlockEntity entity, State state, float tickDelta, Vec3d cameraPos, @Nullable CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumblingOverlay);
        Direction direction = entity.getCachedState().get(BottleRackBlock.FACING);
        if (direction.getAxis() == Axis.X) {
            direction = direction.getOpposite();
        }
        state.facing = direction.getPositiveHorizontalDegrees() + 90;
        state.offset = ((BottleRackBlock)state.blockState.getBlock()).getZOffset() / 16D;
        state.bottles = new ArrayList<>();
        for (int i = 0; i < entity.size(); i++) {
            ItemStack stack = entity.getStack(i);
            if (!stack.isEmpty()) {
                Random rng = RenderUtil.random(entity.getPos().asLong());
                state.bottles.add(new State.Bottle(i,
                        PlacedDrinksModelProvider.INSTANCE.updateRenderState("ground", stack, entity.getWorld(), new PlacedDrinksModelProvider.PlacedDrinkRenderState()),
                        rng.nextFloat() - 0.5F
                ));
            }
        }
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.push();
        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.facing));
        matrices.translate(0.14F - state.offset, -0.55F, -0.8F);
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(-90));
        final float spacing = 0.3F;
        final float rotPoint = 1F;
        state.bottles.forEach(bottle -> {
            matrices.push();
            matrices.translate((1 - (bottle.slot() / 3)) * spacing, 0, (bottle.slot() % 3) * spacing);
            matrices.translate(0, rotPoint, rotPoint * -1.2F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(bottle.rotation() * 4));
            matrices.translate(0, -rotPoint, -rotPoint * -1.2F);
            bottle.item().render(matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        });
        matrices.pop();
    }

    public static class State extends BlockEntityRenderState {
        public float facing;
        public double offset;
        public List<Bottle> bottles = List.of();

        public record Bottle(int slot, PlacedDrinksModelProvider.PlacedDrinkRenderState item, float rotation) {}
    }
}

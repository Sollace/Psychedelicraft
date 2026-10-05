package ivorius.psychedelicraft.client.render.blocks;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.Processable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

public abstract class LabelledBlockEntityRenderer<T extends BlockEntity, S extends LabelledBlockEntityRenderer.State> implements BlockEntityRenderer<T, S> {
    protected final TextRenderer textRenderer;

    static boolean shouldRenderLabel(BlockEntity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        return entity.getPos() != null
                && entity.getWorld() != null
                && client.gameRenderer.getCamera().getBlockPos().getSquaredDistance(entity.getPos()) < 4096
                && client.crosshairTarget instanceof BlockHitResult hit
                && (hit.getBlockPos().equals(entity.getPos()) || (
                        entity instanceof MashTubBlockEntity
                        && hit.getBlockPos().getY() == entity.getPos().getY()
                        && Math.abs(hit.getBlockPos().getX() - entity.getPos().getX()) < 2
                        && Math.abs(hit.getBlockPos().getZ() - entity.getPos().getZ()) < 2
                ));
    }

    public LabelledBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this(context.textRenderer());
    }

    public LabelledBlockEntityRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    static Text getFillPercentage(Processable.Context entity, int volume) {
        int totalFluids = entity.getTotalFluidVolume();
        int percentage = (int)((totalFluids / (float)volume) * 100);
        if (percentage == 0 && totalFluids > 0) {
            return Text.literal(FluidVolumes.format(totalFluids));
        }
        return switch (percentage) {
            case 100 -> Text.literal("Full");
            case 0 -> Text.literal("Empty");
            default -> Text.literal(percentage + "%");
        };
    }

    @Override
    public void updateRenderState(T entity, S state, float tickDelta, Vec3d cameraPos, @Nullable CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumblingOverlay);
        state.hasLabel = shouldRenderLabel(entity);
        state.labelDistanceFromCenter = getLabelDistanceFromCenter(entity);
        state.labelScale = getLabelScale(entity, tickDelta);
    }

    @Override
    public void render(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.hasLabel) {
            matrices.push();
            matrices.translate(0.5, 0, 0.5);
            matrices.multiply(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
            matrices.translate(0, 0, state.labelDistanceFromCenter);
            matrices.scale(state.labelScale, -state.labelScale, state.labelScale);
            renderLabels(state, matrices, queue);
            matrices.pop();
        }
    }

    protected double getLabelDistanceFromCenter(T entity) {
        return 0.5;
    }

    protected float getLabelScale(T entity, float tickDelta) {
        return 0.005F;
    }

    protected abstract void renderLabels(S state, MatrixStack matrices, OrderedRenderCommandQueue queue);

    public static class State extends BlockEntityRenderState {
        public boolean hasLabel;
        public double labelDistanceFromCenter;
        public float labelScale;
    }
}

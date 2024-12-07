package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.fluid.Processable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;

public abstract class LabelledBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    protected final TextRenderer textRenderer;

    static boolean shouldRenderLabel(BlockEntity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        return entity.getPos() != null
                && entity.getWorld() != null
                && client.getEntityRenderDispatcher().camera.getBlockPos().getSquaredDistance(entity.getPos()) < 4096
                && client.crosshairTarget instanceof BlockHitResult hit
                && (hit.getBlockPos().equals(entity.getPos()) || (
                        entity instanceof MashTubBlockEntity
                        && hit.getBlockPos().getY() == entity.getPos().getY()
                        && Math.abs(hit.getBlockPos().getX() - entity.getPos().getX()) < 2
                        && Math.abs(hit.getBlockPos().getZ() - entity.getPos().getZ()) < 2
                ));
    }

    public LabelledBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        textRenderer = context.getTextRenderer();
    }

    static Text getFillPercentage(Processable.Context entity, int volume) {
        int percentage = (int)((entity.getTotalFluidVolume() / (float)volume) * 100);
        return switch (percentage) {
            case 100 -> Text.literal("Full");
            case 0 -> Text.literal("Empty");
            default -> Text.literal(percentage + "%");
        };
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        if (shouldRenderLabel(entity)) {
            matrices.push();
            matrices.translate(0.5, 0, 0.5);
            matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
            matrices.translate(0, 0, getLabelDistanceFromCenter(entity));
            float scale = getLabelScale(entity, tickDelta);
            matrices.scale(scale, -scale, scale);
            renderLabels(entity, tickDelta, matrices, vertices, light, overlay);
            matrices.pop();
        }
    }

    protected double getLabelDistanceFromCenter(T entity) {
        return 0.5;
    }

    protected float getLabelScale(T entity, float tickDelta) {
        return 0.005F;
    }

    protected abstract void renderLabels(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay);
}

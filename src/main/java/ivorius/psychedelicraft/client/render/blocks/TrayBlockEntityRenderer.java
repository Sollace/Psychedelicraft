/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.TrayBlockEntity;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class TrayBlockEntityRenderer extends LabelledBlockEntityRenderer<TrayBlockEntity> {
    private static final Identifier FLUID_TEXTURE = Psychedelicraft.id("textures/entity/tray/fluid.png");

    private TrayContentsModel contentsModel = new TrayContentsModel(TrayContentsModel.getTexturedModelData().createModel());

    public TrayBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(TrayBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay, Vec3d cameraPos) {
        if (entity.getLevel() > 0 || entity.isHardened()) {
            matrices.push();
            matrices.translate(0.5, 1 / 16D, 0.5);
            contentsModel = new TrayContentsModel(TrayContentsModel.getTexturedModelData().createModel());
            contentsModel.setAngles(entity, tickDelta);
            contentsModel.render(matrices, vertices.getBuffer(
                    entity.isHardened() ? RenderLayer.getEntitySolid(FLUID_TEXTURE) : RenderLayer.getEntityTranslucent(FLUID_TEXTURE)
            ), light, overlay);

            matrices.pop();
        }
        super.render(entity, tickDelta, matrices, vertices, light, overlay, cameraPos);
    }

    @Override
    protected void renderLabels(TrayBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        if (entity.getLevel() > 0) {
            int percentage = (int)(100 * entity.getLevel() / 50F);

            Text fillText = switch (percentage) {
                case 100 -> Text.literal("Full");
                case 0 -> Text.literal("Empty");
                default -> Text.literal(percentage + "%");
            };

            textRenderer.draw(fillText, -(textRenderer.getWidth(fillText) - 5) / 2F, -textRenderer.fontHeight - 2, Colors.WHITE, true, matrices.peek().getPositionMatrix(), vertices, TextLayerType.NORMAL, 0, light);
        }

        if (entity.getCraftingResult().isPresent()) {

            Text text = entity.getCraftingResult().get().getName();

            textRenderer.draw(text, -(textRenderer.getWidth(text) - 5) / 2F, 0, Colors.BLUE, true, matrices.peek().getPositionMatrix(), vertices, TextLayerType.NORMAL, 0, light);

        }
    }
}

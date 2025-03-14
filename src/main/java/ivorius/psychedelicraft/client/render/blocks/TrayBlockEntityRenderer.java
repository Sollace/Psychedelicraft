/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.TrayBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TrayBlockEntityRenderer extends LabelledBlockEntityRenderer<TrayBlockEntity> {
    private static final Identifier FLUID_TEXTURE = Psychedelicraft.id("textures/entity/tray/fluid.png");

    private final ItemRenderer itemRenderer;

    private TrayContentsModel contentsModel = new TrayContentsModel(TrayContentsModel.getTexturedModelData().createModel());

    public TrayBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(TrayBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
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
        super.render(entity, tickDelta, matrices, vertices, light, overlay);
    }

    @Override
    protected void renderLabels(TrayBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {

    }
}

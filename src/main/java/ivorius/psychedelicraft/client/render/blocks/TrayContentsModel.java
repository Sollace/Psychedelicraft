package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.block.entity.TrayBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class TrayContentsModel extends Model {
    private final ModelPart tree;

    public TrayContentsModel(ModelPart tree) {
        super(RenderLayer::getEntityTranslucent);
        this.tree = tree;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("contents", ModelPartBuilder.create().uv(0, 0).cuboid(0, 0, 0, 10, 1, 16), ModelTransform.pivot(-5, 0, -8));

        return TexturedModelData.of(data, 32, 32);
    }

    public void setAngles(TrayBlockEntity entity, float tickDelta) {
        tree.yScale = 0.1F + entity.getFluidRatio();
        if (entity.getCachedState().get(Properties.HORIZONTAL_AXIS) == Direction.Axis.X) {
            tree.yaw = MathHelper.HALF_PI;
        } else {
            tree.yaw = 0;
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        tree.render(matrices, vertices, light, overlay, color);
    }
}

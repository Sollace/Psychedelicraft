package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.block.entity.TrayBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class TrayContentsModel extends Model {
    public TrayContentsModel(ModelPart tree) {
        super(tree, RenderLayer::getEntityTranslucent);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("contents", ModelPartBuilder.create().uv(0, 0).cuboid(0, 0, 0, 10, 1, 16), ModelTransform.pivot(-5, 0, -8));

        return TexturedModelData.of(data, 32, 32);
    }

    public void setAngles(TrayBlockEntity entity, float tickDelta) {
        root.yScale = 0.1F + entity.getLevel() / 60F;
        if (entity.getCachedState().get(Properties.HORIZONTAL_AXIS) == Direction.Axis.X) {
            root.yaw = MathHelper.HALF_PI;
        } else {
            root.yaw = 0;
        }
    }
}

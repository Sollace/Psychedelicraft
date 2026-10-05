package ivorius.psychedelicraft.client.render.blocks;

import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.model.*;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class TrayContentsModel extends Model<TrayBlockEntityRenderer.State> {
    public TrayContentsModel(ModelPart tree) {
        super(tree, RenderLayers::entityTranslucent);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("contents", ModelPartBuilder.create().uv(0, 0).cuboid(0, 0, 0, 10, 1, 16), ModelTransform.origin(-5, 0, -8));

        return TexturedModelData.of(data, 32, 32);
    }

    @Override
    public void setAngles(TrayBlockEntityRenderer.State state) {
        root.yScale = 0.1F + state.level / 60F;
        root.yaw = state.axis == Direction.Axis.X ? MathHelper.HALF_PI : 0;
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.blocks;

import ivorius.psychedelicraft.block.entity.BarrelBlockEntity;
import ivorius.psychedelicraft.fluids.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

import org.joml.Matrix4f;

public class BarrelBlockEntityRenderer implements BlockEntityRenderer<BarrelBlockEntity> {
    private final BarrelModel model;

    public BarrelBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        model = new BarrelModel(BarrelModel.getTexturedModelData().createModel());
    }

    @Override
    public void render(BarrelBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getCachedState().get(HorizontalFacingBlock.FACING).asRotation() + 180));

        matrices.push();
        matrices.translate(0, 1, 0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        model.setRotationAngles(entity);
        model.render(matrices, vertices.getBuffer(model.getLayer(getBarrelTexture(entity))), light, overlay, 1, 1, 1, 1);
        matrices.pop();

        Resovoir tank = entity.getTank(Direction.UP);

        SimpleFluid fluid = tank.getFluidType();
        if (!fluid.isEmpty()) {
            float barrelZ = -0.45F;
            float iconSize = 0.5F;

            VertexConsumer buffer = vertices.getBuffer(model.getLayer(fluid.getSymbol()));
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            buffer.vertex(positionMatrix, -iconSize, -iconSize, barrelZ).texture(1, 1).normal(0, 0, 1).next();
            buffer.vertex(positionMatrix, -iconSize,  iconSize, barrelZ).texture(1, 0).normal(0, 0, 1).next();
            buffer.vertex(positionMatrix,  iconSize,  iconSize, barrelZ).texture(0, 0).normal(0, 0, 1).next();
            buffer.vertex(positionMatrix,  iconSize, -iconSize, barrelZ).texture(0, 1).normal(0, 0, 1).next();
        }

        matrices.pop();
    }

    public static Identifier getBarrelTexture(BarrelBlockEntity barrel) {
        BlockState state = barrel.getCachedState();
        Identifier id = Registries.BLOCK.getId(state.getBlock());
        return new Identifier(id.getNamespace(), "textures/entity/barrel/" + id.getPath() + ".png");
    }

}

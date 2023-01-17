/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.RiftJarBlockEntity;
import ivorius.psychedelicraft.client.render.*;
import ivorius.psychedelicraft.client.render.bezier.*;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.*;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Random;

public class RiftJarBlockEntityRenderer implements BlockEntityRenderer<RiftJarBlockEntity> {
    private static final IvBezierPath3DRendererText bezierPath3DRendererText = new IvBezierPath3DRendererText().setFont(new Identifier("alt"));
    public static final Identifier TEXTURE = Psychedelicraft.id("textures/entity/rift_jar/rift_jar.png");
    public static final Identifier CRACKED_TEXTURE = Psychedelicraft.id("textures/entity/rift_jar/rift_jar_cracked.png");

    public static final IvBezierPath3D SPHERE_BEZIER_PATH = IvBezierPath3DCreator.createSpiraledSphere(3.0, 8.0, 0.2);
    public static final IvBezierPath3D OUTGOING_PATH = IvBezierPath3DCreator.createSpiraledBezierPath(0.06, 6.0, 6.0, 1.0, 0.2, 0.0, false);

    private final RiftJarModel model = new RiftJarModel(RiftJarModel.getTexturedModelData().createModel());

    private static final RiftJarBlockEntity ITEM_ENTITY = PSBlockEntities.RIFT_JAR.instantiate(BlockPos.ORIGIN, PSBlocks.RIFT_JAR.getDefaultState());

    public static void renderStack(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        ITEM_ENTITY.currentRiftFraction = PSItems.RIFT_JAR.getRiftFraction(stack);
        ITEM_ENTITY.ticksAliveVisual = (int)((System.currentTimeMillis() % 500) / 100);
        MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(ITEM_ENTITY, matrices, vertices, light, overlay);
    }

    public RiftJarBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(RiftJarBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        float ticks = entity.ticksAliveVisual + tickDelta;

        matrices.push();
        matrices.translate(0.5F, 0.5f, 0.5F);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getCachedState().get(HorizontalFacingBlock.FACING).asRotation() + 180));

        model.setAngles(entity, tickDelta);
        matrices.translate(0, 1, 0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

        model.render(matrices, vertices.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE)), light, overlay, 1, 1, 1, 1);

        float crackedVisibility = Math.min((entity.currentRiftFraction - 0.5F) * 2, 1);

        if (crackedVisibility > 0) {
            model.render(matrices, vertices.getBuffer(model.getLayer(CRACKED_TEXTURE)), light, overlay, 1, 1, 1, crackedVisibility);
        }

        if (entity.currentRiftFraction > 0) {
            matrices.push();
            matrices.translate(0, 1.5F, 0);
            matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(180));
            matrices.scale(0.9F, 0.9F, 0.9F);
            ZeroScreen.render(ticks, (layer, u, v) -> {
                model.renderInterior(matrices, vertices.getBuffer(layer), 0, 0,
                        1, 1, 1, Math.min(entity.currentRiftFraction * 2, 1));
            });
            matrices.pop();
        }

        matrices.pop();
        matrices.pop();

        matrices.push();
        matrices.translate(0.5F, 0.5f, 0.5F);
        RenderSystem.disableCull();

        Vec3d jarPosition = entity.getPos().toCenterPos();

        for (RiftJarBlockEntity.JarRiftConnection connection : entity.getConnections()) {
            if (connection.bezierPath3D == null) {
                connection.bezierPath3D = IvBezierPath3DCreator.createSpiraledBezierPath(0.1, 0.5, 8.0, new double[] {
                        connection.position.x - jarPosition.x,
                        connection.position.y - (jarPosition.y + 0.1F),
                        connection.position.z - jarPosition.z
                }, 0.2, 0.0, false);
            }

            bezierPath3DRendererText.setText("This is a small spiral.");
            bezierPath3DRendererText.setSpreadToFill(true);
            bezierPath3DRendererText.setShift(ticks * -0.002);
            bezierPath3DRendererText.setInwards(false);
            bezierPath3DRendererText.setCapBottom(0);
            bezierPath3DRendererText.setCapTop(connection.fractionUp);

            bezierPath3DRendererText.render(connection.bezierPath3D);

            if (connection.fractionUp > 0) {
                matrices.push();

                matrices.translate(connection.position.x - jarPosition.x, connection.position.y - (jarPosition.y + 0.1), connection.position.z - jarPosition.z);

                bezierPath3DRendererText.setText(cheeseString("This is a small circle.", 1.0f - connection.fractionUp, new Random(42)));
                bezierPath3DRendererText.setSpreadToFill(true);
                bezierPath3DRendererText.setShift(ticks * -0.002F);
                bezierPath3DRendererText.setInwards(false);
                bezierPath3DRendererText.setCapBottom(0);
                bezierPath3DRendererText.setCapTop(1);

                bezierPath3DRendererText.render(SPHERE_BEZIER_PATH);

                matrices.pop();
            }
        }

        float outgoingStrength = entity.fractionHandleUp * entity.fractionOpen;
        if (outgoingStrength > 0) {
            bezierPath3DRendererText.setText("This is a small spiral.");
            bezierPath3DRendererText.setSpreadToFill(true);
            bezierPath3DRendererText.setShift(ticks * 0.002);
            bezierPath3DRendererText.setInwards(false);
            bezierPath3DRendererText.setCapBottom(0);
            bezierPath3DRendererText.setCapTop(outgoingStrength);

            bezierPath3DRendererText.render(OUTGOING_PATH);
        }

        RenderSystem.enableCull();
        matrices.pop();
    }

    public static String cheeseString(String string, float effect, Random rand) {
        if (effect <= 0) {
            return string;
        }

        StringBuilder builder = new StringBuilder(string.length());

        for (int i = 0; i < string.length(); i++) {
            if (rand.nextFloat() <= effect) {
                builder.append(' ');
            } else {
                builder.append(string.charAt(i));
            }
        }

        return builder.toString();
    }
}

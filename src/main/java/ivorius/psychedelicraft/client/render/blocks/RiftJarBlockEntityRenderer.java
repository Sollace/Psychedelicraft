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
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Random;

import org.joml.Vector3d;

public class RiftJarBlockEntityRenderer implements BlockEntityRenderer<RiftJarBlockEntity> {
    public static final Identifier TEXTURE = Psychedelicraft.id("textures/entity/rift_jar/rift_jar.png");
    public static final Identifier CRACKED_TEXTURE = Psychedelicraft.id("textures/entity/rift_jar/rift_jar_cracked.png");
    private static final Identifier FONT = new Identifier("alt");

    private static final Bezier SPHERE_BEZIER_PATH = Bezier.sphere(3, 8, 0.2);
    private static final Bezier OUTGOING_PATH = Bezier.spiral(0.06, 6, 6, 1, 0.2, 0);

    private static final BezierLabelRenderer.Style LABEL_STYLE = new BezierLabelRenderer.Style().spread(true);
    private static final Text SMALL_SPIRAL_TEXT = Text.literal("This is a small spiral.").styled(s -> s.withFont(FONT));

    private final RiftJarModel model = new RiftJarModel(RiftJarModel.getTexturedModelData().createModel());

    private static final RiftJarBlockEntity ITEM_ENTITY = PSBlockEntities.RIFT_JAR.instantiate(BlockPos.ORIGIN, PSBlocks.RIFT_JAR.getDefaultState());

    public static void renderStack(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
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
                model.renderInterior(matrices, vertices.getBuffer(layer), 0, 0, 1, 1, 1, Math.min(entity.currentRiftFraction * 2, 1));
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
            Vector3d connectionPoint = new Vector3d(
                    connection.position.x - jarPosition.x,
                    connection.position.y - (jarPosition.y + 0.1F),
                    connection.position.z - jarPosition.z
            );
            if (connection.bezier == null) {
                connection.bezier = Bezier.spiral(0.1, 0.5, 8, connectionPoint, 0.2, 0);
            }

            BezierLabelRenderer.INSTANCE.render(matrices, connection.bezier, LABEL_STYLE.shift(ticks * -0.002F).topCap(connection.fractionUp), SMALL_SPIRAL_TEXT);

            if (connection.fractionUp > 0) {
                matrices.push();
                matrices.translate(
                        connectionPoint.x,
                        connectionPoint.y,
                        connectionPoint.z
                );
                BezierLabelRenderer.INSTANCE.render(matrices,
                        SPHERE_BEZIER_PATH,
                        LABEL_STYLE.shift(ticks * -0.002F).topCap(1),
                        Text.literal(cheeseString("This is a small circle.", 1 - connection.fractionUp, new Random(42))).styled(s -> s.withFont(FONT)));

                matrices.pop();
            }
        }

        float outgoingStrength = entity.fractionHandleUp * entity.fractionOpen;
        if (outgoingStrength > 0) {
            BezierLabelRenderer.INSTANCE.render(matrices, OUTGOING_PATH, LABEL_STYLE.shift(ticks * -0.002F).topCap(outgoingStrength), SMALL_SPIRAL_TEXT);
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

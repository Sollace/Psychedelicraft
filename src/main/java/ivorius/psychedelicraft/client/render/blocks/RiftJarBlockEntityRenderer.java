/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.RiftJarBlockEntity;
import ivorius.psychedelicraft.client.render.*;
import ivorius.psychedelicraft.client.render.bezier.*;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.*;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3fc;

public class RiftJarBlockEntityRenderer implements BlockEntityRenderer<RiftJarBlockEntity, RiftJarBlockEntityRenderer.State> {
    public static final Identifier TEXTURE = Psychedelicraft.id("textures/entity/rift_jar/rift_jar.png");
    public static final Identifier CRACKED_TEXTURE = Psychedelicraft.id("textures/entity/rift_jar/rift_jar_cracked.png");
    private static final StyleSpriteSource FONT = new StyleSpriteSource.Font(Identifier.ofVanilla("alt"));

    private static final Bezier SPHERE_BEZIER_PATH = Bezier.sphere(3, 8, 0.2);
    private static final Bezier OUTGOING_PATH = Bezier.spiral(0.06, 6, 6, 1, 0.2, 0);

    private static final BezierLabelRenderer.Style LABEL_STYLE = new BezierLabelRenderer.Style().spread(true);
    private static final Text SMALL_SPIRAL_TEXT = Text.literal("This is a small spiral.").styled(s -> s.withFont(FONT));

    private final RiftJarModel model = new RiftJarModel(RiftJarModel.exterior().createModel());
    private final ModelPart interior = RiftJarModel.interior().createModel();

    public RiftJarBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    public RiftJarBlockEntityRenderer() {

    }


    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(RiftJarBlockEntity entity, State state, float tickDelta, Vec3d cameraPos, @Nullable CrumblingOverlayCommand crumbling) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumbling);
        state.crackedVisibility = entity.jarBroken ? 1 : Math.min((entity.currentRiftFraction - 0.5F) * 2, 1);
        state.age = entity.ticksAliveVisual + tickDelta;
        state.fillPercentage = entity.currentRiftFraction;
        state.facing = state.blockState.get(HorizontalFacingBlock.FACING);
        state.openAmount = entity.fractionOpen;
        state.knotPosition = entity.fractionHandleUp;
        state.outflowStrength = entity.fractionHandleUp * entity.fractionOpen;
        state.connection = entity.getConnections();
    }

    public void collectVertices(Consumer<Vector3fc> collector) {
        MatrixStack matrices = new MatrixStack();
        matrices.translate(0.5F, 0.0F, 0.5F);
        matrices.scale(0.6666667F, -0.6666667F, -0.6666667F);
        model.getRootPart().collectVertices(matrices, collector);
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        renderJarBody(state, matrices, queue);
        renderConnections(state, matrices, queue);
    }

    public void renderJarBody(State state, MatrixStack matrices, OrderedRenderCommandQueue queue) {
        matrices.push();
        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90 - state.facing.getHorizontalQuarterTurns()));
        matrices.translate(0, 1.001F, 0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

        queue.submitModel(model, state, matrices, RenderLayers.entityTranslucent(TEXTURE), state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0, state.crumblingOverlay);

        if (state.crackedVisibility > 0) {
            queue.submitModel(model, state, matrices, RenderLayers.entityTranslucent(CRACKED_TEXTURE), state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, MathUtils.withAlpha(Colors.WHITE, state.crackedVisibility), null, 0, state.crumblingOverlay);
        }

        if (state.fillPercentage > 0) {
            matrices.push();
            matrices.translate(0, 1.5F, 0);
            matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(180));
            matrices.scale(0.9F, 1, 0.9F);
            queue.submitModelPart(interior, matrices, ZeroScreen.layer(state.age), 0, 0, null, MathUtils.withAlpha(Colors.WHITE, Math.min(state.fillPercentage * 2, 1)), null);
            matrices.pop();
        }

        matrices.pop();
    }

    public void renderConnections(State state, MatrixStack matrices, OrderedRenderCommandQueue queue) {
        matrices.push();
        matrices.translate(0.5F, 0.5f, 0.5F);

        Vec3d jarPosition = state.pos.toCenterPos();

        for (RiftJarBlockEntity.JarRiftConnection connection : state.connection) {
            Vector3d connectionPoint = new Vector3d(
                    connection.position.x - jarPosition.x,
                    connection.position.y - (jarPosition.y + 0.1F),
                    connection.position.z - jarPosition.z
            );
            if (connection.bezier == null) {
                connection.bezier = Bezier.spiral(0.1, 0.5, 8, connectionPoint, 0.2, 0);
            }

            BezierLabelRenderer.INSTANCE.render(matrices, queue, state.lightmapCoordinates, connection.bezier, LABEL_STYLE.shift(state.age * -0.002F).topCap(connection.fractionUp), SMALL_SPIRAL_TEXT);

            if (connection.fractionUp > 0) {
                matrices.push();
                matrices.translate(
                        connectionPoint.x,
                        connectionPoint.y,
                        connectionPoint.z
                );
                BezierLabelRenderer.INSTANCE.render(matrices, queue, state.lightmapCoordinates,
                        SPHERE_BEZIER_PATH,
                        LABEL_STYLE.shift(state.age * -0.002F).topCap(1),
                        Text.literal(cheeseString("This is a small circle.", 1 - connection.fractionUp, new Random(42))).styled(s -> s.withFont(FONT)));

                matrices.pop();
            }
        }

        if (state.outflowStrength > 0) {
            BezierLabelRenderer.INSTANCE.render(matrices, queue, state.lightmapCoordinates, OUTGOING_PATH, LABEL_STYLE.shift(state.age * -0.002F).topCap(state.outflowStrength), SMALL_SPIRAL_TEXT);
        }

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

    public static class State extends BlockEntityRenderState {
        public float age;
        public float fillPercentage;
        public float crackedVisibility;
        public float openAmount;
        public float knotPosition;

        public float outflowStrength;

        @Nullable
        public Direction facing;

        @Deprecated
        public Collection<RiftJarBlockEntity.JarRiftConnection> connection = List.of();
    }
}

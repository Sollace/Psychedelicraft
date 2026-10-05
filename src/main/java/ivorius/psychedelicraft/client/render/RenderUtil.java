/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.ModelCommandRenderer.CrumblingOverlayCommand;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 17 Jan 2023
 */
public class RenderUtil {
    private static final Vector4f POSITION_VECTOR = new Vector4f();
    private static final Vector3f NORMAL_VECTOR = new Vector3f();
    public static final int SCREEN_Z_OFFSET = -90;
    private static final Random RNG = Random.create(0);
    public static final int OUTLINE_COLOR = ColorHelper.fromFloats(0.4F, 0, 0, 0);
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    public static Random random(long seed) {
        RNG.setSeed(seed);
        return RNG;
    }

    public static void submitOutline(MatrixStack matrices, OrderedRenderCommandQueue queue, VoxelShape shape, int color) {
        var mat = new MatrixStack();
        queue.submitCustom(matrices, RenderLayers.lines(), (transform, buffer) -> {
            mat.peek().copy(transform);
            VertexRendering.drawOutline(
                    mat,
                    buffer,
                    shape,
                    0, 0, 0,
                    color, CLIENT.getWindow().getMinimumLineWidth());
        });
    }

    public static OrderedRenderCommandQueue.Custom customWithCrumbling(OrderedRenderCommandQueue.Custom custom, @Nullable CrumblingOverlayCommand crumbling) {
        return crumbling == null ? custom : (transforms, buffer) -> custom.render(transforms, applyCrumbling(buffer, crumbling));
    }

    public static VertexConsumer applyCrumbling(VertexConsumer buffer, @Nullable CrumblingOverlayCommand crumbling) {
        var crumblingOverlayVertexConsumers = MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers();
        return crumbling == null ? buffer : VertexConsumers.union(buffer, new OverlayVertexConsumer(
            crumblingOverlayVertexConsumers.getBuffer(ModelBaker.BLOCK_DESTRUCTION_RENDER_LAYERS.get(crumbling.progress())),
            crumbling.cameraMatricesEntry(),
            1
        ));
    }

    public static void vertex(VertexConsumer buffer, MatrixStack.Entry transform, float x, float y, float z, float u, float v, int light, int overlay) {
        vertex(buffer, transform, x, y, z, Colors.WHITE, u, v, overlay, light);
    }

    public static void vertex(VertexConsumer buffer, MatrixStack.Entry transform, float x, float y, float z, int color, float u, float v, int overlay, int light) {
        transform.getPositionMatrix().transform(POSITION_VECTOR.set(x, y, z, 1));
        transform.getNormalMatrix().transform(NORMAL_VECTOR.set(0, -1, 0));
        buffer.vertex(POSITION_VECTOR.x, POSITION_VECTOR.y, POSITION_VECTOR.z, color, u, v, overlay, light, NORMAL_VECTOR.x, NORMAL_VECTOR.y, NORMAL_VECTOR.z);
    }

    public static void drawQuad(DrawContext context, Identifier texture, float x0, float y0, float x1, float y1, int color) {
        GuiQuads.builder().quad(x0, y0, x1, y1, 0, 0, 1, 1, color).draw(context, texture);
    }

    public static void drawOverlay(DrawContext context, Identifier texture, float alpha,
            int width, int height,
            float u0, float v0,
            float u1, float v1, int offset) {
        int color = ColorHelper.withAlpha(ColorHelper.channelFromFloat(alpha), Colors.WHITE);
        drawOverlay(context, texture, color, width, height, u0, v0, u1, v1, offset);
    }

    public static void drawOverlay(DrawContext context, Identifier texture, int color,
            int width, int height,
            float u0, float v0,
            float u1, float v1, int offset) {
        if (ColorHelper.getAlpha(color) <= 0) {
            return;
        }
        GuiQuads.builder().quad(-offset, -offset, width + offset, height + offset, u0, v0, u1, v1, color).draw(context, texture);
    }

    public static void drawRepeatingSprite(DrawContext context, Sprite sprite, int x, int y, int width, int height, int color) {
        final int tileSize = 16;

        int tilesX = width / tileSize;
        int tilesY = height / tileSize;

        int remainedWidth = width % tileSize;
        int remainedHeight = height % tileSize;

        for (int tileX = 0; tileX <= tilesX; tileX ++) {
            for (int tileY = 0; tileY <= tilesY; tileY ++) {
                int w = tileX == tilesX ? remainedWidth : tileSize;
                int h = tileY == tilesY ? remainedHeight : tileSize;
                if (h > 0 && w > 0) {
                    context.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, sprite, x + tileX * tileSize, y + tileY * tileSize, w, h, color);
                }
            }
        }
    }
}

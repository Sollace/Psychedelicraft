/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import java.util.Random;

import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 17 Jan 2023
 */
public class RenderUtil {
    private static final Vector4f POSITION_VECTOR = new Vector4f();
    private static final Vector3f NORMAL_VECTOR = new Vector3f();
    public static final int SCREEN_Z_OFFSET = -90;
    private static final Random RNG = new Random(0L);

    public static Random random(long seed) {
        RNG.setSeed(seed);
        return RNG;
    }

    public static void setColor(int color, boolean hasAlpha) {
        RenderSystem.setShaderColor(
                MathUtils.r(color),
                MathUtils.g(color),
                MathUtils.b(color),
                hasAlpha ? MathUtils.a(color) : 1
        );
    }

    private static VertexConsumer fastVertex(VertexConsumer buffer, MatrixStack.Entry entry, float x, float y, float z) {
        entry.getPositionMatrix().transform(POSITION_VECTOR.set(x, y, z, 1));
        return buffer.vertex(POSITION_VECTOR.x, POSITION_VECTOR.y, POSITION_VECTOR.z);
    }

    public static void vertex(VertexConsumer buffer, MatrixStack matrices, float x, float y, float z, float u, float v, int light, int overlay) {
        matrices.peek().getPositionMatrix().transform(POSITION_VECTOR.set(x, y, z, 1));
        matrices.peek().getNormalMatrix().transform(NORMAL_VECTOR.set(0, 1, 0));
        buffer.vertex(POSITION_VECTOR.x, POSITION_VECTOR.y, POSITION_VECTOR.z, Colors.WHITE, u, v, light, overlay, NORMAL_VECTOR.x, NORMAL_VECTOR.y, NORMAL_VECTOR.z);
    }

    public static void drawQuad(DrawContext context, Identifier texture, float x0, float y0, float x1, float y1) {
        drawQuad(context, texture, x0, y0, x1, y1, 0);
    }

    public static void drawQuad(DrawContext context, Identifier texture, float x0, float y0, float x1, float y1, float z) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        MatrixStack.Entry entry = context.getMatrices().peek();
        fastVertex(buffer, entry, x0, y1, z).texture(0, 1);
        fastVertex(buffer, entry, x1, y1, z).texture(1, 1);
        fastVertex(buffer, entry, x1, y0, z).texture(1, 0);
        fastVertex(buffer, entry, x0, y0, z).texture(0, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    public static void drawOverlay(DrawContext context, Identifier texture,float alpha,
            int width, int height,
            float u0, float v0,
            float u1, float v1, int offset) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        MatrixStack.Entry entry = context.getMatrices().peek();
        fastVertex(buffer, entry, -offset, height + offset, SCREEN_Z_OFFSET).texture(u0, v1);
        fastVertex(buffer, entry, width + offset, height + offset, SCREEN_Z_OFFSET).texture(u1, v1);
        fastVertex(buffer, entry, width + offset, -offset, SCREEN_Z_OFFSET).texture(u1, v0);
        fastVertex(buffer, entry, -offset, -offset, SCREEN_Z_OFFSET).texture(u0, v0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    public static void drawBuffer(Framebuffer frame, float r, float g, float b, float a) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, frame.getColorAttachment());
        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(0, 0, 0).texture(0, 1)
              .vertex(0, frame.viewportHeight, 0).texture(0, 0)
              .vertex(frame.viewportWidth, frame.viewportHeight, 0).texture(1, 0)
              .vertex(frame.viewportWidth, 0, 0).texture(1, 1);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    public static void drawRepeatingSprite(DrawContext context, Sprite sprite, int x, int y, int width, int height, float r, float g, float b, float a) {
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
                    context.drawSprite(x + tileX * tileSize, y + tileY * tileSize, 0, w, h, sprite, r, g, b, a);
                }
            }
        }
    }
}

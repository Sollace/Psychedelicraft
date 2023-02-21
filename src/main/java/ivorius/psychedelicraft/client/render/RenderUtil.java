/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import java.util.Random;

import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

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

    public static VertexConsumer fastVertex(VertexConsumer buffer, MatrixStack matrices, float x, float y, float z) {
        matrices.peek().getPositionMatrix().transform(POSITION_VECTOR.set(x, y, z, 1));
        return buffer.vertex(POSITION_VECTOR.x, POSITION_VECTOR.y, POSITION_VECTOR.z);
    }

    public static void vertex(VertexConsumer buffer, MatrixStack matrices, float x, float y, float z, float u, float v, int light, int overlay) {
        matrices.peek().getPositionMatrix().transform(POSITION_VECTOR.set(x, y, z, 1));
        matrices.peek().getNormalMatrix().transform(NORMAL_VECTOR.set(0, 1, 0));
        buffer.vertex(POSITION_VECTOR.x, POSITION_VECTOR.y, POSITION_VECTOR.z, 1, 1, 1, 1, u, v, light, overlay, NORMAL_VECTOR.x, NORMAL_VECTOR.y, NORMAL_VECTOR.z);
    }

    public static void drawQuad(MatrixStack matrices, float x0, float y0, float x1, float y1) {
        drawQuad(matrices, x0, y0, x1, y1, 0);
    }

    public static void drawQuad(MatrixStack matrices, float x0, float y0, float x1, float y1, float z) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        fastVertex(buffer, matrices, x0, y1, z).texture(0, 1).next();
        fastVertex(buffer, matrices, x1, y1, z).texture(1, 1).next();
        fastVertex(buffer, matrices, x1, y0, z).texture(1, 0).next();
        fastVertex(buffer, matrices, x0, y0, z).texture(0, 0).next();
        Tessellator.getInstance().draw();
    }

    public static void drawOverlay(MatrixStack matrices, float alpha,
            int width, int height,
            Identifier texture,
            float u0, float v0,
            float u1, float v1, int offset) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        RenderSystem.setShaderTexture(0, texture);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        fastVertex(buffer, matrices, -offset, height + offset, SCREEN_Z_OFFSET).texture(u0, v1).next();
        fastVertex(buffer, matrices, width + offset, height + offset, SCREEN_Z_OFFSET).texture(u1, v1).next();
        fastVertex(buffer, matrices, width + offset, -offset, SCREEN_Z_OFFSET).texture(u1, v0).next();
        fastVertex(buffer, matrices, -offset, -offset, SCREEN_Z_OFFSET).texture(u0, v0).next();
        Tessellator.getInstance().draw();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static void drawRepeatingTexture(MatrixStack matrices, BufferVertexConsumer buffer,
            float x0, float x1, float y0, float y1,
            float u0, float u1, float v0, float v1,
            float amplification, int light, int overlay) {

        final float xFrameSize = (u1 - u0) * amplification;
        final float yFrameSize = (v1 - v0) * amplification;

        final int xRepeats = MathHelper.ceil((x1 - x0) / xFrameSize);
        final int yRepeats = MathHelper.ceil((y1 - y0) / yFrameSize);

        for (int x = 0; x <= xRepeats; x++) {
            float x2 = x0 + x * xFrameSize;
            float x3 = Math.min(x2 + xFrameSize, x1);
            float xLen = x0 == x1 ? 0 : (x3 - x2) / (x1 - x0);

            for (int y = 0; y <= yRepeats; y++) {
                float y2 = y0 + y * yFrameSize;
                float y3 = Math.min(y2 + yFrameSize, y1);
                float yLen = y0 == y1 ? 0 : (y3 - y2) / (y1 - y0);

                float u2 = u0 + Math.min(u1 - u0, xLen);
                float v2 = v0 + Math.min(v1 - v0, yLen);

                vertex(buffer, matrices, x2, y3, 0, u0, v2, light, overlay);
                vertex(buffer, matrices, x3, y3, 0, u2, v2, light, overlay);
                vertex(buffer, matrices, x3, y2, 0, u2, v0, light, overlay);
                vertex(buffer, matrices, x2, y2, 0, u0, v0, light, overlay);
            }
        }
    }
}

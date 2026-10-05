package ivorius.psychedelicraft.client.render;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;

/**
 * Arbitrary textured quads for the (deferred) gui renderer. Vertices are added in quad order.
 */
public record GuiQuads(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, List<Vertex> vertices, @Nullable ScreenRect bounds, @Nullable ScreenRect scissorArea) implements SimpleGuiElementRenderState {
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void setupVertices(VertexConsumer consumer) {
        for (Vertex v : vertices) {
            consumer.vertex(pose, v.x, v.y).texture(v.u, v.v).color(v.color);
        }
    }

    public record Vertex(float x, float y, float u, float v, int color) {}

    public static final class Builder {
        private final List<Vertex> vertices = new ArrayList<>();

        public Builder vertex(float x, float y, float u, float v, int color) {
            vertices.add(new Vertex(x, y, u, v, color));
            return this;
        }

        public Builder quad(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, int color) {
            return vertex(x0, y0, u0, v0, color)
                .vertex(x0, y1, u0, v1, color)
                .vertex(x1, y1, u1, v1, color)
                .vertex(x1, y0, u1, v0, color);
        }

        public void draw(DrawContext context, Identifier texture) {
            AbstractTexture tex = MinecraftClient.getInstance().getTextureManager().getTexture(texture);
            draw(context, RenderPipelines.GUI_TEXTURED, TextureSetup.of(tex.getGlTextureView(), tex.getSampler()));
        }

        public void draw(DrawContext context, RenderPipeline pipeline, TextureSetup textureSetup) {
            if (vertices.isEmpty()) {
                return;
            }
            Matrix3x2f pose = new Matrix3x2f(context.getMatrices());
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            Vector2f p = new Vector2f();
            for (Vertex v : vertices) {
                pose.transformPosition(v.x, v.y, p);
                minX = Math.min(minX, p.x);
                minY = Math.min(minY, p.y);
                maxX = Math.max(maxX, p.x);
                maxY = Math.max(maxY, p.y);
            }
            ScreenRect bounds = new ScreenRect((int)Math.floor(minX), (int)Math.floor(minY), (int)Math.ceil(maxX - minX), (int)Math.ceil(maxY - minY));
            context.state.addSimpleElement(new GuiQuads(pipeline, textureSetup, pose, List.copyOf(vertices), bounds, context.scissorStack.peekLast()));
        }
    }
}

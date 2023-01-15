package ivorius.psychedelicraft.client.render.effect;

import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;

public interface ScreenEffect extends AutoCloseable {
    default boolean shouldApply(float tickDelta) {
        return true;
    }

    void update(float tickDelta);

    void render(MatrixStack matrices, VertexConsumerProvider vertices, int screenWidth, int screenHeight, float ticks, PingPong pingPong);

    static void drawScreen(int screenWidth, int screenHeight) {
        BufferBuilder renderer = Tessellator.getInstance().getBuffer();
        renderer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        renderer.vertex(0, 0, 0).texture(0, 1).next();
        renderer.vertex(0, screenHeight, 0).texture(0, 0).next();
        renderer.vertex(screenWidth, screenHeight, 0).texture(1, 0).next();
        renderer.vertex(screenWidth, 0, 0).texture(1, 1).next();
        Tessellator.getInstance().draw();
    }

    interface PingPong {
        void pingPong();
    }
}

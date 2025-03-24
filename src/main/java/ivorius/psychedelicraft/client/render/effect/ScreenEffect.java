package ivorius.psychedelicraft.client.render.effect;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;

public interface ScreenEffect extends AutoCloseable {
    default boolean shouldApply(float tickDelta) {
        return true;
    }

    void update(float tickDelta);

    void render(DrawContext context, Window window, float tickDelta);

    interface PingPong {
        void pingPong();
    }
}

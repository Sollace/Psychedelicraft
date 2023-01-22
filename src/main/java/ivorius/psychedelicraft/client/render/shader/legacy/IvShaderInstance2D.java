package ivorius.psychedelicraft.client.render.shader.legacy;

import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;

/**
 * Created by Sollace on 4 Jan 2023
 */
@Deprecated
public abstract class IvShaderInstance2D implements AutoCloseable {
    static void drawScreen(int screenWidth, int screenHeight) {
    }

    public interface PingPong {
        void pingPong();
    }

    protected IvShaderInstance2D(Logger logger) {}

    protected boolean useShader() {
        return true;
    }

    protected void stopUsingShader() {}

    protected void setUniformFloats(String name, float...ints) {}
    protected void setUniformInts(String name, int...ints) {}

    protected void setUniformMatrix(String name, Matrix4f matrix) {}

    public void render(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {

    }

    public abstract boolean shouldApply(float tickDelta);

    protected void drawFullScreen(int width, int height, PingPong pingPong) {}

    @Override
    public void close() throws Exception {}

    public void update(float tickDelta) {}
}

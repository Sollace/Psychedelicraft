package ivorius.psychedelicraft.client.render.shader.legacy.program;

import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;

/**
 * Created by Sollace on 4 Jan 2023
 */
public abstract class IvShaderInstance3D implements AutoCloseable, ShaderWorld {
    protected IvShaderInstance3D(Logger logger) {

    }

    @Override
    public boolean isShaderActive() {
        return false;
    }

    public void trySettingUpShader(String vertexShaderFile, String fragmentShaderFile) {

    }

    static void drawScreen(int screenWidth, int screenHeight) {
    }

    public interface PingPong {
        void pingPong();
    }

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

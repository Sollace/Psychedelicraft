package ivorius.psychedelicraft.client.render.shader.program;

import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;

import ivorius.psychedelicraft.client.render.effect.ScreenEffect;

/**
 * Created by Sollace on 4 Jan 2023
 */
public class IvShaderInstance2D implements ScreenEffect {

    protected IvShaderInstance2D(Logger logger) {}

    protected boolean useShader() {
        return true;
    }

    protected void stopUsingShader() {}

    protected void setUniformFloats(String name, float...ints) {}
    protected void setUniformInts(String name, int...ints) {}

    protected void setUniformMatrix(String name, Matrix4f matrix) {}

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {

    }

    @Override
    public boolean shouldApply(float tickDelta) {
        return false;
    }

    protected void drawFullScreen(int width, int height, ScreenEffect.PingPong pingPong) {}
}

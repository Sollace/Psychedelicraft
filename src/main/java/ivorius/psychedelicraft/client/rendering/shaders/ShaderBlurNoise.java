/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.shaders;

import org.apache.logging.log4j.Logger;

/**
 * Created by lukas on 18.02.14.
 */
@Deprecated
public class ShaderBlurNoise extends IvShaderInstance2D {
    public float strength;
    public float seed;

    public ShaderBlurNoise(Logger logger) {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks) {
        return strength > 0 && super.shouldApply(ticks);
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        useShader();

        setUniformInts("tex0", 0);
        setUniformFloats("pixelSize", 1F / screenWidth, 1F / screenHeight);
        setUniformFloats("strength", strength);
        setUniformFloats("seed", seed);
        setUniformFloats("totalAlpha", 1F);

        drawFullScreen(screenWidth, screenHeight, pingPong);

        stopUsingShader();
    }
}

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
public class ShaderSimpleEffects extends IvShaderInstance2D {
    public float slowColorRotation;
    public float quickColorRotation;
    public float colorIntensification;
    public float desaturation;

    public ShaderSimpleEffects(Logger logger) {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks) {
        return super.shouldApply(ticks) && (
               slowColorRotation > 0
            || quickColorRotation > 0
            || colorIntensification > 0
            || desaturation > 0
        );
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        useShader();
        setUniformInts("tex0", 0);
        setUniformFloats("totalAlpha", 1F);
        setUniformFloats("slowColorRotation", slowColorRotation);
        setUniformFloats("quickColorRotation", quickColorRotation);
        setUniformFloats("colorIntensification", colorIntensification);
        setUniformFloats("desaturation", desaturation);
        drawFullScreen(screenWidth, screenHeight, pingPong);
        stopUsingShader();
    }
}

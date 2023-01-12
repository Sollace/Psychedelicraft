/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.shaders;

import org.apache.logging.log4j.Logger;

import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 18.02.14.
 */
@Deprecated
public class ShaderBlur extends IvShaderInstance2D {
    public float vBlur;
    public float hBlur;

    public ShaderBlur(Logger logger) {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks) {
        return (vBlur > 0.0f || hBlur > 0.0f) && super.shouldApply(ticks);
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        useShader();

        setUniformInts("tex0", 0);
        setUniformFloats("pixelSize", 1F / screenWidth, 1F / screenHeight);

        for (int n = 0; n < MathHelper.ceil(Math.max(hBlur, vBlur)); n++) {
            for (int i = 0; i < 2; i++) {
                float activeBlur = Math.min(1, (i == 0 ? hBlur : vBlur) - n);

                if (activeBlur > 0) {
                    setUniformInts("vertical", i);
                    setUniformFloats("totalAlpha", activeBlur);

                    drawFullScreen(screenWidth, screenHeight, pingPong);
                }
            }
        }

        stopUsingShader();
    }
}

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
public class ShaderRadialBlur extends IvShaderInstance2D {
    public float radialBlur;

    public ShaderRadialBlur(Logger logger) {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks) {
        return radialBlur > 0 && super.shouldApply(ticks);
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        useShader();

        setUniformInts("tex0", 0);
        setUniformFloats("pixelSize", 1F / screenWidth * (radialBlur * 1.5f + 1F), 1F / screenHeight * (radialBlur * 1.5F + 1F));

        for (int n = 0; n < MathHelper.floor(radialBlur) + 1; n++) {
            float activeBlur = Math.min(1, radialBlur - n);

            if (activeBlur > 0) {
                setUniformFloats("totalAlpha", activeBlur);
                drawFullScreen(screenWidth, screenHeight, pingPong);
            }
        }

        stopUsingShader();
    }
}

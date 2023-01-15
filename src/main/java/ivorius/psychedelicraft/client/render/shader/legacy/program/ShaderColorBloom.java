/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy.program;

import org.apache.logging.log4j.Logger;

import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 18.02.14.
 */
public class ShaderColorBloom extends IvShaderInstance2D {
    public float[] coloredBloom;

    public ShaderColorBloom(Logger logger) {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks) {
        return coloredBloom[3] > 0 && super.shouldApply(ticks);
    }

    @Override
    public void render(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        useShader();

        setUniformInts("tex0", 0);
        setUniformFloats("pixelSize", 1F / screenWidth, 1F / screenHeight);
        setUniformFloats("bloomColor", coloredBloom[0], coloredBloom[1], coloredBloom[2]);

        for (int n = 0; n < MathHelper.ceil(coloredBloom[3]); n++) {
            setUniformFloats("totalAlpha", Math.min(1, coloredBloom[3] - n));

            for (int i = 0; i < 2; i++) {
                setUniformInts("vertical", i);
                drawFullScreen(screenWidth, screenHeight, pingPong);
            }
        }

        stopUsingShader();
    }
}

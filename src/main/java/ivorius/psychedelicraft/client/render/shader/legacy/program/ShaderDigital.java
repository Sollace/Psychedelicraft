/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy.program;

import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.util.MathUtils;

/**
 * Created by lukas on 18.02.14.
 */
@Deprecated
public class ShaderDigital extends IvShaderInstance2D {
    public float digital;
    public float[] maxDownscale;

    public int digitalTextTexture;

    public ShaderDigital(Logger logger) {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks) {
        return digital > 0 && digitalTextTexture > 0;
    }

    @Override
    public void render(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        useShader();
        uploadTextures();
        setUniformFloats("totalAlpha", 1);
        uploadUniforms(screenWidth, screenHeight);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        drawFullScreen(screenWidth, screenHeight, pingPong);
        stopUsingShader();
    }

    protected void uploadTextures() {
        RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE, digitalTextTexture);
        for (int i = 0; i < 3; i++) {
            setUniformInts("tex" + i, i);
        }
    }

    protected void uploadUniforms(int screenWidth, int screenHeight) {
        float downscale = MathUtils.mixEaseInOut(0, 0.95F, Math.min(digital * 3, 1));
        downscale += digital * 0.05f; //Bigger pixels!

        setUniformFloats("newResolution",
                screenWidth * (1 + (maxDownscale[0] - 1) * downscale),
                screenHeight * (1 + (maxDownscale[1] - 1) * downscale)
        );

        float textProgress = MathUtils.easeZeroToOne((digital - 0.2F) * 5);
        float binaryProgress = MathUtils.easeZeroToOne((digital - 0.8F) * 10);

        setUniformFloats("textProgress", textProgress + binaryProgress);
        setUniformFloats("maxColors", digital > 0.4F ? (Math.max(256F / ((digital - 0.4F) * 640 + 1), 2)) : -1); //Step 3, 0.2 is enough for only 2 colors
        setUniformFloats("saturation", 1 - MathUtils.easeZeroToOne((digital - 0.6F) * 5));
    }
}

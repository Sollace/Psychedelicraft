/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.program;

import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.render.GLStateProxy;
import net.minecraft.util.math.MathHelper;

/**
 * Created by lukas on 18.02.14.
 */
@Deprecated
public class ShaderDoF extends IvShaderInstance2D {
    public int depthTextureIndex;

    public float zNear;
    public float zFar;

    public float focalPointNear;
    public float focalBlurNear;

    public float focalPointFar;
    public float focalBlurFar;

    public ShaderDoF(Logger logger) {
        super(logger);
    }

    @Override
    public boolean shouldApply(float ticks) {
        return depthTextureIndex > 0 && (focalBlurNear > 0 || focalBlurFar > 0);
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        useShader();

        RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 1, depthTextureIndex);
        setUniformInts("depthTex", 2);
        RenderSystem.activeTexture(GLStateProxy.DEFAULT_TEXTURE);
        setUniformInts("tex", 0);
        setUniformFloats("pixelSize", 1.0f / screenWidth, 1.0f / screenHeight);
        setUniformFloats("focalPointNear", focalPointNear);
        setUniformFloats("focalPointFar", focalPointFar);

        float maxDof = Math.max(focalBlurFar, focalBlurNear);

        for (int n = 0; n < MathHelper.ceil(maxDof); n++) {
            float curBlurNear = MathHelper.clamp(focalBlurNear - n, 0, 1);
            float curBlurFar = MathHelper.clamp(focalBlurFar - n, 0, 1);

            if (curBlurNear > 0.0f || curBlurFar > 0.0f) {
                setUniformFloats("focalBlurNear", curBlurNear);
                setUniformFloats("focalBlurFar", curBlurFar);

                for (int i = 0; i < 2; i++) {
                    setUniformInts("vertical", i);
                    drawFullScreen(screenWidth, screenHeight, pingPong);
                }
            }
        }

        setUniformFloats("depthRange", zNear, zFar);
        stopUsingShader();
    }
}

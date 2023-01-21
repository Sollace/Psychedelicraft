/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.shader.legacy.program.IvShaderInstance2D;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

import java.util.Random;

import org.apache.logging.log4j.Logger;

/**
 * Created by lukas on 26.04.14.
 */
public class WrapperBlurNoise extends ShaderWrapper<WrapperBlurNoise.ShaderBlurNoise> {
    public WrapperBlurNoise(String utils) {
        super(new ShaderBlurNoise(Psychedelicraft.LOGGER), getRL("shaderBasic.vert"), getRL("shaderBlurNoise.frag"), utils);
    }

    @Override
    public void setShaderValues(float partialTicks, int ticks, Framebuffer depthBuffer) {
        DrugProperties drugProperties = DrugProperties.of(MinecraftClient.getInstance().player);

        if (drugProperties != null) {
            shaderInstance.strength = drugProperties.getDrugValue(DrugType.POWER) * 0.6f;
            shaderInstance.seed = new Random((long) ((ticks + partialTicks) * 1000.0)).nextFloat() * 9.0f + 1.0f;
        } else {
            shaderInstance.strength = 0.0f;
        }
    }

    @Deprecated
    static class ShaderBlurNoise extends IvShaderInstance2D {
        public float strength;
        public float seed;

        public ShaderBlurNoise(Logger logger) {
            super(logger);
        }

        @Override
        public boolean shouldApply(float ticks) {
            return strength > 0;
        }

        @Override
        public void render(int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
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
}

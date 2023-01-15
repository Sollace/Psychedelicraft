/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy.program;

import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;

import org.apache.logging.log4j.Logger;

/**
 * Created by lukas on 26.02.14.
 */
@Deprecated
public class ShaderMainDepth extends IvShaderInstance3D implements ShaderWorld {
    public ShaderMainDepth(Logger logger) {
        super(logger);
    }

    @Override
    public boolean activate(float partialTicks, float ticks) {
        if (!useShader()) {
            return false;
        }

        MinecraftClient mc = MinecraftClient.getInstance();

        setUniformFloats("ticks", ticks);
        setUniformInts("worldTime", (int) mc.world.getTime());

        setUniformFloats("playerPos", (float) mc.player.getX(), (float) mc.player.getY(), (float) mc.player.getZ());
        setDepthMultiplier(1.0f);
        setTexture2DEnabled(GLStateProxy.isTextureEnabled(GLStateProxy.DEFAULT_TEXTURE));
        setOverrideColor(null);
        setUniformInts("useScreenTexCoords", GLStateProxy.getUsesScreenTexCoords() ? 1 : 0);
        setUniformFloats("pixelSize", GLStateProxy.getResolution());

        DrugProperties drugProperties = DrugProperties.of(mc.player);
        setUniformFloats("bigWaves", drugProperties.getHallucinations().getBigWaveStrength(partialTicks));
        setUniformFloats("smallWaves", drugProperties.getHallucinations().getSmallWaveStrength(partialTicks));
        setUniformFloats("wiggleWaves", drugProperties.getHallucinations().getWiggleWaveStrength(partialTicks));
        setUniformFloats("distantWorldDeformation", drugProperties.getHallucinations().getDistantWorldDeformationStrength(partialTicks));

        return true;
    }

    @Override
    public void deactivate() {
        stopUsingShader();
    }

    @Override
    public void setTexture2DEnabled(boolean enabled) {
        setUniformInts("texture2DEnabled", enabled ? 1 : 0);
    }

    @Override
    public void setOverrideColor(float[] color) {
        if (color != null) {
            setUniformFloats("overrideColor", color);
        } else {
            setUniformFloats("overrideColor", 1F, 1F, 1F, 1F);
        }
    }

    @Override
    public void setDepthMultiplier(float depthMultiplier) {
        setUniformFloats("depthMultiplier", depthMultiplier);
    }
}

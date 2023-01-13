/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.program;

import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

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

        Entity renderEntity = mc.getCameraEntity();
        DrugProperties drugProperties = DrugProperties.getDrugProperties(renderEntity);

        setUniformFloats("ticks", ticks);
        setUniformInts("worldTime", (int) mc.world.getTime());

        setUniformFloats("playerPos", (float) renderEntity.getX(), (float) renderEntity.getY(), (float) renderEntity.getZ());
        setDepthMultiplier(1.0f);
        setTexture2DEnabled(GLStateProxy.isTextureEnabled(GLStateProxy.DEFAULT_TEXTURE));
        setOverrideColor(null);
        setUseScreenTexCoords(false);
        setPixelSize(1F / mc.getWindow().getFramebufferWidth(), 1F / mc.getWindow().getFramebufferHeight());

        float bigWaveStrength = 0.0f;
        float smallWaveStrength = 0.0f;
        float wiggleWaveStrength = 0.0f;
        float distantWorldDeformationStrength = 0.0f;
        if (drugProperties != null)
        {
            bigWaveStrength = drugProperties.getHallucinations().getBigWaveStrength(drugProperties, partialTicks);
            smallWaveStrength = drugProperties.getHallucinations().getSmallWaveStrength(drugProperties, partialTicks);
            wiggleWaveStrength = drugProperties.getHallucinations().getWiggleWaveStrength(drugProperties, partialTicks);
            distantWorldDeformationStrength = drugProperties.getHallucinations().getDistantWorldDeformationStrength(drugProperties, partialTicks);
        }
        setUniformFloats("bigWaves", bigWaveStrength);
        setUniformFloats("smallWaves", smallWaveStrength);
        setUniformFloats("wiggleWaves", wiggleWaveStrength);
        setUniformFloats("distantWorldDeformation", distantWorldDeformationStrength);

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
    public void setLightmapEnabled(boolean enabled) {

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
    public void setGLLightEnabled(boolean enabled) { }

    @Override
    public void setGLLight(int number, float x, float y, float z, float strength, float specular) { }

    @Override
    public void setGLLightAmbient(float strength) {  }

    @Override
    public void setFogMode(int mode) { }

    @Override
    public void setFogEnabled(boolean enabled) { }

    @Override
    public void setDepthMultiplier(float depthMultiplier) {
        setUniformFloats("depthMultiplier", depthMultiplier);
    }

    @Override
    public void setUseScreenTexCoords(boolean enabled) {
        setUniformInts("useScreenTexCoords", enabled ? 1 : 0);
    }

    @Override
    public void setPixelSize(float pixelWidth, float pixelHeight) {
        setUniformFloats("pixelSize", pixelWidth, pixelHeight);
    }

    @Override
    public void setBlendModeEnabled(boolean enabled) { }

    @Override
    public void setBlendFunc(int sFactor, int dFactor, int sFactorA, int dFactorA) { }

    @Override
    public void setProjectShadows(boolean projectShadows) { }

    @Override
    public void setForceColorSafeMode(boolean enable) { }
}

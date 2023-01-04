/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.shaders;

import ivorius.ivtoolkit.rendering.IvDepthBuffer;
import ivorius.ivtoolkit.rendering.IvShaderInstance3D;
import ivorius.psychedelicraft.client.rendering.GLStateProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.EntityLivingBase;
import org.apache.logging.log4j.Logger;

/**
 * Created by lukas on 26.02.14.
 */
@Deprecated(forRemoval = true, since = "Not necessary: the game implements shaders for us already")
public class ShaderShadows extends IvShaderInstance3D implements ShaderWorld {
    public IvDepthBuffer depthBuffer;

    public ShaderShadows(Logger logger) {
        super(logger);

        int pixels = getShadowPixels();
        depthBuffer = new IvDepthBuffer(pixels, pixels, logger);
    }

    @Override
    public void trySettingUpShader(String vertexShaderFile, String fragmentShaderFile) {
        super.trySettingUpShader(vertexShaderFile, fragmentShaderFile);
        depthBuffer.allocate();
    }

    @Override
    public boolean activate(float partialTicks, float ticks) {
        if (!depthBuffer.isAllocated() || !useShader()) {
            return false;
        }

        int pixels = getShadowPixels();
        depthBuffer.setSize(pixels, pixels);

        Minecraft mc = Minecraft.getMinecraft();

        EntityLivingBase renderEntity = mc.renderViewEntity;

        setUniformFloats("ticks", ticks);
        setUniformInts("worldTime", (int) mc.theWorld.getWorldTime());

        setTexture2DEnabled(GLStateProxy.isTextureEnabled(OpenGlHelper.defaultTexUnit));
        setUniformFloats("playerPos", (float) renderEntity.posX, (float) renderEntity.posY, (float) renderEntity.posZ);
        setUniformFloats("depthMultiplier", 1.0f);
        setUniformFloats("pixelSize", 1.0f / depthBuffer.getTextureWidth(), 1.0f / depthBuffer.getTextureHeight());
        setUniformInts("useScreenTexCoords", 0);
        setOverrideColor(null);

        depthBuffer.setParentFB(PSRenderStates.getMCFBO());
        depthBuffer.bind();

        return true;
    }

    @Override
    public void deactivate() {
        if (isShaderActive()) {
            depthBuffer.unbind();
        }

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
    public void setGLLightEnabled(boolean enabled) {

    }

    @Override
    public void setGLLight(int number, float x, float y, float z, float strength, float specular) {

    }

    @Override
    public void setGLLightAmbient(float strength) {

    }

    @Override
    public void setFogMode(int mode) {

    }

    @Override
    public void setFogEnabled(boolean enabled) {

    }

    @Override
    public void setDepthMultiplier(float depthMultiplier) {

    }

    @Override
    public void setUseScreenTexCoords(boolean enabled) {

    }

    @Override
    public void setPixelSize(float pixelWidth, float pixelHeight) {

    }

    @Override
    public void setBlendModeEnabled(boolean enabled) {

    }

    @Override
    public void setBlendFunc(int sFactor, int dFactor, int sFactorA, int dFactorA) {

    }

    @Override
    public void setProjectShadows(boolean projectShadows) {
        // Do something? :/
    }

    @Override
    public void setForceColorSafeMode(boolean enable) {

    }

    public static int getShadowPixels() {
        return MinecraftClient.getInstance().options.getViewDistance().getValue() * PSRenderStates.shadowPixelsPerChunk;
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.shaders;

import ivorius.psychedelicraft.client.rendering.GLStateProxy;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

import org.apache.logging.log4j.Logger;

/**
 * Created by lukas on 26.02.14.
 */
@Deprecated(forRemoval = true, since = "Not necessary: the game implements shaders for us already")
public class ShaderShadows extends IvShaderInstance3D implements ShaderWorld {
    public Framebuffer depthBuffer;

    public ShaderShadows(Logger logger) {
        super(logger);

        int pixels = getShadowPixels();
        depthBuffer = new SimpleFramebuffer(pixels, pixels, true, true);
    }

    @Override
    public void trySettingUpShader(String vertexShaderFile, String fragmentShaderFile) {
        int pixels = getShadowPixels();
        depthBuffer.initFbo(pixels, pixels, false);
    }

    @Override
    public boolean activate(float partialTicks, float ticks) {
        if (depthBuffer.fbo == -1 || !useShader()) {
            return false;
        }

        int pixels = getShadowPixels();
        depthBuffer.resize(pixels, pixels, true);

        MinecraftClient mc = MinecraftClient.getInstance();

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d pos = camera.getPos();

        setUniformFloats("ticks", ticks);
        setUniformInts("worldTime", (int) mc.world.getTime());

        setTexture2DEnabled(GLStateProxy.isTextureEnabled(GLStateProxy.DEFAULT_TEXTURE));
        setUniformFloats("playerPos", (float) pos.x, (float) pos.y, (float) pos.z);
        setUniformFloats("depthMultiplier", 1.0f);
        setUniformFloats("pixelSize", 1F / depthBuffer.textureWidth, 1F / depthBuffer.textureHeight);
        setUniformInts("useScreenTexCoords", 0);
        setOverrideColor(null);

        return true;
    }

    @Override
    public void deactivate() {
        if (isShaderActive()) {
            depthBuffer.delete();
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

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy.program;

import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.PsycheShadowHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

import org.apache.logging.log4j.Logger;

/**
 * Created by lukas on 26.02.14.
 */
@Deprecated
public class ShaderShadows extends IvShaderInstance3D implements ShaderWorld {
    public static int shadowPixelsPerChunk = 256;

    public Framebuffer depthBuffer;

    public ShaderShadows(Logger logger) {
        super(logger);

        int pixels = PsycheShadowHelper.getShadowPixels();
        depthBuffer = new SimpleFramebuffer(pixels, pixels, true, true);
    }

    @Override
    public void trySettingUpShader(String vertexShaderFile, String fragmentShaderFile) {
        int pixels = PsycheShadowHelper.getShadowPixels();
        depthBuffer.initFbo(pixels, pixels, false);
    }

    @Override
    public boolean activate(float partialTicks, float ticks) {
        if (depthBuffer.fbo == -1 || !useShader()) {
            return false;
        }

        int pixels = PsycheShadowHelper.getShadowPixels();
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
    public void setOverrideColor(float[] color) {
        if (color != null) {
            setUniformFloats("overrideColor", color);
        } else {
            setUniformFloats("overrideColor", 1F, 1F, 1F, 1F);
        }
    }

    @Override
    public void setProjectShadows(boolean projectShadows) {
        // Do something? :/
    }
}

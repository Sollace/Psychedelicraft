/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.shaders;

import com.google.common.base.Charsets;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.*;
import ivorius.psychedelicraft.client.rendering.effectWrappers.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class PSRenderStates {
    private static ShaderWorld currentShader;
    private static ShaderMain shaderInstance;
    private static ShaderMainDepth shaderInstanceDepth;
    private static ShaderShadows shaderInstanceShadows;

    private static List<EffectWrapper> effectWrappers = new ArrayList<>();

    private static boolean shader3DEnabled = true;
    public static boolean shader2DEnabled = true;
    public static boolean doShadows = false;
    public static boolean doHeatDistortion = false;
    public static boolean doWaterDistortion = false;
    public static boolean doMotionBlur = false;

    public static float sunFlareIntensity;
    public static int shadowPixelsPerChunk = 256;

    private static String currentRenderPass;

    @Deprecated(since = "hook")
    public static boolean startRenderPass(String pass, float partialTicks, float ticks) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (currentRenderPass != null)
            endRenderPass();

        currentRenderPass = pass;

        switch (pass) {
            case "Default":
                shaderInstance.shouldDoShadows = doShadows;
                shaderInstance.shadowDepthTextureIndex = shaderInstanceShadows.depthBuffer.getDepthAttachment();
                return useShader(partialTicks, ticks, shaderInstance);
            case "Depth":
                return useShader(partialTicks, ticks, shaderInstanceDepth);
            case "Shadows":
                MinecraftClient.getInstance().cameraEntity = new EntityFakeSun(mc.cameraEntity);
                return useShader(partialTicks, ticks, shaderInstanceShadows);
        }

        return true;
    }

    @Deprecated(since = "hook")
    public static void endRenderPass() {
        switch (currentRenderPass) {
            case "Default":
                break;
            case "Depth":
                break;
            case "Shadows":
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.cameraEntity instanceof EntityFakeSun)
                    mc.cameraEntity = ((EntityFakeSun) mc.cameraEntity).prevViewEntity;
                break;
        }

        if (currentShader != null) {
            currentShader.deactivate();
            currentShader = null;
        }

        currentRenderPass = null;
    }

    @Deprecated(since = "hook")
    public static boolean setupCameraTransform() {
        if ("Shadows".equals(currentRenderPass)/* || (MinecraftClient.getInstance().ingameGUI.getUpdateCounter() % 100 > 2)*/) {
            PsycheShadowHelper.setupSunGLTransform();

            return true;
        }

        return false;
    }

    public static void allocate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        String utils = mc.getResourceManager()
                .getResource(new Identifier(Psychedelicraft.MODID, Psychedelicraft.filePathShaders + "shaderUtils.frag"))
                .map(resource -> {
                    try {
                        return IOUtils.toString(resource.getInputStream(), Charsets.UTF_8);
                    } catch (Exception ex) {
                        Psychedelicraft.logger.error("Could not load shader utils!", ex);
                    }
                    return null;
                }).orElse(null);


        shaderInstance = new ShaderMain(Psychedelicraft.logger);
        //setUpShader(shaderInstance, "shader3D.vert", "shader3D.frag", utils);

        shaderInstanceDepth = new ShaderMainDepth(Psychedelicraft.logger);
        //setUpShader(shaderInstanceDepth, "shader3D.vert", "shader3DDepth.frag", utils);

        shaderInstanceShadows = new ShaderShadows(Psychedelicraft.logger);
        //setUpShader(shaderInstanceShadows, "shader3D.vert", "shader3DDepth.frag", utils);

        // Add order = Application order!
        effectWrappers.add(new WrapperHeatDistortion(utils));
        effectWrappers.add(new WrapperUnderwaterDistortion(utils));
        effectWrappers.add(new WrapperWaterOverlay(utils));
        effectWrappers.add(new WrapperSimpleEffects(utils));
        effectWrappers.add(new WrapperMotionBlur());
        effectWrappers.add(new WrapperBlur(utils));
        effectWrappers.add(new WrapperDoF(utils));
        effectWrappers.add(new WrapperRadialBlur(utils));
        effectWrappers.add(new WrapperBloom(utils));
        effectWrappers.add(new WrapperColorBloom(utils));
        effectWrappers.add(new WrapperDoubleVision(utils));
        effectWrappers.add(new WrapperBlurNoise(utils));
        effectWrappers.add(new WrapperDigital(utils));
    }

    public static void setUpRealtimeCacheTexture() {
    }

    public static void update() {
        if (MinecraftClient.getInstance().world != null) {
            effectWrappers.forEach(EffectWrapper::update);
        }
    }

    private static boolean useShader(float partialTicks, float ticks, ShaderWorld shader)
    {
        //RenderSystem.setShader(() -> (ShaderProgram)shader);
        currentShader = null;

        if (shader != null && shader3DEnabled)
        {
            if (shader.isShaderActive())
                return true;

            if (shader.activate(partialTicks, ticks))
            {
                currentShader = shader;
                return true;
            }
        }

        return false;
    }

    @Deprecated(since = "unused")
    public static void preRenderSky(float partialTicks)
    {
        /*if (renderFakeSkybox)
        {
            setForceColorSafeMode(true);
            float boxSize = MinecraftClient.getInstance().gameSettings.renderDistanceChunks * 16 * 0.75f;
            float[] fogColor = PSAccessHelperClient.getFogColor();

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor3f(fogColor[0], fogColor[1], fogColor[2]);
            Tessellator.instance.startDrawingQuads();
            IvRenderHelper.renderCuboid(Tessellator.instance, -boxSize, -boxSize, -boxSize, 1.0f);
            Tessellator.instance.draw();
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            setForceColorSafeMode(false);
        }*/
    }

    public static void setUseScreenTexCoords(boolean enabled) {
        if (currentShader != null)
            currentShader.setUseScreenTexCoords(enabled);
    }

    public static void setScreenSizeDefault() {
        Window mc = MinecraftClient.getInstance().getWindow();
        setScreenSize(mc.getWidth(), mc.getHeight());
    }

    private static void setScreenSize(float screenWidth, float screenHeight) {
        setPixelSize(1.0f / screenWidth, 1.0f / screenHeight);
    }

    public static void setPixelSize(float pixelWidth, float pixelHeight) {
        if (currentShader != null)
            currentShader.setPixelSize(pixelWidth, pixelHeight);
    }

    public static int getMCFBO() {
        MinecraftClient mc = MinecraftClient.getInstance();
        Framebuffer framebuffer = mc.getFramebuffer();

        return (framebuffer != null && framebuffer.fbo >= 0) ? framebuffer.fbo : 0;
    }

    public static int getTextureIndex(Identifier loc) {
        TextureManager tm = MinecraftClient.getInstance().getTextureManager();
        tm.bindTexture(loc); // Allocate texture. MOJANG!
        return tm.getTexture(loc).getGlId();
    }
}

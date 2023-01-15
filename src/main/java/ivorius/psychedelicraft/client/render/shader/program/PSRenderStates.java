/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.program;

import com.google.common.base.Charsets;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.*;
import ivorius.psychedelicraft.client.render.effect.EffectWrapper;
import ivorius.psychedelicraft.client.render.shader.*;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.math.MatrixStack;

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

    private static String currentRenderPass;

    private static final String[] RENDER_PASSES = { "Default", "Depth", "Shadows" };

    @Deprecated(since = "hook")
    public static void beforeWorldRender(float partialTicks, int rendererUpdateCount) {

        float ticks = rendererUpdateCount + partialTicks;
       // preRender(ticks);

        for (String pass : RENDER_PASSES) {
            if (!pass.equals("Default")) {
                if (startRenderPass(pass, partialTicks, ticks)) {
                   // MinecraftClient.getInstance().worldRenderer.renderWorld(partialTicks, 0L);
                    endRenderPass();
                }
            }
        }

        startRenderPass("Default", partialTicks, ticks);
       // preRender3D(ticks);
    }

    @Deprecated(since = "hook")
    public static void afterWorldRender(MatrixStack matrices, float partialTicks, int rendererUpdateCount) {
        float ticks = rendererUpdateCount + partialTicks;
        PSRenderStates.endRenderPass();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            DrugRenderer.INSTANCE.renderOverlaysBeforeShaders(matrices, partialTicks, mc.player, rendererUpdateCount,
                    mc.getWindow().getFramebufferWidth(),
                    mc.getWindow().getFramebufferHeight(), DrugProperties.of(mc.player));
        }

       // postRender(ticks, partialTicks);
    }

    private static boolean startRenderPass(String pass, float partialTicks, float ticks) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (currentRenderPass != null)
            endRenderPass();

        currentRenderPass = pass;

        switch (pass) {
            case "Default":
                shaderInstance.shouldDoShadows = PsychedelicraftClient.getConfig().visual.doShadows;
                shaderInstance.shadowDepthTextureIndex = shaderInstanceShadows.depthBuffer.getDepthAttachment();
                return useShader(partialTicks, ticks, shaderInstance);
            case "Depth":
                return useShader(partialTicks, ticks, shaderInstanceDepth);
            case "Shadows":
                MinecraftClient.getInstance().cameraEntity = new FakeSunEntity(mc.cameraEntity);
                return useShader(partialTicks, ticks, shaderInstanceShadows);
        }

        return true;
    }

    private static void endRenderPass() {
        switch (currentRenderPass) {
            case "Default":
                break;
            case "Depth":
                break;
            case "Shadows":
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.cameraEntity instanceof FakeSunEntity)
                    mc.cameraEntity = ((FakeSunEntity) mc.cameraEntity).prevViewEntity;
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

    @Deprecated
    public static void allocate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        String utils = mc.getResourceManager()
                .getResource(Psychedelicraft.id(Psychedelicraft.SHADERS_PATH + "shaderUtils.frag"))
                .map(resource -> {
                    try {
                        return IOUtils.toString(resource.getInputStream(), Charsets.UTF_8);
                    } catch (Exception ex) {
                        Psychedelicraft.LOGGER.error("Could not load shader utils!", ex);
                    }
                    return null;
                }).orElse(null);


        shaderInstance = new ShaderMain(Psychedelicraft.LOGGER);
        //setUpShader(shaderInstance, "shader3D.vert", "shader3D.frag", utils);

        shaderInstanceDepth = new ShaderMainDepth(Psychedelicraft.LOGGER);
        //setUpShader(shaderInstanceDepth, "shader3D.vert", "shader3DDepth.frag", utils);

        shaderInstanceShadows = new ShaderShadows(Psychedelicraft.LOGGER);
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

    // this is called to update the values used by shaders
    @Deprecated(since = "hook")
    public static void update() {
        if (MinecraftClient.getInstance().world != null) {
            effectWrappers.forEach(EffectWrapper::update);
        }
    }

    private static boolean useShader(float partialTicks, float ticks, ShaderWorld shader) {
        //RenderSystem.setShader(() -> (ShaderProgram)shader);
        currentShader = null;

        if (shader != null && PsychedelicraftClient.getConfig().visual.shader3DEnabled) {
            if (shader.isShaderActive())
                return true;

            if (shader.activate(partialTicks, ticks)) {
                currentShader = shader;
                return true;
            }
        }

        return false;
    }

    @Deprecated(since = "hook")
    public static void preRenderSky(float partialTicks) {
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

    @Deprecated(since = "unused")
    public static int getMCFBO() {
        MinecraftClient mc = MinecraftClient.getInstance();
        Framebuffer framebuffer = mc.getFramebuffer();

        return (framebuffer != null && framebuffer.fbo >= 0) ? framebuffer.fbo : 0;
    }
}

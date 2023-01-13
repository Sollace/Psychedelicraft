/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.program;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.PsycheShadowHelper;
import ivorius.psychedelicraft.entity.drugs.DrugProperties;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.Entity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.MathHelper;

import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FOG;

/**
 * Created by lukas on 26.02.14.
 */
@Deprecated
public class ShaderMain extends IvShaderInstance3D implements ShaderWorld {
    public boolean shouldDoShadows;
    public int shadowDepthTextureIndex;

    private boolean colorSafeModeIsEnabled;
    private boolean colorSafeModeIsForceEnabled;

    public ShaderMain(Logger logger) {
        super(logger);
    }

    @Override
    public boolean activate(float partialTicks, float ticks) {
        if (!useShader()) {
            return false;
        }

        MinecraftClient mc = MinecraftClient.getInstance();

        Entity renderEntity = mc.cameraEntity;

        setUniformInts("texture", 0);
        setUniformInts("lightmapTex", 1);

        setUniformFloats("ticks", ticks);
        setUniformInts("worldTime", (int) mc.world.getTime());
        setUniformInts("uses2DShaders", PsychedelicraftClient.getConfig().visual.shader2DEnabled ? 1 : 0);

        setUniformFloats("playerPos", (float) renderEntity.getX(), (float) renderEntity.getY(), (float) renderEntity.getZ());

        setTexture2DEnabled(GLStateProxy.isTextureEnabled(GLStateProxy.DEFAULT_TEXTURE));
        setLightmapEnabled(GLStateProxy.isTextureEnabled(GLStateProxy.LIGHTMAP_TEXTURE));
        setFogEnabled(GLStateProxy.isEnabled(GL_FOG));
        evaluateColorSafeMode();

        setDepthMultiplier(1.0f);
        setUseScreenTexCoords(false);
        setPixelSize(1.0f / mc.getWindow().getFramebufferWidth(), 1.0f / mc.getWindow().getFramebufferHeight());
        setFogMode(GL11.GL_LINEAR);
        setOverrideColor(null);

        float desaturation = 0.0f;
        float colorIntensification = 0.0f;
        float quickColorRotationStrength = 0.0f;
        float slowColorRotationStrength = 0.0f;
        float bigWaveStrength = 0.0f;
        float smallWaveStrength = 0.0f;
        float wiggleWaveStrength = 0.0f;
        float surfaceFractalStrength = 0.0f;
        float distantWorldDeformationStrength = 0.0f;
        float[] contrastColorization = new float[]{1f, 1f, 1f, 0f};
        float[] pulseColor = new float[]{1f, 1f, 1f, 0f};

        DrugProperties drugProperties = DrugProperties.of(renderEntity).orElse(null);

        if (drugProperties != null) {
            desaturation = drugProperties.hallucinationManager.getDesaturation(drugProperties, partialTicks);
            colorIntensification = drugProperties.hallucinationManager.getColorIntensification(drugProperties, partialTicks);
            quickColorRotationStrength = drugProperties.hallucinationManager.getQuickColorRotation(drugProperties, partialTicks);
            slowColorRotationStrength = drugProperties.hallucinationManager.getSlowColorRotation(drugProperties, partialTicks);
            bigWaveStrength = drugProperties.hallucinationManager.getBigWaveStrength(drugProperties, partialTicks);
            smallWaveStrength = drugProperties.hallucinationManager.getSmallWaveStrength(drugProperties, partialTicks);
            wiggleWaveStrength = drugProperties.hallucinationManager.getWiggleWaveStrength(drugProperties, partialTicks);
            surfaceFractalStrength = drugProperties.hallucinationManager.getSurfaceFractalStrength(drugProperties, partialTicks);
            distantWorldDeformationStrength = drugProperties.hallucinationManager.getDistantWorldDeformationStrength(drugProperties, partialTicks);
            drugProperties.hallucinationManager.applyContrastColorization(drugProperties, contrastColorization, partialTicks);
            drugProperties.hallucinationManager.applyPulseColor(drugProperties, pulseColor, partialTicks);
        }

        setUniformFloats("desaturation", desaturation);
        setUniformFloats("quickColorRotation", quickColorRotationStrength);
        setUniformFloats("slowColorRotation", slowColorRotationStrength);
        setUniformFloats("colorIntensification", colorIntensification);

        setUniformFloats("bigWaves", bigWaveStrength);
        setUniformFloats("smallWaves", smallWaveStrength);
        setUniformFloats("wiggleWaves", wiggleWaveStrength);
        setUniformFloats("distantWorldDeformation", distantWorldDeformationStrength);
        pulseColor[3] = MathHelper.clamp(pulseColor[3], 0, 1);
        setUniformFloats("pulses", pulseColor);
        if (surfaceFractalStrength > 0.0f) {
            registerFractals();
        }
        setUniformFloats("surfaceFractal", MathHelper.clamp(surfaceFractalStrength, 0, 1));
        contrastColorization[3] = MathHelper.clamp(contrastColorization[3], 0, 1);
        setUniformFloats("worldColorization", contrastColorization);

        if (shouldDoShadows) {
            setUniformMatrix("inverseViewMatrix", PsycheShadowHelper.getInverseViewMatrix(partialTicks));
            setUniformMatrix("sunMatrix", PsycheShadowHelper.getSunMatrix());
            setUniformInts("texShadow", 3);
            setUniformFloats("sunDepthRange", PsycheShadowHelper.getSunZNear(), PsycheShadowHelper.getSunZFar());
            setUniformFloats("shadowBias", PsycheShadowHelper.getShadowBias());
            RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 2, shadowDepthTextureIndex);
        }
        setUniformInts("doShadows", shouldDoShadows ? 1 : 0);

        return true;
    }

    @Override
    public void deactivate() {
        stopUsingShader();
    }

    public void registerFractals() {
        RenderSystem.setShaderTexture(GLStateProxy.LIGHTMAP_TEXTURE + 1, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        Sprite sprite = MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(Blocks.NETHER_PORTAL.getDefaultState());
        setUniformFloats("fractal0TexCoords", sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());
    }

    @Override
    public void setTexture2DEnabled(boolean enabled) {
        setUniformInts("texture2DEnabled", enabled ? 1 : 0);
    }

    @Override
    public void setLightmapEnabled(boolean enabled) {
        setUniformInts("lightmapEnabled", enabled ? 1 : 0);
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
        setUniformInts("glLightEnabled", enabled ? 1 : 0);
    }

    @Override
    public void setGLLight(int number, float x, float y, float z, float strength, float specular) {
        setUniformFloats("glLightPos" + number, x, y, z);
        setUniformFloats("glLightStrength" + number, strength, specular);
    }

    @Override
    public void setGLLightAmbient(float strength) {
        setUniformFloats("glLightAmbient", strength);
    }

    @Override
    public void setFogMode(int mode) {
        setUniformInts("fogMode", mode);
    }

    @Override
    public void setFogEnabled(boolean enabled) {
        setUniformInts("fogEnabled", enabled ? 1 : 0);
    }

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
    public void setBlendModeEnabled(boolean enabled) {
        evaluateColorSafeMode();
    }

    @Override
    public void setBlendFunc(int sFactor, int dFactor, int sFactorA, int dFactorA) {
        evaluateColorSafeMode();
    }

    public void evaluateColorSafeMode() {
        boolean enable = colorSafeModeIsForceEnabled || (GLStateProxy.isEnabled(GL_BLEND) && GLStateProxy.getBlendDFactor() != GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (colorSafeModeIsEnabled != enable)
            setUniformInts("colorSafeMode", enable ? 1 : 0);
        colorSafeModeIsEnabled = enable;
    }

    @Override
    public void setProjectShadows(boolean projectShadows) {
        if (shouldDoShadows) {
            setUniformInts("doShadows", projectShadows ? 1 : 0);
        }
    }

    @Override
    public void setForceColorSafeMode(boolean enable) {
        colorSafeModeIsForceEnabled = enable;
        evaluateColorSafeMode();
    }
}

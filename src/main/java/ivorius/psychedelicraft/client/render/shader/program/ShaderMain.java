/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.program;

import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.GLStateProxy;
import ivorius.psychedelicraft.client.render.PsycheShadowHelper;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
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

        setUniformInts("texture", 0);
        setUniformInts("lightmapTex", 1);

        setUniformFloats("ticks", ticks);
        setUniformInts("worldTime", (int) mc.world.getTime());
        setUniformInts("uses2DShaders", PsychedelicraftClient.getConfig().visual.shader2DEnabled ? 1 : 0);

        setUniformFloats("playerPos", (float) mc.player.getX(), (float) mc.player.getY(), (float) mc.player.getZ());

        setTexture2DEnabled(GLStateProxy.isTextureEnabled(GLStateProxy.DEFAULT_TEXTURE));
        setLightmapEnabled(GLStateProxy.isTextureEnabled(GLStateProxy.LIGHTMAP_TEXTURE));
        setFogEnabled(GLStateProxy.isEnabled(GL_FOG));
        evaluateColorSafeMode();

        setDepthMultiplier(1.0f);
        setUseScreenTexCoords(false);
        setPixelSize(1.0f / mc.getWindow().getFramebufferWidth(), 1.0f / mc.getWindow().getFramebufferHeight());
        setFogMode(GL11.GL_LINEAR);
        setOverrideColor(null);

        DrugProperties drugProperties = DrugProperties.of(mc.player);

        setUniformFloats("desaturation", drugProperties.getHallucinations().getDesaturation(partialTicks));
        setUniformFloats("quickColorRotation", drugProperties.getHallucinations().getQuickColorRotation(partialTicks));
        setUniformFloats("slowColorRotation", drugProperties.getHallucinations().getSlowColorRotation(partialTicks));
        setUniformFloats("colorIntensification", drugProperties.getHallucinations().getColorIntensification(partialTicks));
        setUniformFloats("bigWaves", drugProperties.getHallucinations().getBigWaveStrength(partialTicks));
        setUniformFloats("smallWaves", drugProperties.getHallucinations().getSmallWaveStrength(partialTicks));
        setUniformFloats("wiggleWaves", drugProperties.getHallucinations().getWiggleWaveStrength(partialTicks));
        setUniformFloats("distantWorldDeformation", drugProperties.getHallucinations().getDistantWorldDeformationStrength(partialTicks));
        setUniformFloats("pulses", drugProperties.getHallucinations().getPulseColor(partialTicks));
        setUniformFloats("worldColorization", drugProperties.getHallucinations().getContrastColorization(partialTicks));
        float surfaceFractalStrength = MathHelper.clamp(drugProperties.getHallucinations().getSurfaceFractalStrength(partialTicks), 0, 1);
        if (surfaceFractalStrength > 0) {
            registerFractals();
        }
        setUniformFloats("surfaceFractal", surfaceFractalStrength);

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

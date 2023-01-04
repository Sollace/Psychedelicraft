/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.shaders;

import ivorius.ivtoolkit.math.IvMathHelper;
import ivorius.ivtoolkit.rendering.IvDepthBuffer;
import ivorius.ivtoolkit.rendering.IvShaderInstance3D;
import ivorius.psychedelicraft.client.rendering.GLStateProxy;
import ivorius.psychedelicraft.client.rendering.PsycheShadowHelper;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FOG;

/**
 * Created by lukas on 26.02.14.
 */
@Deprecated(forRemoval = true, since = "Not necessary: the game implements shaders for us already")
public class ShaderMain extends IvShaderInstance3D implements ShaderWorld
{
    public boolean shouldDoShadows;
    public int shadowDepthTextureIndex;

    private boolean colorSafeModeIsEnabled;
    private boolean colorSafeModeIsForceEnabled;

    public ShaderMain(Logger logger)
    {
        super(logger);
    }

    @Override
    public boolean activate(float partialTicks, float ticks)
    {
        if (useShader())
        {
            Minecraft mc = Minecraft.getMinecraft();

            EntityLivingBase renderEntity = mc.renderViewEntity;
            DrugProperties drugProperties = DrugProperties.getDrugProperties(renderEntity);

            setUniformInts("texture", 0);
            setUniformInts("lightmapTex", 1);

            setUniformFloats("ticks", ticks);
            setUniformInts("worldTime", (int) mc.theWorld.getWorldTime());
            setUniformInts("uses2DShaders", PSRenderStates.shader2DEnabled ? 1 : 0);

            setUniformFloats("playerPos", (float) renderEntity.posX, (float) renderEntity.posY, (float) renderEntity.posZ);

            setTexture2DEnabled(GLStateProxy.isTextureEnabled(OpenGlHelper.defaultTexUnit));
            setLightmapEnabled(GLStateProxy.isTextureEnabled(OpenGlHelper.lightmapTexUnit));
            setFogEnabled(GLStateProxy.isEnabled(GL_FOG));
            evaluateColorSafeMode();

            setDepthMultiplier(1.0f);
            setUseScreenTexCoords(false);
            setPixelSize(1.0f / mc.displayWidth, 1.0f / mc.displayHeight);
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
            if (drugProperties != null)
            {
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
            pulseColor[3] = IvMathHelper.clamp(0.0f, pulseColor[3], 1.0f);
            setUniformFloats("pulses", pulseColor);
            if (surfaceFractalStrength > 0.0f)
                registerFractals();
            setUniformFloats("surfaceFractal", IvMathHelper.clamp(0.0f, surfaceFractalStrength, 1.0f));
            contrastColorization[3] = IvMathHelper.clamp(0.0f, contrastColorization[3], 1.0f);
            setUniformFloats("worldColorization", contrastColorization);

            if (shouldDoShadows)
            {
                setUniformMatrix("inverseViewMatrix", PsycheShadowHelper.getInverseViewMatrix(partialTicks));
                setUniformMatrix("sunMatrix", PsycheShadowHelper.getSunMatrix());

                setUniformInts("texShadow", 3);
                setUniformFloats("sunDepthRange", PsycheShadowHelper.getSunZNear(), PsycheShadowHelper.getSunZFar());
                setUniformFloats("shadowBias", PsycheShadowHelper.getShadowBias());
                IvDepthBuffer.bindTextureForSource(OpenGlHelper.lightmapTexUnit + 2, shadowDepthTextureIndex);
            }
            setUniformInts("doShadows", shouldDoShadows ? 1 : 0);

            return true;
        }

        return false;
    }

    @Override
    public void deactivate()
    {
        stopUsingShader();
    }

    public void registerFractals()
    {
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit + 1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);

        IIcon icon = Blocks.portal.getIcon(0, 0);
        float var4 = icon.getMinU();
        float var5 = icon.getMinV();
        float var6 = icon.getMaxU();
        float var7 = icon.getMaxV();

        setUniformFloats("fractal0TexCoords", var4, var5, var6, var7);
    }

    @Override
    public void setTexture2DEnabled(boolean enabled)
    {
        setUniformInts("texture2DEnabled", enabled ? 1 : 0);
    }

    @Override
    public void setLightmapEnabled(boolean enabled)
    {
        setUniformInts("lightmapEnabled", enabled ? 1 : 0);
    }

    @Override
    public void setOverrideColor(float[] color)
    {
        if (color != null)
        {
            setUniformFloats("overrideColor", color);
        }
        else
        {
            setUniformFloats("overrideColor", 1F, 1F, 1F, 1F);
        }
    }

    @Override
    public void setGLLightEnabled(boolean enabled)
    {
        setUniformInts("glLightEnabled", enabled ? 1 : 0);
    }

    @Override
    public void setGLLight(int number, float x, float y, float z, float strength, float specular)
    {
        setUniformFloats("glLightPos" + number, x, y, z);
        setUniformFloats("glLightStrength" + number, strength, specular);
    }

    @Override
    public void setGLLightAmbient(float strength)
    {
        setUniformFloats("glLightAmbient", strength);
    }

    @Override
    public void setFogMode(int mode)
    {
        setUniformInts("fogMode", mode);
    }

    @Override
    public void setFogEnabled(boolean enabled)
    {
        setUniformInts("fogEnabled", enabled ? 1 : 0);
    }

    @Override
    public void setDepthMultiplier(float depthMultiplier)
    {
        setUniformFloats("depthMultiplier", depthMultiplier);
    }

    @Override
    public void setUseScreenTexCoords(boolean enabled)
    {
        setUniformInts("useScreenTexCoords", enabled ? 1 : 0);
    }

    @Override
    public void setPixelSize(float pixelWidth, float pixelHeight)
    {
        setUniformFloats("pixelSize", pixelWidth, pixelHeight);
    }

    @Override
    public void setBlendModeEnabled(boolean enabled)
    {
        evaluateColorSafeMode();
    }

    @Override
    public void setBlendFunc(int sFactor, int dFactor, int sFactorA, int dFactorA)
    {
        evaluateColorSafeMode();
    }

    public void evaluateColorSafeMode()
    {
        boolean enable = colorSafeModeIsForceEnabled || (GLStateProxy.isEnabled(GL_BLEND) && GLStateProxy.getBlendDFactor() != GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (colorSafeModeIsEnabled != enable)
            setUniformInts("colorSafeMode", enable ? 1 : 0);
        colorSafeModeIsEnabled = enable;
    }

    @Override
    public void setProjectShadows(boolean projectShadows)
    {
        if (shouldDoShadows)
        {
            setUniformInts("doShadows", projectShadows ? 1 : 0);
        }
    }

    @Override
    public void setForceColorSafeMode(boolean enable)
    {
        colorSafeModeIsForceEnabled = enable;
        evaluateColorSafeMode();
    }
}

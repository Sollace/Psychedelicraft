/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering.shaders;

/**
 * Created by lukas on 03.03.14.
 */
@Deprecated(forRemoval = true, since = "Not necessary: the game implements shaders for us already")
public interface ShaderWorld
{
    boolean isShaderActive();

    boolean activate(float partialTicks, float ticks);

    void deactivate();

    void setTexture2DEnabled(boolean enabled);

    void setLightmapEnabled(boolean enabled);

    void setOverrideColor(float[] color);

    void setGLLightEnabled(boolean enabled);

    void setGLLight(int number, float x, float y, float z, float strength, float specular);

    void setGLLightAmbient(float strength);

    void setFogMode(int mode);

    void setFogEnabled(boolean enabled);

    void setDepthMultiplier(float depthMultiplier);

    void setUseScreenTexCoords(boolean enabled);

    void setPixelSize(float pixelWidth, float pixelHeight);

    void setBlendModeEnabled(boolean enabled);

    void setBlendFunc(int sFactor, int dFactor, int sFactorA, int dFactorA);

    void setProjectShadows(boolean projectShadows);

    void setForceColorSafeMode(boolean enable);
}

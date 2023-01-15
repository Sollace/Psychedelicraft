/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.shader.legacy.program;

/**
 * Created by lukas on 03.03.14.
 */
@Deprecated(forRemoval = true, since = "Not necessary: the game implements shaders for us already")
public interface ShaderWorld {
    boolean isShaderActive();

    boolean activate(float partialTicks, float ticks);

    void deactivate();

    default void setTexture2DEnabled(boolean enabled) {}

    default void setLightmapEnabled(boolean enabled) {}

    default void setOverrideColor(float[] color) {}

    default void setGLLightEnabled(boolean enabled) {}

    default void setGLLight(int number, float x, float y, float z, float strength, float specular) {}

    default void setGLLightAmbient(float strength) {}

    default void setFogMode(int mode) {}

    default void setFogEnabled(boolean enabled) {}

    default void setDepthMultiplier(float depthMultiplier) {}

    default void setBlendModeEnabled(boolean enabled) {}

    default void setBlendFunc(int sFactor, int dFactor, int sFactorA, int dFactorA) {}

    default void setProjectShadows(boolean projectShadows) {}

    default void setForceColorSafeMode(boolean enable) {}
}

package ivorius.psychedelicraft.client.rendering;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import java.util.Arrays;

/**
 * Created by lukas on 16.11.14.
 */
@Deprecated
public class GLStateProxy
{
    private static int activeTextureUnit = OpenGlHelper.defaultTexUnit;
    private static final boolean[] enabledTextures = new boolean[40];

    private static boolean fogEnabled = false;

    private static boolean blendEnabled = false;
    private static int blendSFactor = 0;
    private static int blendDFactor = 0;
    private static int blendSFactorA = 0;
    private static int blendDFactorA = 0;

    static
    {
        Arrays.fill(enabledTextures, true);
    }

    public static boolean isEnabled(int cap)
    {
        switch (cap)
        {
            case GL11.GL_FOG:
                return fogEnabled;
            case GL11.GL_BLEND:
                return blendEnabled;
            case GL11.GL_TEXTURE_2D:
                return isTextureEnabled(activeTextureUnit);
        }

        return false;
    }

    public static void setEnabled(int cap, boolean enabled)
    {
        switch (cap)
        {
            case GL11.GL_FOG:
                fogEnabled = enabled;
                break;
            case GL11.GL_BLEND:
                blendEnabled = enabled;
                break;
            case GL11.GL_TEXTURE_2D:
                setTextureEnabled(activeTextureUnit, enabled);
        }
    }

    public static boolean isTextureEnabled(int textureUnit)
    {
        textureUnit -= GlStateManager.TEXTURE_COUNT;

        return textureUnit >= 0 && textureUnit < enabledTextures.length && enabledTextures[textureUnit];
    }

    public static void setTextureEnabled(int textureUnit, boolean enabled)
    {
        textureUnit -= GlStateManager.TEXTURE_COUNT;

        if (textureUnit >= 0 && textureUnit < enabledTextures.length)
            enabledTextures[textureUnit] = enabled;
    }

    public static void setActiveTextureUnit(int textureUnit)
    {
        activeTextureUnit = textureUnit;
    }

    public static int getActiveTextureUnit()
    {
        return activeTextureUnit;
    }

    public static void glBlendFunc(int sFactor, int dFactor, int sFactorA, int dFactorA)
    {
        blendSFactor = sFactor;
        blendDFactor = dFactor;
        blendSFactorA = sFactorA;
        blendDFactorA = dFactorA;
    }

    public static int getBlendSFactor()
    {
        return blendSFactor;
    }

    public static int getBlendDFactor()
    {
        return blendDFactor;
    }

    public static int getBlendSFactorA()
    {
        return blendSFactorA;
    }

    public static int getBlendDFactorA()
    {
        return blendDFactorA;
    }
}

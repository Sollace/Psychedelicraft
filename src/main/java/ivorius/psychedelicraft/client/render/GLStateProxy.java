package ivorius.psychedelicraft.client.render;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 16.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class GLStateProxy {
    public static final int DEFAULT_TEXTURE = GlConst.GL_TEXTURE0;
    public static final int LIGHTMAP_TEXTURE = GlConst.GL_TEXTURE1;

    private static final TextureManager TEXURE_MANAGER = MinecraftClient.getInstance().getTextureManager();

    private static boolean usesScreenTexCoords;
    private static boolean resolutionSet;
    private static final float[] resolution = new float[2];

    public static boolean isEnabled(int cap) {
        RenderSystem.assertOnRenderThread();
        return GL11.glIsEnabled(cap);
    }

    public static void setResolution(float width, float height) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> _setResolution(width, height));
        } else {
            _setResolution(width, height);
        }
    }

    private static void _setResolution(float width, float height) {
        resolution[0] = 1F / width;
        resolution[1] = 1F / height;
        resolutionSet = true;
    }

    public static float[] getResolution() {
        RenderSystem.assertOnRenderThread();
        if (!resolutionSet) {
            Window window = MinecraftClient.getInstance().getWindow();
            resolution[0] = 1F / window.getFramebufferWidth();
            resolution[1] = 1F / window.getFramebufferHeight();
        }
        return resolution;
    }

    public static void clearResolution() {
        RenderSystem.assertOnRenderThread();
        resolutionSet = false;
    }

    public static void enableTexCoords() {
        RenderSystem.assertOnRenderThread();
        usesScreenTexCoords = true;
    }

    public static void disableScreenTexCoords() {
        RenderSystem.assertOnRenderThread();
        usesScreenTexCoords = false;
    }

    public static boolean getUsesScreenTexCoords() {
        return usesScreenTexCoords;
    }

    public static boolean isTextureEnabled(int textureUnit) {
        // TODO: (Sollace) accessor to GlStateManager.TEXTURES[textureUnit - GlConst.GL_TEXTURE0].capState;
        return true;
    }

    public static int getBlendDFactor() {
        // TODO: (Sollace) accessor to GlStateManager.BLEND.dstFactorRGB;
        return 0;
    }

    public static void setShadeMode(ShadeMode shadeMode) {
        // Shade mode is deprecated
        /*if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> _setShadeMode(shadeMode));
        } else {
            _setShadeMode(shadeMode);
        }*/
    }

    private static void _setShadeMode(ShadeMode shadeMode) {
        // GL11.glShadeModel(shadeMode == ShadeMode.FLAT ? GL11.GL_FLAT : GL11.GL_SMOOTH);
    }

    public static void copyTexture(int offsetX, int offsetY, int x, int y, int width, int height) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> _copyTexture(offsetX, offsetY, x, y, width, height));
        } else {
            _copyTexture(offsetX, offsetY, x, y, width, height);
        }
    }

    private static void _copyTexture(int offsetX, int offsetY, int x, int y, int width, int height) {
        GlStateManager._glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, offsetX, offsetY, x, y, width, height);
    }

    public static int getTextureId(Identifier texture) {
        RenderSystem.assertOnRenderThread();
        TEXURE_MANAGER.bindTexture(texture); // Allocate texture. MOJANG!
        return TEXURE_MANAGER.getTexture(texture).getGlId();
    }

    public enum ShadeMode {
        FLAT,
        SMOOTH
    }
}

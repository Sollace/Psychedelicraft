package ivorius.psychedelicraft.client.rendering;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

/**
 * Created by lukas on 16.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class GLStateProxy {
    public static final int DEFAULT_TEXTURE = GlConst.GL_TEXTURE0;
    public static final int LIGHTMAP_TEXTURE = GlConst.GL_TEXTURE1;

    private static final TextureManager TEXURE_MANAGER = MinecraftClient.getInstance().getTextureManager();

    public static boolean isEnabled(int cap) {
        RenderSystem.assertOnRenderThread();
        return GL11.glIsEnabled(cap);
    }

    public static boolean isTextureEnabled(int textureUnit) {
        // TODO: accessor to GlStateManager.TEXTURES[textureUnit - GlConst.GL_TEXTURE0].capState;
        return true;
    }

    public static int getBlendDFactor() {
        // TODO: accessor to GlStateManager.BLEND.dstFactorRGB;
        return 0;
    }

    public static void copyTexture(int offsetX, int offsetY, int x, int y, int width, int height) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, offsetX, offsetY, x, y, width, height);
    }

    public static int getTextureId(Identifier texture) {
        RenderSystem.assertOnRenderThread();
        TEXURE_MANAGER.bindTexture(texture); // Allocate texture. MOJANG!
        return TEXURE_MANAGER.getTexture(texture).getGlId();
    }
}

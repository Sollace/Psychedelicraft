/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;

import ivorius.ivtoolkit.rendering.IvRenderHelper;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import ivorius.psychedelicraft.entities.EntityRealityRift;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by lukas on 03.03.14.
 */
public class RenderRealityRift extends EntityRenderer<EntityRealityRift> {
    public ResourceLocation[] zeroScreenTexture;
    public ResourceLocation zeroCenterTexture;

    public RenderRealityRift(EntityRendererFactory.Context ctx)
    {
        super(ctx);
        zeroScreenTexture = new ResourceLocation[8];
        for (int i = 0; i < zeroScreenTexture.length; i++)
        {
            zeroScreenTexture[i] = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "zeroScreen" + i + ".png");
        }

        zeroCenterTexture = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "zeroCenter.png");
    }

    @Override
    public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9)
    {
        EntityRealityRift entityRealityRift = (EntityRealityRift) var1;
        GL11.glPushMatrix();
        GL11.glTranslated(var2, var4 + var1.height * 0.5, var6);

        GL11.glPushMatrix();
        float riftSize = entityRealityRift.visualRiftSize;
        float visualRiftSize = riftSize < 0.01f ? (riftSize * 10.0f) : (0.1f + (riftSize - 0.01f) * 0.1f);
        GL11.glScaled(visualRiftSize, visualRiftSize, visualRiftSize);
        float critStatus = entityRealityRift.getCriticalStatus();
        renderRift(var9, var1.ticksExisted + var9 + (critStatus * critStatus * 3000.0f));

        GL11.glScaled(5.0, 5.0, 5.0);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
//        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        bindTexture(zeroCenterTexture);
        IvRenderHelper.renderParticle(Tessellator.instance, var9, 1.0f);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();

        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity var1)
    {
        return null;
    }

    public void renderRift(float partialTicks, float ticks)
    {
        int textureChosen = MathHelper.floor_double(ticks * 0.5f);
        Random thisTextureMov = new Random(textureChosen);
        bindTexture(zeroScreenTexture[textureChosen % 8]);
        PSRenderStates.setUseScreenTexCoords(true);
        float pixelsX = 140.0f / 2.0f;
        float pixelsY = 224.0f / 2.0f;
        PSRenderStates.setPixelSize(1.0f / pixelsX, -1.0f / pixelsY);
        GL11.glTexCoord2f(thisTextureMov.nextInt(10) * 0.1f * pixelsX, thisTextureMov.nextInt(8) * 0.125f * pixelsY);
        renderLightsScreen(ticks, 1.0f, 0xffffffff, 20);
        PSRenderStates.setScreenSizeDefault();
        PSRenderStates.setUseScreenTexCoords(false);
    }

    public static void renderLightsScreen(float ticks, float alpha, int color, int number)
    {
        float width = 2.5f;

        Tessellator var3 = Tessellator.instance;

        RenderHelper.disableStandardItemLighting();
        float var4 = ticks / 200.0F;

        Random var6 = new Random(432L);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
//        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(false);
        GL11.glPushMatrix();

        for (int var7 = 0; (float) var7 < number; ++var7)
        {
            float xLogFunc = (((float) var7 / number * 28493.0f + ticks) / 10.0f) % 20.0f;
            if (xLogFunc > 10.0f)
            {
                xLogFunc = 20.0f - xLogFunc;
            }

            float yLogFunc = 1.0f / (1.0f + (float) Math.pow(2.71828f, -0.8f * xLogFunc) * ((1.0f / 0.01f) - 1.0f));

            float lightAlpha = yLogFunc;

            if (lightAlpha > 0.01f)
            {
                GL11.glRotatef(var6.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(var6.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(var6.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(var6.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(var6.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(var6.nextFloat() * 360.0F + var4 * 90.0F, 0.0F, 0.0F, 1.0F);
                var3.startDrawing(6);
                float var8 = var6.nextFloat() * 20.0F + 5.0F;
                float var9 = var6.nextFloat() * 2.0F + 1.0F;
                var3.setColorRGBA_I(color, (int) (255.0F * alpha * lightAlpha));
                var3.addVertex(0.0D, 0.0D, 0.0D);
                var3.setColorRGBA_I(color, 0);
                var3.addVertex(-width * (double) var9, var8, (-0.5F * var9));
                var3.addVertex(width * (double) var9, var8, (-0.5F * var9));
                var3.addVertex(0.0D, var8, (1.0F * var9));
                var3.addVertex(-width * (double) var9, var8, (-0.5F * var9));
                var3.draw();
            }
        }

        GL11.glPopMatrix();
        GL11.glDepthMask(true);
//        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        RenderHelper.enableStandardItemLighting();
    }
}

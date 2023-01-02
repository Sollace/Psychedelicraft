/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.rendering;

import ivorius.ivtoolkit.math.IvMathHelper;
import ivorius.ivtoolkit.rendering.Iv2DScreenEffect;
import ivorius.ivtoolkit.rendering.IvOpenGLTexturePingPong;
import ivorius.ivtoolkit.rendering.IvRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by lukas on 26.02.14.
 */
public class EffectLensFlare implements Iv2DScreenEffect
{
    public float[] sunFlareSizes;
    public float[] sunFlareInfluences;
    public Identifier[] sunFlareTextures;

    public Identifier sunBlindnessTexture;
    public float sunFlareIntensity;

    public float actualSunAlpha = 0.0f;

    public void updateLensFlares()
    {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        EntityLivingBase renderEntity = mc.renderViewEntity;

        if (renderEntity != null && world != null)
        {
            float sunSizeRadians = -5.0f / 180.0f * 3.1315926f;
            float sunWidth = 20.0f;
            float sunRadians = world.getCelestialAngleRadians(1.0f);

            Vec3 sunVecTopLeft = Vec3.createVectorHelper(-MathHelper.sin(sunRadians - sunSizeRadians) * 120.0f, MathHelper.cos(sunRadians - sunSizeRadians) * 120.0f, -sunWidth);
            Vec3 sunVecTopRight = Vec3.createVectorHelper(-MathHelper.sin(sunRadians - sunSizeRadians) * 120.0f, MathHelper.cos(sunRadians - sunSizeRadians) * 120.0f, sunWidth);
            Vec3 sunVecBottomLeft = Vec3.createVectorHelper(-MathHelper.sin(sunRadians + sunSizeRadians) * 120.0f, MathHelper.cos(sunRadians + sunSizeRadians) * 120.0f, -sunWidth);
            Vec3 sunVecBottomRight = Vec3.createVectorHelper(-MathHelper.sin(sunRadians + sunSizeRadians) * 120.0f, MathHelper.cos(sunRadians + sunSizeRadians) * 120.0f, sunWidth);

            Vec3 playerPos = renderEntity.getPosition(1.0f);

            //Need copies, because the raytracer edits these
            Vec3 playerPositionLT = Vec3.createVectorHelper(playerPos.xCoord, playerPos.yCoord, playerPos.zCoord);
            Vec3 playerPositionLB = Vec3.createVectorHelper(playerPos.xCoord, playerPos.yCoord, playerPos.zCoord);
            Vec3 playerPositionRT = Vec3.createVectorHelper(playerPos.xCoord, playerPos.yCoord, playerPos.zCoord);
            Vec3 playerPositionRB = Vec3.createVectorHelper(playerPos.xCoord, playerPos.yCoord, playerPos.zCoord);

            Vec3 sunPosTopLeft = playerPositionLT.addVector(sunVecTopLeft.xCoord, sunVecTopLeft.yCoord, sunVecTopLeft.zCoord);
            Vec3 sunPosTopRight = playerPositionRT.addVector(sunVecTopRight.xCoord, sunVecTopRight.yCoord, sunVecTopRight.zCoord);
            Vec3 sunPosBottomLeft = playerPositionLB.addVector(sunVecBottomLeft.xCoord, sunVecBottomLeft.yCoord, sunVecBottomLeft.zCoord);
            Vec3 sunPosBottomRight = playerPositionRB.addVector(sunVecBottomRight.xCoord, sunVecBottomRight.yCoord, sunVecBottomRight.zCoord);

            // Raytraceblocks
            MovingObjectPosition sunTopLeft = world.func_147447_a(playerPositionLT, sunPosTopLeft, true, true, true);
            MovingObjectPosition sunTopRight = world.func_147447_a(playerPositionRT, sunPosTopRight, true, true, true);
            MovingObjectPosition sunBottomLeft = world.func_147447_a(playerPositionLB, sunPosBottomLeft, true, true, true);
            MovingObjectPosition sunBottomRight = world.func_147447_a(playerPositionRB, sunPosBottomRight, true, true, true);

            float newSunAlpha = (1.0F - world.getRainStrength(1.0f)) * ((sunTopLeft == null ? 0.25f : 0.0f) + (sunTopRight == null ? 0.25f : 0.0f) + (sunBottomLeft == null ? 0.25f : 0.0f) + (sunBottomRight == null ? 0.25f : 0.0f));
            actualSunAlpha = IvMathHelper.nearValue(actualSunAlpha, newSunAlpha, 0.1f, 0.01f);
            if (actualSunAlpha > 1.0f)
            {
                actualSunAlpha = 1.0f;
            }
        }
    }

    public void renderLensFlares(int screenWidth, int screenHeight, float partialTicks)
    {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        EntityLivingBase renderEntity = mc.renderViewEntity;

        float sunRadians = world.getCelestialAngleRadians(partialTicks);

        Vector3f sunVecCenter = new Vector3f(-MathHelper.sin(sunRadians) * 120.0f, MathHelper.cos(sunRadians) * 120.0f, 0.0f);

        if (actualSunAlpha > 0.0f)
        {
            float genSize = screenWidth > screenHeight ? screenWidth : screenHeight;

            Vector3f sunPositionOnScreen = PsycheMatrixHelper.projectPointCurrentView(sunVecCenter, partialTicks);

            Vector3f normSunPos = new Vector3f();
            sunPositionOnScreen.normalise(normSunPos);
            float xDist = normSunPos.x * screenWidth;
            float yDist = normSunPos.y * screenHeight;

            Vec3 color = world.getFogColor(1.0f);

            if (sunPositionOnScreen.z > 0.0f)
            {
                float alpha = sunPositionOnScreen.z;
                if (alpha > 1.0f)
                {
                    alpha = 1.0f;
                }

                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(false);
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                Tessellator var3 = Tessellator.instance;

                float screenCenterX = screenWidth * 0.5f;
                float screenCenterY = screenHeight * 0.5f;

                for (int i = 0; i < sunFlareSizes.length; i++)
                {
                    float flareSizeHalf = sunFlareSizes[i] * genSize * 0.5f;
                    float flareCenterX = screenCenterX + xDist * sunFlareInfluences[i];
                    float flareCenterY = screenCenterY + yDist * sunFlareInfluences[i];

                    GL11.glColor4f((float) color.xCoord - 0.1f, (float) color.yCoord - 0.1f, (float) color.zCoord - 0.1f, (alpha * i == 8 ? 1.0f : 0.5f) * actualSunAlpha * sunFlareIntensity);

                    mc.renderEngine.bindTexture(sunFlareTextures[i]);
                    var3.startDrawingQuads();
                    var3.addVertexWithUV(flareCenterX - flareSizeHalf, flareCenterY + flareSizeHalf, -90.0D, 0.0D, 1.0D);
                    var3.addVertexWithUV(flareCenterX + flareSizeHalf, flareCenterY + flareSizeHalf, -90.0D, 1.0D, 1.0D);
                    var3.addVertexWithUV(flareCenterX + flareSizeHalf, flareCenterY - flareSizeHalf, -90.0D, 1.0D, 0.0D);
                    var3.addVertexWithUV(flareCenterX - flareSizeHalf, flareCenterY - flareSizeHalf, -90.0D, 0.0D, 0.0D);
                    var3.draw();
                }

                // Looks weird because of a hard edge... :|
                float genDist = 1.0f - (normSunPos.x * normSunPos.x + normSunPos.y * normSunPos.y);
                float blendingSize = (genDist - 0.1f) * sunFlareIntensity * 250.0f * genSize;

                if (blendingSize > 0.0f)
                {
                    float blendingSizeHalf = blendingSize * 0.5f;
                    float blendCenterX = screenCenterX + xDist;
                    float blendCenterY = screenCenterY + yDist;

                    float blendAlpha = blendingSize / genSize / 150f;
                    if (blendAlpha > 1.0f)
                    {
                        blendAlpha = 1.0f;
                    }

                    GL11.glColor4f((float) color.xCoord - 0.1f, (float) color.yCoord - 0.1f, (float) color.zCoord - 0.1f, blendAlpha * actualSunAlpha);
                    GL11.glEnable(GL11.GL_BLEND);
                    OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

                    mc.renderEngine.bindTexture(sunBlindnessTexture);
                    var3.startDrawingQuads();
                    var3.addVertexWithUV(blendCenterX - blendingSizeHalf, blendCenterY + blendingSizeHalf, -90.0D, 0.0D, 1.0D);
                    var3.addVertexWithUV(blendCenterX + blendingSizeHalf, blendCenterY + blendingSizeHalf, -90.0D, 1.0D, 1.0D);
                    var3.addVertexWithUV(blendCenterX + blendingSizeHalf, blendCenterY - blendingSizeHalf, -90.0D, 1.0D, 0.0D);
                    var3.addVertexWithUV(blendCenterX - blendingSizeHalf, blendCenterY - blendingSizeHalf, -90.0D, 0.0D, 0.0D);
                    var3.draw();
                }

                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        // Reset
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public boolean shouldApply(float ticks)
    {
        return sunFlareIntensity > 0.0f;
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float ticks, IvOpenGLTexturePingPong pingPong)
    {
        pingPong.pingPong();
        IvRenderHelper.drawRectFullScreen(screenWidth, screenHeight);

        renderLensFlares(screenWidth, screenHeight, 1.0f);
    }

    @Override
    public void destruct()
    {

    }
}

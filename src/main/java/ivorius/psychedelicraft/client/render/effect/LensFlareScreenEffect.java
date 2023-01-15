/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.MCColorHelper;
import ivorius.psychedelicraft.client.render.PsycheMatrixHelper;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

/**
 * Created by lukas on 26.02.14.
 */
public class EffectLensFlare implements ScreenEffect {
    private static final float SUN_RADIANS = 5F * MathHelper.RADIANS_PER_DEGREE;
    private static final float SUN_WIDTH = 20;

    public static final float[] SUN_FLARE_SIZES = new float[]{0.15f, 0.24f, 0.12f, 0.036f, 0.06f, 0.048f, 0.006f, 0.012f, 0.5f, 0.09f, 0.036f, 0.09f, 0.06f, 0.05f, 0.6f};
    public static final float[] SUN_FLARE_INFLUENCES = new float[]{-1.3f, -2.0f, 0.2f, 0.4f, 0.25f, -0.25f, -0.7f, -1.0f, 1.0f, 1.4f, -1.31f, -1.2f, -1.5f, -1.55f, -3.0f};
    public static final Identifier[] SUN_FLARE_TEXTURES = IntStream.range(0, SUN_FLARE_SIZES.length)
            .mapToObj(i -> Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "flare" + i + ".png"))
            .toArray(Identifier[]::new);

    public Identifier sunBlindnessTexture = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "sun_blindness.png");
    public float sunFlareIntensity;

    public float actualSunAlpha = 0.0f;

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Override
    public boolean shouldApply(float ticks) {
        return sunFlareIntensity > 0;
    }

    @Override
    public void apply(int screenWidth, int screenHeight, float tickDelta, @Nullable PingPong pingPong) {
        if (pingPong != null) {
            pingPong.pingPong();
            MCColorHelper.drawScreen(screenWidth, screenHeight);
        }

        if (actualSunAlpha <= 0) {
            return;
        }

        World world = client.world;
        Entity renderEntity = client.getCameraEntity();

        float genSize = screenWidth > screenHeight ? screenWidth : screenHeight;
        float sunRadians = world.getSkyAngleRadians(tickDelta);
        Vector3f sunVecCenter = new Vector3f(
                -MathHelper.sin(sunRadians) * 120,
                MathHelper.cos(sunRadians) * 120,
                0
        );
        Vector3f sunPositionOnScreen = PsycheMatrixHelper.projectPointCurrentView(sunVecCenter, tickDelta);

        Vector3f normSunPos = new Vector3f();
        sunPositionOnScreen.normalize(normSunPos);
        float xDist = normSunPos.x * screenWidth;
        float yDist = normSunPos.y * screenHeight;

        int colorValue = world.getBiome(renderEntity.getBlockPos()).value().getFogColor();
        int fogRed = NativeImage.getRed(colorValue);
        int fogGreen = NativeImage.getGreen(colorValue);
        int fogBlue = NativeImage.getBlue(colorValue);

        if (sunPositionOnScreen.z > 0) {
            float alpha = Math.min(1, sunPositionOnScreen.z);

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ONE, DstFactor.ZERO);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            float screenCenterX = screenWidth * 0.5f;
            float screenCenterY = screenHeight * 0.5f;

            for (int i = 0; i < SUN_FLARE_SIZES.length; i++) {
                float flareSizeHalf = SUN_FLARE_SIZES[i] * genSize * 0.5f;
                float flareCenterX = screenCenterX + xDist * SUN_FLARE_INFLUENCES[i];
                float flareCenterY = screenCenterY + yDist * SUN_FLARE_INFLUENCES[i];

                RenderSystem.setShaderColor(fogRed - 0.1F, fogGreen - 0.1F, fogBlue - 0.1F, (alpha * i == 8 ? 1F : 0.5F) * actualSunAlpha * sunFlareIntensity);
                RenderSystem.setShaderTexture(0, SUN_FLARE_TEXTURES[i]);

                buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
                buffer.vertex(flareCenterX - flareSizeHalf, flareCenterY + flareSizeHalf, -90.0D).texture(0, 1).next();
                buffer.vertex(flareCenterX + flareSizeHalf, flareCenterY + flareSizeHalf, -90.0D).texture(1, 1).next();
                buffer.vertex(flareCenterX + flareSizeHalf, flareCenterY - flareSizeHalf, -90.0D).texture(1, 0).next();
                buffer.vertex(flareCenterX - flareSizeHalf, flareCenterY - flareSizeHalf, -90.0D).texture(0, 0).next();
                tessellator.draw();
            }

            // Looks weird because of a hard edge... :|
            float genDist = 1.0f - (normSunPos.x * normSunPos.x + normSunPos.y * normSunPos.y);
            float blendingSize = (genDist - 0.1f) * sunFlareIntensity * 250.0f * genSize;

            if (blendingSize > 0.0f) {
                float blendingSizeHalf = blendingSize * 0.5f;
                float blendCenterX = screenCenterX + xDist;
                float blendCenterY = screenCenterY + yDist;
                float blendAlpha = Math.min(1, blendingSize / genSize / 150f);

                RenderSystem.setShaderColor(fogRed - 0.1F, fogGreen - 0.1F, fogBlue - 0.1F, blendAlpha * actualSunAlpha);
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ONE, DstFactor.ZERO);
                RenderSystem.setShaderTexture(0, sunBlindnessTexture);
                buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
                buffer.vertex(blendCenterX - blendingSizeHalf, blendCenterY + blendingSizeHalf, -90).texture(0, 1).next();
                buffer.vertex(blendCenterX + blendingSizeHalf, blendCenterY + blendingSizeHalf, -90).texture(1, 1).next();
                buffer.vertex(blendCenterX + blendingSizeHalf, blendCenterY - blendingSizeHalf, -90).texture(1, 0).next();
                buffer.vertex(blendCenterX - blendingSizeHalf, blendCenterY - blendingSizeHalf, -90).texture(0, 0).next();
                tessellator.draw();
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        // Reset
        RenderSystem.disableBlend();
    }

    public void updateLensFlares() {
        World world = client.world;
        Entity renderEntity = client.getCameraEntity();

        float tickDelta = 1;

        if (renderEntity != null && world != null) {
            float sunRadians = world.getSkyAngleRadians(tickDelta);

            float sunMinX = MathHelper.sin(sunRadians + SUN_RADIANS) * 120;
            float sunMinY = MathHelper.cos(sunRadians + SUN_RADIANS) * 120;

            float sunMaxX = MathHelper.sin(sunRadians - SUN_RADIANS) * 120;
            float sunMaxY = MathHelper.cos(sunRadians - SUN_RADIANS) * 120;

            float newSunAlpha = (1 - world.getRainGradient(tickDelta)) * (
                      (checkIntersection(world, renderEntity, tickDelta, new Vec3d(-sunMinX, sunMinY, -SUN_WIDTH)) ? 0.25F : 0)
                    + (checkIntersection(world, renderEntity, tickDelta, new Vec3d(-sunMinX, sunMinY, SUN_WIDTH)) ? 0.25F : 0)
                    + (checkIntersection(world, renderEntity, tickDelta, new Vec3d(-sunMaxX, sunMaxY, -SUN_WIDTH)) ? 0.25F : 0)
                    + (checkIntersection(world, renderEntity, tickDelta, new Vec3d(-sunMaxX, sunMaxY, SUN_WIDTH)) ? 0.25F : 0)
            );
            actualSunAlpha = Math.min(1, MathUtils.nearValue(actualSunAlpha, newSunAlpha, 0.1f, 0.01f));
        }
    }

    private boolean checkIntersection(World world, Entity entity, float tickDelta, Vec3d offset) {
        Vec3d start = entity.getCameraPosVec(tickDelta);
        return world.raycast(new RaycastContext(start, start.add(offset),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.ANY, entity))
                .getType() != Type.MISS;
    }
}

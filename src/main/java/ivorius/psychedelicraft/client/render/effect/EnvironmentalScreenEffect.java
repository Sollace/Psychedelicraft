/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.MeteorlogicalUtil;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap.Type;

/**
 * @author Sollace
 * @since 15 Jan 2023
 */
public class EnvironmentalScreenEffect implements ScreenEffect {
    private static final Identifier HURT_OVERLAY = Psychedelicraft.id("textures/environment/hurt_overlay.png");

    private float experiencedHealth = 5F;

    private int timeScreenWet;
    private boolean wasInWater;
    private boolean wasInRain;

    private float currentHeat;

    public float getHeatDistortion() {
        return wasInWater ? 0 : MathHelper.clamp(((currentHeat - 1) * 0.0015f), 0, 0.01F);
    }

    public float getWaterDistortion() {
        return wasInWater ? 0.025F : 0;
    }

    public float getWaterScreenDistortion() {
        return timeScreenWet > 0 && !wasInWater ? Math.min(1, timeScreenWet / 80F) : 0;
    }

    @Override
    public void update(float tickDelta) {

        PlayerEntity entity = MinecraftClient.getInstance().player;

        if (PsychedelicraftClient.getConfig().visual.hurtOverlayEnabled) {
            experiencedHealth = MathUtils.nearValue(experiencedHealth, entity.getHealth(), 0.01f, 0.01f);
        }

        wasInWater = entity.world.getFluidState(new BlockPos(entity.getEyePos())).isIn(FluidTags.WATER);
        wasInRain = entity.world.getRainGradient(tickDelta) > 0
                && entity.world.getBiome(entity.getBlockPos()).value().getDownfall() > 0
                && entity.world.getTopPosition(Type.MOTION_BLOCKING, entity.getBlockPos()).getY() <= entity.getY();

        if (PsychedelicraftClient.getConfig().visual.waterOverlayEnabled) {
            timeScreenWet--;

            if (wasInWater) {
                timeScreenWet += 20;
            }
            if (wasInRain) {
                timeScreenWet += 4;
            }

            timeScreenWet = MathHelper.clamp(timeScreenWet, 0, 100);
        }

        BlockPos pos = entity.getBlockPos();
        float newHeat = wasInWater ? 0 : entity.getWorld().getBiome(pos).value().getTemperature();
        if (!entity.getWorld().getDimension().hasCeiling()) {
            newHeat *= MeteorlogicalUtil.getSunIntensity(entity.getWorld());
            newHeat *= MeteorlogicalUtil.getSkyLightIntensity(entity.getWorld(), BlockPos.ofFloored(entity.getEyePos()));
        }

        this.currentHeat = MathUtils.nearValue(currentHeat, newHeat, 0.01f, 0.01f);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int screenWidth, int screenHeight, float ticks, PingPong pingPong) {
        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity entity = client.player;
        DrugProperties properties = DrugProperties.of(entity);

        float pulseStrength = properties.getMusicManager().getHeartbeatPulseStrength(ticks);

        if (PsychedelicraftClient.getConfig().visual.hurtOverlayEnabled && (entity.hurtTime > 0 || experiencedHealth < 5 || pulseStrength > 0)) {
            float p1 = Math.max((float)entity.hurtTime / entity.maxHurtTime, pulseStrength);
            float p2 = (5 - (experiencedHealth * (1 - pulseStrength))) / 6F;

            float p = p1 > 0 ? p1 : p2 > 0 ? p2 : 0;
            RenderUtil.drawOverlay(matrices, p, screenWidth, screenHeight, HURT_OVERLAY, 0, 0, 1, 1, (int) ((1 - p) * 40));
        }

        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    @Override
    public void close() {

    }
}

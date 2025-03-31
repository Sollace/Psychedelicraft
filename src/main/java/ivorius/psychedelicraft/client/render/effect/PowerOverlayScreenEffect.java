package ivorius.psychedelicraft.client.render.effect;

import java.util.Random;
import java.util.stream.IntStream;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.type.PowerDrug;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class PowerOverlayScreenEffect extends DrugOverlayScreenEffect<PowerDrug> {
    private static final Identifier POWER_PARTICLE_TEXTURE = Psychedelicraft.id("textures/drug/power/particle.png");
    private static final Identifier[] LIGHTNING_TEXTURES = IntStream.range(0, 4)
            .mapToObj(i -> Psychedelicraft.id("textures/drug/power/lightning_" + i + ".png"))
            .toArray(Identifier[]::new);

    public PowerOverlayScreenEffect() {
        super(DrugType.POWER);
    }

    @Override
    protected void render(DrawContext context, Window window, float tickDelta, DrugProperties properties, PowerDrug drug) {

        PlayerEntity entity = properties.asEntity();

        int width = window.getScaledWidth();
        int height = window.getScaledHeight();

        float power = (float)drug.getActiveValue();
        Random powerR = new Random(entity.age); // 20 changes / sec is alright
        int powerParticles = MathHelper.floor(powerR.nextFloat() * 200.0f * power);
        if (powerParticles > 0) {
            renderRandomParticles(context, powerParticles, width / 10, MathHelper.ceil(height / 10 * power), width, height, powerR);
        }

        Random powerLR = new Random(entity.age / 2 * 21124871824l); // Chaos principle doesn't apply ;_;
        float lightningChance = (power - 0.5f) * 0.1f;
        int powerLightnings = 0;
        while (powerLR.nextFloat() < lightningChance && powerLightnings < 3) {
            powerLightnings++;
        }

        if (powerLightnings > 0) {
            int lightningW = height;

            Immediate vertices = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

            for (int i = 0; i < powerLightnings; i++) {
                float lX = powerLR.nextInt(width + lightningW) - lightningW;
                lX += (powerLR.nextFloat() - 0.5f) * lightningW * tickDelta * 2;
                int lIndex = powerLR.nextInt(LIGHTNING_TEXTURES.length);
                boolean upsideDown = powerLR.nextBoolean();
                float lightningTime = ((entity.age % 2) + tickDelta) * 0.5F;

                int color = ColorHelper.withAlpha(ColorHelper.channelFromFloat((0.05f + power * 0.1F) * (1 - lightningTime)), Colors.WHITE);
                var layer = RenderLayer.getGuiTexturedOverlay(LIGHTNING_TEXTURES[lIndex]);
                VertexConsumer buffer = vertices.getBuffer(layer);
                buffer.vertex(lX, height,              -90F).texture(0, upsideDown ? 0 : 1).color(color)
                      .vertex(lX + lightningW, height, -90F).texture(1, upsideDown ? 0 : 1).color(color)
                      .vertex(lX + lightningW, 0,      -90F).texture(1, upsideDown ? 1 : 0).color(color)
                      .vertex(lX, 0,                   -90F).texture(0, upsideDown ? 1 : 0).color(color);
                vertices.draw(layer);
            }
        }
    }

    private void renderRandomParticles(DrawContext context, int number, int width, int height, int screenWidth, int screenHeight, Random rand) {
        for (int i = 0; i < number; i++) {
            int x = rand.nextInt(screenWidth + width) - width;
            int y = rand.nextInt(screenHeight + height) - height;

            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderUtil.drawQuad(context, POWER_PARTICLE_TEXTURE, x, y, x + width, y + height);
        }
    }
}

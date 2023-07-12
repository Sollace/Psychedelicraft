package ivorius.psychedelicraft.client.render.effect;

import java.util.Random;
import java.util.stream.IntStream;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.type.PowerDrug;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
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
    protected void render(MatrixStack matrices, VertexConsumerProvider vertices, int width, int height, float partialTicks, DrugProperties properties, PowerDrug drug) {

        PlayerEntity entity = properties.asEntity();

        float power = (float)drug.getActiveValue();
        Random powerR = new Random(entity.age); // 20 changes / sec is alright
        int powerParticles = MathHelper.floor(powerR.nextFloat() * 200.0f * power);
        if (powerParticles > 0) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, POWER_PARTICLE_TEXTURE);
            renderRandomParticles(matrices, powerParticles, height / 10, MathHelper.ceil(height / 10 * power), width, height, powerR);
        }

        Random powerLR = new Random(entity.age / 2 * 21124871824l); // Chaos principle doesn't apply ;_;
        float lightningChance = (power - 0.5f) * 0.1f;
        int powerLightnings = 0;
        while (powerLR.nextFloat() < lightningChance && powerLightnings < 3) {
            powerLightnings++;
        }

        if (powerLightnings > 0) {
            int lightningW = height;

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE,
                    GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO
            );
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            for (int i = 0; i < powerLightnings; i++) {
                float lX = powerLR.nextInt(width + lightningW) - lightningW;
                lX += (powerLR.nextFloat() - 0.5f) * lightningW * partialTicks * 2.0f;
                int lIndex = powerLR.nextInt(LIGHTNING_TEXTURES.length);
                boolean upsideDown = powerLR.nextBoolean();
                float lightningTime = ((entity.age % 2) + partialTicks) * 0.5f;

                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (0.05f + power * 0.1f) * (1.0f - lightningTime));
                RenderSystem.setShaderTexture(0, LIGHTNING_TEXTURES[lIndex]);

                buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                buffer.vertex(lX, height, -90.0).texture(0, upsideDown ? 0 : 1).next();
                buffer.vertex(lX + lightningW, height, -90.0).texture(1, upsideDown ? 0 : 1).next();
                buffer.vertex(lX + lightningW, 0, -90.0).texture(1, upsideDown ? 1 : 0).next();
                buffer.vertex(lX, 0, -90.0).texture(0, upsideDown ? 1 : 0).next();
                tessellator.draw();
            }
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.defaultBlendFunc();
        }
    }

    public void renderRandomParticles(MatrixStack matrices, int number, int width, int height, int screenWidth, int screenHeight, Random rand) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        for (int i = 0; i < number; i++) {
            int x = rand.nextInt(screenWidth + width) - width;
            int y = rand.nextInt(screenHeight + height) - height;

            buffer.vertex(x, y + height, -90.0).texture(0, 1).next();
            buffer.vertex(x + width, y + height, -90.0).texture(1, 1).next();
            buffer.vertex(x + width, y, -90.0).texture(1, 0).next();
            buffer.vertex(x, y, -90.0).texture(0, 0).next();
        }
        tessellator.draw();
    }
}

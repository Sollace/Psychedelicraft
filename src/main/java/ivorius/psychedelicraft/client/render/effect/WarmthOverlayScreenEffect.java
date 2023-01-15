package ivorius.psychedelicraft.client.render.effect;

import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.type.WarmthDrug;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class WarmthOverlayScreenEffect extends DrugOverlayScreenEffect<WarmthDrug> {
    private static final Identifier COFFEE_OVERLAY = Psychedelicraft.id("textures/drug/coffee/overlay.png");

    public WarmthOverlayScreenEffect() {
        super(DrugType.WARMTH);
    }

    @Override
    protected void render(MatrixStack matrices, VertexConsumerProvider vertices, int screenWidth, int screenHeight, float ticks, DrugProperties properties, WarmthDrug drug) {
        renderWarmthOverlay(matrices, (float)drug.getActiveValue() * 0.5F, screenWidth, screenHeight, MinecraftClient.getInstance().player.age);
    }

    private void renderWarmthOverlay(MatrixStack matrices, float alpha, int width, int height, int ticks) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, COFFEE_OVERLAY);
        RenderSystem.enableBlend();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        for (int i = 0; i < 3; i++) {
            int steps = 30;

            float prevXL = -1;
            float prevXR = -1;
            float prevY = -1;
            boolean init = false;

            for (int y = 0; y < steps; y++) {
                float prog = (float) (steps - y) / (float) steps;

                RenderSystem.setShaderColor(1, 0.5F + prog * 0.3F, 0.35F + prog * 0.1F, alpha - prog * 0.4F);

                int segWidth = width / 9;

                int xM = (int) (segWidth * (i * 3 + 1.5F));

                float xShift = MathHelper.sin((float) y / (float) steps * 5F + ticks / 10F);
                float mXL = xM - segWidth + xShift * segWidth * 0.25f;
                float mXR = xM + segWidth + xShift * segWidth * 0.25f;
                float mY = (float) y / (float) steps * height / 7 * 5 + height / 7;

                if (init) {
                    buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                    buffer.vertex(mXL, mY, -90).texture(0, (float) y / (float) steps).next();
                    buffer.vertex(mXR, mY, -90).texture(1, (float) y / (float) steps).next();
                    buffer.vertex(prevXR, prevY, -90).texture(1, (float) (y - 1) / (float) steps).next();
                    buffer.vertex(prevXL, prevY, -90).texture(0, (float) (y - 1) / (float) steps).next();
                    Tessellator.getInstance().draw();
                } else {
                    init = true;
                }

                prevY = mY;
                prevXL = mXL;
                prevXR = mXR;
            }
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}

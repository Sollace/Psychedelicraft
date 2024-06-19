package ivorius.psychedelicraft.client.render.effect;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.type.WarmthDrug;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class WarmthOverlayScreenEffect extends DrugOverlayScreenEffect<WarmthDrug> {
    private static final Identifier COFFEE_OVERLAY = Psychedelicraft.id("textures/drug/coffee/overlay.png");

    public WarmthOverlayScreenEffect() {
        super(DrugType.WARMTH);
    }

    @Override
    protected void render(DrawContext context, Window window, float tickDelta, DrugProperties properties, WarmthDrug drug) {
        renderWarmthOverlay(context, (float)drug.getActiveValue() * 0.5F, window.getScaledWidth(), window.getScaledHeight(), properties.asEntity().age);
    }

    private void renderWarmthOverlay(DrawContext context, float alpha, int width, int height, int ticks) {
        var buffer = context.getVertexConsumers().getBuffer(RenderLayer.getEntityTranslucent(COFFEE_OVERLAY));
        final int segWidth = width / 9;
        final int segHeight = height / 3;

        for (int i = 0; i < 3; i++) {
            int steps = 20;

            float prevXL = -1;
            float prevXR = -1;
            float prevY = -1;
            boolean init = false;

            for (int y = 0; y < steps; y++) {
                float prog = (float) (steps - y) / (float) steps;

                int xM = (int) (segWidth * (i * 3 + 1.5F));

                float xShift = MathHelper.sin((float) y / (float) steps * 5F + ticks / 10F);
                float mXL = xM - segWidth + xShift * segWidth * 0.25f;
                float mXR = xM + segWidth + xShift * segWidth * 0.25f;
                float mY = (float) y / (float) steps * height / 7 * 5 + segHeight;

                if (init) {
                    int color = ColorHelper.Argb.fromFloats(
                            Math.max(0, alpha - prog * 0.4F),
                            1,
                            0.5F + prog * 0.3F,
                            0.35F + prog * 0.1F
                    );
                    buffer.vertex(mXL,    mY,    -90, color, 0, (float)  y      / (float) steps, OverlayTexture.DEFAULT_UV, LightmapTextureManager.MAX_LIGHT_COORDINATE, 0, 0, 0);
                    buffer.vertex(mXR,    mY,    -90, color, 1, (float)  y      / (float) steps, OverlayTexture.DEFAULT_UV, LightmapTextureManager.MAX_LIGHT_COORDINATE, 0, 0, 0);
                    buffer.vertex(prevXR, prevY, -90, color, 1, (float) (y - 1) / (float) steps, OverlayTexture.DEFAULT_UV, LightmapTextureManager.MAX_LIGHT_COORDINATE, 0, 0, 0);
                    buffer.vertex(prevXL, prevY, -90, color, 0, (float) (y - 1) / (float) steps, OverlayTexture.DEFAULT_UV, LightmapTextureManager.MAX_LIGHT_COORDINATE, 0, 0, 0);
                } else {
                    init = true;
                }

                prevY = mY;
                prevXL = mXL;
                prevXR = mXR;
            }
        }
    }
}

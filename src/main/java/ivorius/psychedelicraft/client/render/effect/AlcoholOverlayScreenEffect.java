package ivorius.psychedelicraft.client.render.effect;

import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.type.AlcoholDrug;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;

public class AlcoholOverlayScreenEffect extends DrugOverlayScreenEffect<AlcoholDrug> {
    public AlcoholOverlayScreenEffect() {
        super(DrugType.ALCOHOL);
    }

    @Override
    protected void render(DrawContext context, Window window, float tickDelta, DrugProperties properties, AlcoholDrug drug) {
        float alcohol = (float)drug.getActiveValue();
        if (alcohol <= 0) {
            return;
        }

        float overlayAlpha = Math.min(0.8F, (MathHelper.sin(tickDelta / 80F) * alcohol * 0.5F + alcohol));
        Sprite sprite = MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(Blocks.NETHER_PORTAL.getDefaultState());
        context.drawSprite(0, 0, -90, window.getScaledWidth(), window.getScaledHeight(), sprite, 1, 1, 1, overlayAlpha);
    }
}

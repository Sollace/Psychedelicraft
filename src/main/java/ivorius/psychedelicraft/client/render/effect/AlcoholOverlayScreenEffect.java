package ivorius.psychedelicraft.client.render.effect;

import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.type.AlcoholDrug;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.MathHelper;

public class AlcoholOverlayScreenEffect extends DrugOverlayScreenEffect<AlcoholDrug> {
    public AlcoholOverlayScreenEffect() {
        super(DrugType.ALCOHOL);
    }

    @Override
    protected void render(MatrixStack matrices, VertexConsumerProvider vertices, int width, int height, float ticks, DrugProperties properties, AlcoholDrug drug) {
        float alcohol = (float)drug.getActiveValue();
        if (alcohol <= 0) {
            return;
        }

        float overlayAlpha = Math.min(0.8F, (MathHelper.sin(ticks / 80F) * alcohol * 0.5F + alcohol));
        Sprite sprite = MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelParticleSprite(Blocks.NETHER_PORTAL.getDefaultState());
        DrugRenderer.drawOverlay(matrices, overlayAlpha * 0.25f, width, height, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                sprite.getMinU(),
                sprite.getMinV(),
                sprite.getMaxU(),
                sprite.getMaxV(),
                0
        );
    }
}

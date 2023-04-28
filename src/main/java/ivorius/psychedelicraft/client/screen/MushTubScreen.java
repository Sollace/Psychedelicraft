package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

/**
 * Created by lukas on 13.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class MushTubScreen extends FluidProcessingContraptionScreen<MashTubBlockEntity> {
    public MushTubScreen(FluidContraptionScreenHandler<MashTubBlockEntity> handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawAdditionalInfo(MatrixStack matrices, int baseX, int baseY) {
        float progress = handler.getBlockEntity().getProgress();
        if (progress > 0 && progress < 1) {
            drawTexture(matrices, baseX + 140, baseY + 14, 233, 22, 23, 22);
            int barHeight = (int)(22 * (1 - progress));
            drawTexture(matrices, baseX + 140, baseY + 14 + barHeight, 233, barHeight, 23, 23 - barHeight);
        }
    }
}

package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.block.entity.BarrelBlockEntity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

/**
 * Created by lukas on 13.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class BarrelScreen extends FluidProcessingContraptionScreen<BarrelBlockEntity> {

    public BarrelScreen(FluidContraptionScreenHandler<BarrelBlockEntity> handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawAdditionalInfo(DrawContext context, int baseX, int baseY) {
        float progress = handler.getBlockEntity().getProgress();
        if (progress > 0 && progress < 1) {
            context.drawTexture(background, baseX + 110, baseY + 20, 233, 22, 23, 22);
            int barHeight = (int)(22 * (1 - progress));
            context.drawTexture(background, baseX + 110, baseY + 20 + barHeight, 233, barHeight, 23, 23 - barHeight);
        }
    }
}

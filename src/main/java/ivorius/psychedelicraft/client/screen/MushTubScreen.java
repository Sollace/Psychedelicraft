package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.*;

import java.util.List;

/**
 * Created by lukas on 13.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class MushTubScreen extends FlaskScreen<MashTubBlockEntity> {
    public static final Identifier BACKGROUND = Psychedelicraft.id("textures/gui/wooden_vat.png");

    private static final List<Text> FERMENTING_LABEL = List.of(Text.translatable("fluid.status.fermenting").formatted(Formatting.GREEN));

    public MushTubScreen(FluidContraptionScreenHandler<MashTubBlockEntity> handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected Identifier getBackgroundTexture() {
        return BACKGROUND;
    }

    @Override
    protected void drawAdditionalInfo(MatrixStack matrices, int baseX, int baseY) {
        float progress = handler.getBlockEntity().getProgress();
        if (progress > 0 && progress < 1) {
            drawTexture(matrices, baseX + 23, baseY + 14, 176, 0, 24 - (int)(progress * 24), 17);
        }
    }

    @Override
    protected List<Text> getAdditionalTankText() {
        return handler.getBlockEntity().isActive() ? FERMENTING_LABEL : List.of();
    }
}

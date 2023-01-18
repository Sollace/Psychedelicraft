package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.DistilleryBlockEntity;
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
public class DistilleryScreen extends FlaskScreen<DistilleryBlockEntity> {
    public static final Identifier TEXTURE = Psychedelicraft.id("textures/gui/distillery.png");

    private static final List<Text> DISTILLING_LABEL = List.of(Text.translatable("fluid.status.distilling").formatted(Formatting.GREEN));

    public DistilleryScreen(FluidContraptionScreenHandler<DistilleryBlockEntity> handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected Identifier getBackgroundTexture() {
        return TEXTURE;
    }

    @Override
    protected void drawAdditionalInfo(MatrixStack matrices, int baseX, int baseY) {
        float progress = handler.getBlockEntity().getProgress();
        if (progress > 0 && progress < 1) {
            int timeLeft = (int)(progress * 13);
            drawTexture(matrices, baseX + 24, baseY + 15 + timeLeft, 176, timeLeft, 20, 13 - timeLeft);
        }
    }

    @Override
    protected List<Text> getAdditionalTankText() {
        return handler.getBlockEntity().isActive() ? DISTILLING_LABEL : List.of();
    }
}

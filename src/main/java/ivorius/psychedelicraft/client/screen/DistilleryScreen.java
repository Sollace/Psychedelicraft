package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.DistilleryBlockEntity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 13.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class DistilleryScreen extends FluidContraptionScreen<DistilleryBlockEntity> {
    public static final Identifier TEXTURE = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "container_distillery.png");

    public DistilleryScreen(FluidContraptionScreenHandler<DistilleryBlockEntity> handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected Identifier getBackgroundTexture() {
        return TEXTURE;
    }

    @Override
    protected void drawAdditionalInfo(MatrixStack matrices, int baseX, int baseY) {
        int timeLeftFermenting = handler.getBlockEntity().getProgress(13);
        if (timeLeftFermenting < 24) {
            drawTexture(matrices, baseX + 24, baseY + 15 + timeLeftFermenting, 176, timeLeftFermenting, 20, 13 - timeLeftFermenting);
        }
    }

    @Override
    protected List<Text> getAdditionalTankText() {
        return handler.getBlockEntity().isActive()
                ? Arrays.asList(Text.translatable("fluid.status.distilling").formatted(Formatting.GREEN))
                : List.of();
    }
}

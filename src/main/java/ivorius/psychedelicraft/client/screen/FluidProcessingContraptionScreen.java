package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.*;

import java.util.List;
import java.util.Locale;

abstract class FluidProcessingContraptionScreen<T extends FluidProcessingBlockEntity> extends FlaskScreen<T> {

    private final List<Text> processingLabel;

    public FluidProcessingContraptionScreen(FluidContraptionScreenHandler<T> handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        processingLabel = List.of(Text.translatable("fluid.status." + handler.getBlockEntity().getProcessType().name().toLowerCase(Locale.ROOT) + "ing").formatted(Formatting.GREEN));
    }

    @Override
    protected final List<Text> getAdditionalTankText() {
        return handler.getBlockEntity().isActive() ? processingLabel : List.of();
    }
}

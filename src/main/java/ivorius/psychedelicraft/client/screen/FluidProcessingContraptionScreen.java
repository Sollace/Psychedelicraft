package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.util.List;

abstract class FluidProcessingContraptionScreen<T extends FluidProcessingBlockEntity> extends FlaskScreen<T> {

    private final List<Text> processingLabel;

    public FluidProcessingContraptionScreen(FluidContraptionScreenHandler<T> handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        processingLabel = List.of(handler.getBlockEntity().getProcessType().getStatus());
    }

    @Override
    protected final List<Text> getAdditionalTankText() {
        return handler.getBlockEntity().isActive() ? processingLabel : super.getAdditionalTankText();
    }
}

package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

abstract class FluidProcessingContraptionScreen<T extends FluidProcessingBlockEntity> extends FlaskScreen<T> {

    public FluidProcessingContraptionScreen(FluidContraptionScreenHandler<T> handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected List<Text> getAdditionalTankText() {
        List<Text> tooltip = new ArrayList<>();
        Resovoir tank = handler.getBlockEntity().getPrimaryTank();
        tank.getContents().fluid().appendTankTooltip(tank.getContents(), handler.getBlockEntity().getWorld(), tooltip, handler.getBlockEntity());
        return tooltip;
    }
}

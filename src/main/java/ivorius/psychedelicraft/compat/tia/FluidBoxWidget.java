package ivorius.psychedelicraft.compat.tia;

import java.util.List;

import io.github.mattidragon.tlaapi.api.gui.CustomTlaWidget;
import io.github.mattidragon.tlaapi.api.gui.TlaBounds;
import ivorius.psychedelicraft.client.screen.AbstractFluidContraptionScreen;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.gui.DrawContext;

final class FluidBoxWidget implements CustomTlaWidget {
    private final List<ItemFluids> fluids;
    private final TlaBounds bounds;
    private final int capacity;

    public static FluidBoxWidget create(ItemFluids fluids, int capacity, int x, int y, int width, int height) {
        return create(List.of(fluids), capacity, x, y, width, height);
    }

    public static FluidBoxWidget create(List<ItemFluids> fluids, int capacity, int x, int y, int width, int height) {
        return new FluidBoxWidget(fluids, capacity, x, y, width, height);
    }

    private FluidBoxWidget(List<ItemFluids> fluids, int capacity, int x, int y, int width, int height) {
        this.fluids = fluids;
        this.capacity = capacity;
        this.bounds = new TlaBounds(x, y, width, height);
    }

    @Override
    public TlaBounds getBounds() {
        return bounds;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ItemFluids fluids = this.fluids.get((int)(System.currentTimeMillis() / 3000) % this.fluids.size());
        AbstractFluidContraptionScreen.drawTank(context, fluids, capacity, bounds.left(), bounds.top(), bounds.width(), bounds.height());
        AbstractFluidContraptionScreen.drawTankTooltip(context, fluids, capacity, bounds.left(), bounds.top(), bounds.width(), bounds.height(), mouseX, mouseY, List.of());
    }
}

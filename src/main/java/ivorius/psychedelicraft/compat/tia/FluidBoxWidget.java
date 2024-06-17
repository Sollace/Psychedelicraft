package ivorius.psychedelicraft.compat.tia;

import java.util.ArrayList;
import java.util.List;

import io.github.mattidragon.tlaapi.api.gui.CustomTlaWidget;
import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.gui.TlaBounds;
import io.github.mattidragon.tlaapi.api.gui.WidgetConfig;
import ivorius.psychedelicraft.client.screen.AbstractFluidContraptionScreen;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.gui.DrawContext;

final class FluidBoxWidget implements CustomTlaWidget {
    private final List<ItemFluids> fluids;
    private final TlaBounds bounds;
    private final int capacity;

    private final List<TlaBounds> exclusionZones = new ArrayList<>();

    public static FluidBoxWidget create(ItemFluids fluids, int capacity, int x, int y, int width, int height, GuiBuilder builder) {
        return create(List.of(fluids), capacity, x, y, width, height, builder);
    }

    public static FluidBoxWidget create(List<ItemFluids> fluids, int capacity, int x, int y, int width, int height, GuiBuilder builder) {
        return new FluidBoxWidget(fluids, capacity, x, y, width, height, builder);
    }

    private FluidBoxWidget(List<ItemFluids> fluids, int capacity, int x, int y, int width, int height, GuiBuilder builder) {
        this.fluids = fluids;
        this.capacity = capacity;
        this.bounds = new TlaBounds(x, y, width, height);

        builder.addCustomWidget(this);
    }

    @Override
    public TlaBounds getBounds() {
        return bounds;
    }

    public void addExclusion(TlaBounds bounds) {
        exclusionZones.add(bounds);
    }

    public void addExclusion(WidgetConfig config) {
        exclusionZones.add(config.getBounds());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ItemFluids fluids = this.fluids.get((int)(System.currentTimeMillis() / 3000) % this.fluids.size()).ofAmount(capacity);
        int width = bounds.width();
        int height = bounds.height();

        AbstractFluidContraptionScreen.drawTank(context, fluids, capacity, 0, 0, width, height);

        for (var exclusion : exclusionZones) {
            if (exclusion.contains(mouseX, mouseY)) {
                return;
            }
        }
        AbstractFluidContraptionScreen.drawTankTooltip(context, fluids, 0, 0, 0, width, height, mouseX - bounds.left(), mouseY - bounds.top(), List.of());
    }
}

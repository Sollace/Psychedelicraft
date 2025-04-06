package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.fluid.ConsumableFluid.ConsumptionType;
import net.minecraft.item.DyeableItem;

public class TintedGlassBottleItem extends DrinkableItem implements DyeableItem {

    public TintedGlassBottleItem(Settings settings, int consumptionVolume, int consumptionTime,
            ConsumptionType consumptionType) {
        super(settings, consumptionVolume, consumptionTime, consumptionType);
    }

}

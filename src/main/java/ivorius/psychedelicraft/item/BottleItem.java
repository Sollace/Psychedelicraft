/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.fluid.*;
import net.minecraft.item.*;

/**
 * Created by Sollace on Jan 1 2023
 */
public class BottleItem extends DrinkableItem implements DyeableItem {
    public BottleItem(Settings settings, int capacity, int consumptionVolume, ConsumableFluid.ConsumptionType consumptionType) {
        super(settings, capacity, consumptionVolume, Item.DEFAULT_MAX_USE_TIME, consumptionType);
    }
}

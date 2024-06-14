/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.fluid.*;

/**
 * Created by Sollace on Jan 1 2023
 */
public class BottleItem extends DrinkableItem {
    public BottleItem(Settings settings, int consumptionVolume, ConsumableFluid.ConsumptionType consumptionType) {
        super(settings, consumptionVolume, DEFAULT_MAX_USE_TIME, consumptionType);
    }
}

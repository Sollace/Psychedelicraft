/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.fluid.ConsumableFluid;

public class InjectableItem extends DrinkableItem {
    public static final int FLUID_PER_INJECTION = 10;

    public InjectableItem(Settings settings, int capacity) {
        super(settings, capacity, FLUID_PER_INJECTION, DEFAULT_MAX_USE_TIME, ConsumableFluid.ConsumptionType.INJECT);
    }
}

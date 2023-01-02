/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluids;

import ivorius.psychedelicraft.items.FluidContainerItem;
import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 22.10.14.
 */
public class FluidHelper {
    public static final int MILLIBUCKETS_PER_LITER = 1_000;

    public static int getTranslucentFluidColor(ItemStack stack) {
        SimpleFluid fluid = stack.getItem() instanceof FluidContainerItem ? ((FluidContainerItem) stack.getItem()).getFluid(stack) : SimpleFluid.EMPTY;
        int color = fluid.getColor(stack);
        if (!fluid.isTranslucent()) {
            return color | 0xff000000;
        }
        return 0xFFFFFFFF;
    }
}

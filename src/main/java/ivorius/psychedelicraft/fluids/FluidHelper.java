/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluids;

import ivorius.psychedelicraft.items.FluidContainerItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 22.10.14.
 */
public class FluidHelper
{
    public static int MILLIBUCKETS_PER_LITER = 1_000;

    public static int getTranslucentFluidColor(ItemStack stack)
    {
        Fluid fluid = stack.getItem() instanceof FluidContainerItem ? ((FluidContainerItem) stack.getItem()).getFluid(stack) : null;
        if (fluid != null)
        {
            return fluid.getFluid().getColor(fluid) | (fluid.getFluid() instanceof TranslucentFluid ? 0 : 0xff000000);
        }

        return 0xffffffff;
    }
}

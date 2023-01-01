/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluids;

import ivorius.psychedelicraft.events.FluidDrinkEvent;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 22.10.14.
 */
public class FluidHelper
{
    public static int MILLIBUCKETS_PER_LITER = 1_000;

    public static List<FluidStack> allFluids(String type)
    {
        List<FluidStack> fluidStacks = new ArrayList<>();
        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values())
        {
            if (fluid instanceof FluidWithTypes)
                ((FluidWithTypes) fluid).addCreativeSubtypes(type, fluidStacks);
        }
        return fluidStacks;
    }

    public static List<FluidStack> allFluids(String type, int capacity)
    {
        List<FluidStack> fluidStacks = new ArrayList<>();
        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values())
        {
            if (fluid instanceof FluidWithTypes)
                ((FluidWithTypes) fluid).addCreativeSubtypes(type, fluidStacks);
        }

        for (FluidStack fluidStack : fluidStacks)
            fluidStack.amount = capacity;

        return fluidStacks;
    }

    public static void ensureTag(FluidStack stack)
    {
        if (stack.tag == null)
            stack.tag = new NBTTagCompound();
    }

    public static int getTranslucentFluidColor(ItemStack stack)
    {
        FluidStack fluid = stack.getItem() instanceof IFluidContainerItem ? ((IFluidContainerItem) stack.getItem()).getFluid(stack) : null;
        if (fluid != null)
        {
            return fluid.getFluid().getColor(fluid) | (fluid.getFluid() instanceof TranslucentFluid ? 0 : 0xff000000);
        }

        return 0xffffffff;
    }

    public static FluidStack drink(ItemStack stack, EntityLivingBase entity, int maxDrunk, boolean doDrink)
    {
        IFluidContainerItem container = stack.getItem() instanceof IFluidContainerItem ? (IFluidContainerItem) stack.getItem() : null;
        if (container != null)
        {
            FluidStack wouldDrain = container.drain(stack, maxDrunk, false);
            if (wouldDrain != null && wouldDrain.amount > 0)
            {
                Fluid fluid = wouldDrain.getFluid();
                if (fluid instanceof DrinkableFluid && ((DrinkableFluid) fluid).canDrink(wouldDrain, entity))
                {
                    FluidStack drained = container.drain(stack, maxDrunk, doDrink);
                    if (doDrink)
                    {
                        ((DrinkableFluid) fluid).drink(drained, entity);
                        MinecraftForge.EVENT_BUS.post(new FluidDrinkEvent(entity, drained));
                    }
                    return drained;
                }
            }
        }

        return null;
    }

    public static FluidStack inject(ItemStack stack, EntityLivingBase entity, int maxInjected, boolean doInject)
    {
        IFluidContainerItem container = stack.getItem() instanceof IFluidContainerItem ? (IFluidContainerItem) stack.getItem() : null;
        if (container != null)
        {
            FluidStack wouldDrain = container.drain(stack, maxInjected, false);
            if (wouldDrain != null && wouldDrain.amount > 0)
            {
                Fluid fluid = wouldDrain.getFluid();
                if (fluid instanceof InjectableFluid && ((InjectableFluid) fluid).canInject(wouldDrain, entity))
                {
                    FluidStack drained = container.drain(stack, maxInjected, doInject);
                    if (doInject)
                        ((InjectableFluid) fluid).inject(drained, entity);
                    return drained;
                }
            }
        }

        return null;
    }
}

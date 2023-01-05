/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.crafting;

import ivorius.psychedelicraft.items.PSItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 03.11.14.
 */
@Deprecated
public class RecipePourDrink implements RecipeAction
{
    @Override
    public ItemStack visualCraftingResult(InventoryCrafting inventoryCrafting)
    {
        Pair<ItemStack, ItemStack> pouringPair = getPouringPair(inventoryCrafting);
        if (pouringPair != null)
        {
            ItemStack src = pouringPair.getLeft();
            ItemStack dst = pouringPair.getRight().copy();

            Pourable srcItem = (Pourable) src.getItem();
            Pourable dstItem = (Pourable) dst.getItem();

            if (srcItem.canPour(src, dst) && dstItem.canReceivePour(dst, src))
            {
                FluidStack drainSim = srcItem.drain(src, dstItem.getCapacity(dst), false);
                int maxFill = dstItem.fill(dst, drainSim, true);

                if (maxFill > 0)
                {
                    dst.setStackDisplayName(StatCollector.translateToLocalFormatted("recipe.action.pour", drainSim.getFluid().getLocalizedName(drainSim)));
                    return dst;
                }
            }
        }

        return null;
    }

    @Override
    public Pair<ItemStack, List<ItemStack>> craftingResult(InventoryCrafting inventoryCrafting)
    {
        Pair<ItemStack, ItemStack> pouringPair = getPouringPair(inventoryCrafting);

        if (pouringPair != null)
        {
            ItemStack src = pouringPair.getLeft().copy();
            ItemStack dst = pouringPair.getRight().copy();

            Pourable srcItem = (Pourable) src.getItem();
            Pourable dstItem = (Pourable) dst.getItem();

            FluidStack drainSim = srcItem.drain(src, dstItem.getCapacity(dst), false);
            int maxFill = dstItem.fill(dst, drainSim, true);
            srcItem.drain(src, maxFill, true);

            return new ImmutablePair<>(dst, Collections.singletonList(src));
        }

        return null;
    }

    public Pair<ItemStack, ItemStack> getPouringPair(InventoryCrafting inventoryCrafting)
    {
        ItemStack first = null;
        ItemStack second = null;

        for (int y = 0; y < 3; y++)
        {
            List<ItemStack> items = itemStacksInRow(inventoryCrafting, y);

            if (items.size() > 1)
                return null;
            else if (items.size() == 1)
            {
                ItemStack rowStack = items.get(0);

                if (!(rowStack.getItem() instanceof Pourable) || rowStack.stackSize != 1)
                    return null;

                if (first == null)
                    first = rowStack;
                else if (second == null)
                    second = rowStack;
                else
                    return null;
            }
        }

        if (first != null && second != null)
            return new ImmutablePair<>(first, second);

        return null;
    }

    public List<ItemStack> itemStacksInRow(InventoryCrafting inventoryCrafting, int row)
    {
        List<ItemStack> list = new ArrayList<>();

        for (int x = 0; x < 3; x++)
        {
            ItemStack stack = inventoryCrafting.getStackInRowAndColumn(x, row);
            if (stack != null)
                list.add(stack);
        }

        return list;
    }

    @Override
    public int getRecipeSize()
    {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return new ItemStack(PSItems.woodenMug);
    }
}

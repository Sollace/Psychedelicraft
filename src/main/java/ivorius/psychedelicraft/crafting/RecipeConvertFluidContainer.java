package ivorius.psychedelicraft.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ivorius.psychedelicraft.items.FluidContainerItem;

/**
 * Created by lukas on 10.11.14.
 */
public class RecipeConvertFluidContainer implements IRecipe
{
    public static ItemStack getFirstFluidContainer(InventoryCrafting inventoryCrafting)
    {
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                ItemStack itemstack = inventoryCrafting.getStackInRowAndColumn(j, i);

                if (itemstack != null && (itemstack.getItem() instanceof FluidContainerItem))
                    return itemstack;
            }
        }

        return null;
    }

    private final ItemStack recipeOutput;
    public final List<Object> recipeItems;

    public RecipeConvertFluidContainer(ItemStack recipeOutput, List<Object> items)
    {
        this.recipeOutput = recipeOutput;
        this.recipeItems = items;
    }

    public RecipeConvertFluidContainer(ItemStack recipeOutput, Object... items)
    {
        this(recipeOutput, RecipeFillDrink.getItemStacks(items));
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return recipeOutput;
    }

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world)
    {
        ArrayList<Object> required = new ArrayList<>(this.recipeItems);

        ItemStack drinkHolder = getFirstFluidContainer(inventoryCrafting);
        if (drinkHolder == null)
            return false;

        for (int x = 0; x < inventoryCrafting.getSizeInventory(); x++)
        {
            ItemStack slot = inventoryCrafting.getStackInSlot(x);

            if (slot != null)
            {
                boolean inRecipe = false;
                Iterator<Object> req = required.iterator();

                while (req.hasNext())
                {
                    boolean match = false;

                    Object next = req.next();

                    if (next instanceof ItemStack)
                    {
                        match = OreDictionary.itemMatches((ItemStack) next, slot, false);
                    }
                    else if (next instanceof ArrayList)
                    {
                        Iterator<ItemStack> itr = ((ArrayList<ItemStack>) next).iterator();
                        while (itr.hasNext() && !match)
                        {
                            match = OreDictionary.itemMatches(itr.next(), slot, false);
                        }
                    }

                    if (match)
                    {
                        inRecipe = true;
                        required.remove(next);
                        break;
                    }
                }

                if (!inRecipe)
                {
                    return false;
                }
            }
        }

        return required.isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting)
    {
        ItemStack fluidContainer = getFirstFluidContainer(inventoryCrafting);
        IFluidContainerItem fluidContainerItem = (IFluidContainerItem) fluidContainer.getItem();
        FluidStack containedFluid = fluidContainerItem.drain(fluidContainer, fluidContainerItem.getCapacity(fluidContainer), false);

        ItemStack result = recipeOutput.copy();
        if (containedFluid != null)
            ((IFluidContainerItem) result.getItem()).fill(result, containedFluid, true);

        return result;
    }

    @Override
    public int getRecipeSize()
    {
        return this.recipeItems.size();
    }
}

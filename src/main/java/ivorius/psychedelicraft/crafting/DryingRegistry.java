package ivorius.psychedelicraft.crafting;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

/**
 * Created by lukas on 07.11.14.
 */
public class DryingRegistry
{
    private static Map<Object, ItemStack> dryingRecipes = new HashMap<>();

    public static void addDryingResult(Object src, ItemStack result)
    {
        dryingRecipes.put(src, result);
    }

    public static ItemStack dryingResult(Collection<ItemStack> sources)
    {
        if (sources.size() != 9)
            return null;

        for (Object target : allDryingSources())
        {
            boolean allSame = true;

            for (ItemStack src : sources)
            {
                if (!matches(src, target))
                {
                    allSame = false;
                    break;
                }
            }

            if (allSame)
                return dryingResult(target);
        }

        return null;
    }

    public static ItemStack dryingResult(Object source)
    {
        return dryingRecipes.get(source).copy();
    }

    public static Set<Object> allDryingSources()
    {
        return dryingRecipes.keySet();
    }

    private static boolean matches(ItemStack stack, Object target) {
        if (target instanceof ItemStack)
            return ItemStack.areEqual(stack, (ItemStack) target);
        else if (target instanceof Item)
            return stack.getItem() == target;
        else if (target instanceof Block)
            return stack.getItem() == ((Block) target).asItem();

        return false;
    }
}

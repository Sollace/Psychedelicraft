package ivorius.psychedelicraft.recipe;

import net.minecraft.block.Block;
import net.minecraft.item.*;

import java.util.*;

import ivorius.psychedelicraft.item.PSItems;

/**
 * Created by lukas on 07.11.14.
 */
@Deprecated
public class DryingRegistry
{
    private static Map<Object, ItemStack> dryingRecipes;

    static {
        dryingRecipes = new HashMap<>();
        addDryingResult(PSItems.CANNABIS_LEAF, new ItemStack(PSItems.DRIED_CANNABIS_LEAF, 3));
        addDryingResult(PSItems.CANNABIS_BUDS, new ItemStack(PSItems.DRIED_CANNABIS_BUDS, 3));
        addDryingResult(Items.BROWN_MUSHROOM, new ItemStack(PSItems.BROWN_MAGIC_MUSHROOMS, 3));
        addDryingResult(Items.RED_MUSHROOM, new ItemStack(PSItems.RED_MAGIC_MUSHROOMS, 3));
        addDryingResult(PSItems.TOBACCO_LEAVES, new ItemStack(PSItems.DRIED_TOBACCO, 3));
        addDryingResult(PSItems.COCA_LEAVES, new ItemStack(PSItems.DRIED_COCA_LEAVES, 3));
        addDryingResult(PSItems.PEYOTE, new ItemStack(PSItems.DRIED_PEYOTE, 3));
    }

    public static void addDryingResult(Object src, ItemStack result) {
        dryingRecipes.put(src, result);
    }

    public static ItemStack dryingResult(Collection<ItemStack> sources) {
        if (sources.size() != 9)
            return null;

        for (Object target : allDryingSources()) {
            boolean allSame = true;

            for (ItemStack src : sources) {
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

    public static ItemStack dryingResult(Object source) {
        return dryingRecipes.get(source).copy();
    }

    public static Set<Object> allDryingSources() {
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

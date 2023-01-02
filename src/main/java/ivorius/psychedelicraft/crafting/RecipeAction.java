/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by lukas on 03.11.14.
 */
@Deprecated
public interface RecipeAction
{
    ItemStack visualCraftingResult(CraftingInventory inventory);

    Pair<ItemStack, List<ItemStack>> craftingResult(CraftingInventory inventory);

    int getRecipeSize();

    ItemStack getRecipeOutput();
}

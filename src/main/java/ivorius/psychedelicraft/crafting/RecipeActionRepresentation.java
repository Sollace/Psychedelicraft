/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.crafting;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;

/**
 * Created by lukas on 03.11.14.
 */
public class RecipeActionRepresentation implements IRecipe {
    public static final String ACTION_TAG_ID = "PS_ACTION_RECIPE_ID";

    public RecipeAction recipeAction;

    public RecipeActionRepresentation(RecipeAction recipeAction) {
        this.recipeAction = recipeAction;
    }

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        return recipeAction.visualCraftingResult(inventoryCrafting) != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        ItemStack result = recipeAction.visualCraftingResult(inventoryCrafting);
        if (result != null)
        {
            result = result.copy();
            result.setTagInfo(ACTION_TAG_ID, new NBTTagString(RecipeActionRegistry.idForAction(recipeAction)));
            return result;
        }

        return null;
    }

    @Override
    public int getRecipeSize() {
        return recipeAction.getRecipeSize();
    }

    @Override
    public ItemStack getRecipeOutput() {
        return recipeAction.getRecipeOutput();
    }
}

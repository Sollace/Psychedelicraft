package ivorius.psychedelicraft.recipe;

import java.util.List;

import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/**
 * Recipe for pouring fluid from one container to another.
 */
public class PouringRecipe extends SpecialCraftingRecipe {
    public PouringRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.POUR_DRINK;
    }

    @Override
    public boolean matches(CraftingRecipeInput inventory, World world) {
        List<ItemStack> recepticals = RecipeUtils
                .recepticals(inventory.getStacks().stream())
                .toList();

        if (inventory.getStacks().size() != recepticals.size() || recepticals.size() < 2) {
            return false;
        }

        return ItemFluids.of(recepticals.get(1)).canCombine( ItemFluids.of(recepticals.get(0)))
                && ItemFluids.of(recepticals.get(1)).amount() < FluidCapacity.get(recepticals.get(1));
    }

    @Override
    public ItemStack craft(CraftingRecipeInput inventory, WrapperLookup registries) {
        var recepticals = RecipeUtils.recepticals(inventory.getStacks().stream()).toList();

        ItemFluids.Transaction to = ItemFluids.Transaction.begin(recepticals.get(1));
        ItemFluids.Transaction from = ItemFluids.Transaction.begin(recepticals.get(0));

        int maxMoved = Math.min(from.fluids().amount(), to.capacity() - to.fluids().amount());
        to.deposit(from.withdraw(maxMoved));

        return to.toItemStack();
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput inventory) {
        var recepticals = RecipeUtils.recepticalSlots(inventory).toList();
        if (recepticals.size() < 2) {
            return DefaultedList.ofSize(inventory.getSize(), ItemStack.EMPTY);
        }

        ItemFluids.Transaction to = ItemFluids.Transaction.begin(recepticals.get(1).content());
        ItemFluids.Transaction from = ItemFluids.Transaction.begin(recepticals.get(0).content());

        from.withdraw(Math.min(from.fluids().amount(), to.capacity() - to.fluids().amount()));

        DefaultedList<ItemStack> remainder = DefaultedList.ofSize(inventory.getSize(), ItemStack.EMPTY);
        remainder.set(recepticals.get(0).position(), to.toItemStack());
        return remainder;
    }


    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 2;
    }
}

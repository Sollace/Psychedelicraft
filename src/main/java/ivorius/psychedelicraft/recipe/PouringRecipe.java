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
 *
 * The left container in the grid must contain fluid to pour
 * The right container in the grid must either be empty or have the same fluid with room to pour into
 *
 * cup of water + empty bucket -> empty cup + bucket (partially filled) of water
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

        if (inventory.getStacks().size() != 2 || recepticals.size() != 2) {
            return false;
        }

        ItemFluids to = ItemFluids.of(recepticals.get(1));
        ItemFluids from = ItemFluids.of(recepticals.get(0));

        return to.canCombine(from)
                && FluidCapacity.getPercentage(recepticals.get(0)) > 0
                && FluidCapacity.getPercentage(recepticals.get(1)) < 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput inventory, WrapperLookup registries) {
        var recepticals = RecipeUtils.recepticals(inventory.getStacks().stream()).toList();

        ItemFluids.Transaction to = ItemFluids.Transaction.begin(recepticals.get(1).copy());
        ItemFluids.Transaction from = ItemFluids.Transaction.begin(recepticals.get(0).copy());

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
        remainder.set(recepticals.get(0).position(), from.toItemStack());
        return remainder;
    }


    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 2;
    }
}

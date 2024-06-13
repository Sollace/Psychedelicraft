package ivorius.psychedelicraft.recipe;

import java.util.List;
import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;
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
        List<MutableFluidContainer> recepticals = RecipeUtils
                .toRecepticals(inventory.getStacks().stream())
                .map(e -> e.getKey().toMutable(e.getValue()))
                .toList();

        if (inventory.getStacks().size() != recepticals.size() || recepticals.size() < 2) {
            return false;
        }

        return recepticals.get(1).canReceive(recepticals.get(0).getFluid());
    }

    @Override
    public ItemStack craft(CraftingRecipeInput inventory, WrapperLookup registries) {
        var recepticals = RecipeUtils.toRecepticals(inventory.getStacks().stream()).toList();

        MutableFluidContainer mutableTo = recepticals.get(1).getKey().toMutable(recepticals.get(1).getValue());
        MutableFluidContainer mutableFrom = recepticals.get(0).getKey().toMutable(recepticals.get(0).getValue());

        mutableFrom.transfer(mutableTo.getCapacity() - mutableTo.getLevel(), mutableTo, null);

        return mutableTo.asStack();
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput inventory) {
        var recepticals = RecipeUtils.recepticalSlots(inventory).toList();
        if (recepticals.size() < 2) {
            return DefaultedList.ofSize(inventory.getSize(), ItemStack.EMPTY);
        }

        var from = recepticals.get(0);
        var to = recepticals.get(1);

        MutableFluidContainer mutableTo = to.content().getKey().toMutable(to.content().getValue());
        MutableFluidContainer mutableFrom = from.content().getKey().toMutable(from.content().getValue());

        mutableFrom.transfer(mutableTo.getCapacity() - mutableTo.getLevel(), mutableTo, null);

        DefaultedList<ItemStack> remainder = DefaultedList.ofSize(inventory.getSize(), ItemStack.EMPTY);
        remainder.set(from.position(), mutableFrom.asStack());
        return remainder;
    }


    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 2;
    }
}

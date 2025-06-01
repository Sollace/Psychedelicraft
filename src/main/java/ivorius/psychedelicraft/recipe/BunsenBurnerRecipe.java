package ivorius.psychedelicraft.recipe;

import java.util.List;
import java.util.Set;

import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.Processable.ByProductConsumer;
import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.RecipeInput;

public interface BunsenBurnerRecipe extends Recipe<BunsenBurnerRecipe.Input> {
    /**
     * The number of ticks it takes for this recipe to complete
     */
    int stewTime();

    boolean isAcceptableIngredient(ItemStack stack);


    @Override
    default IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    default RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    default boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    default RecipeType<BunsenBurnerRecipe> getType() {
        return PSRecipes.CHEMISTRY;
    }

    public record Input(FluidMound fluids, ItemMound input, Product consumer) implements RecipeInput {
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return fluids.isEmpty() && input.isEmpty();
        }
    }

    public record Product(FluidMound fluids, List<ItemStack> items, Set<Impurities.Impurity> impurities) implements ByProductConsumer {
        @Override
        public void accept(ItemStack stack) {
            items.add(stack);
        }

        @Override
        public void accept(ItemFluids stack) {
            fluids.add(stack);
            if (stack.isOf(PSFluids.GASOLINE)) {
                accept(Impurities.Impurity.GASOLINE);
            }
            if (stack.isOf(PSFluids.ETHANOL)) {
                accept(Impurities.Impurity.ETHANOL);
            }
        }

        public void accept(Impurities.Impurity impurity) {
            impurities.add(impurity);
        }
    }
}

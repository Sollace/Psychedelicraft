package ivorius.psychedelicraft.recipe;

import java.util.List;

import ivorius.psychedelicraft.fluid.Processable.ByProductConsumer;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;

public interface BunsenBurnerRecipe extends Recipe<BunsenBurnerRecipe.Input> {
    /**
     * The number of ticks it takes for this recipe to complete
     */
    int stewTime();

    @Override
    default RecipeType<?> getType() {
        return PSRecipes.BUNSEN_BURNER;
    }

    @Override
    default boolean fits(int width, int height) {
        return (width * height) > 0;
    }

    public record Input(FluidMound fluids, ItemMound input, ByProductConsumer consumer) implements RecipeInput {
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSize() {
            return 1;
        }
    }

    public record Product(FluidMound fluids, List<ItemStack> items) implements ByProductConsumer {
        @Override
        public void accept(ItemStack stack) {
            items.add(stack);
        }

        @Override
        public void accept(ItemFluids stack) {
            fluids.add(stack);
        }
    }
}

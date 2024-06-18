package ivorius.psychedelicraft.compat.tia;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack.TlaItemStack;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.FluidIngredient;
import net.minecraft.item.ItemStack;

interface RecipeUtil {
    static TlaStack toTlaStack(ItemFluids fluids) {
        return TlaStack.of(fluids.toVariant(), fluids.amount() / 1000);
    }

    static TlaStack toTlaStack(ItemStack receptical, ItemFluids fluids) {
        return TlaStack.of(ItemFluids.set(receptical.copy(), fluids.ofAmount(FluidCapacity.get(receptical))));
    }

    static TlaIngredient toFilled(TlaStack receptical, ItemFluids fluids) {
        return toTlaStack(((TlaItemStack)receptical).toStack(), fluids).asIngredient();
    }

    static TlaIngredient toIngredient(ItemStack receptical, ItemFluids fluids) {
        return toTlaStack(receptical, fluids).asIngredient();
    }

    static TlaIngredient toIngredient(FluidIngredient ingredient, int amount) {
        return toIngredient(ingredient.getAsItemFluid(amount));
    }

    static TlaIngredient toIngredient(ItemFluids fluids) {
        return toTlaStack(fluids).asIngredient();
    }

    static Stream<TlaIngredient> grouped(Stream<TlaIngredient> ingredients) {
        Map<TlaIngredient, Integer> counts = new HashMap<>();
        ingredients.forEach(ingredient -> counts.compute(ingredient, (key, count) -> count == null ? 1 : (count + 1)));
        return counts.entrySet()
                .stream()
                .map(entry -> entry.getKey().withAmount(entry.getValue()));
    }

    record Contents(ItemFluids type, TlaIngredient contents, TlaIngredient empty, TlaIngredient filled) {
        static Contents of(TlaStack receptical, ItemFluids fluid) {
            return new Contents(
                    fluid,
                    toIngredient(fluid),
                    toFilled(receptical, ItemFluids.EMPTY),
                    toFilled(receptical, fluid)
            );
        }
    }
}

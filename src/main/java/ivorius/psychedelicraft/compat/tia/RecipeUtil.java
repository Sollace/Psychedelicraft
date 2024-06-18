package ivorius.psychedelicraft.compat.tia;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack.TlaItemStack;
import ivorius.psychedelicraft.fluid.container.FluidRefillRegistry;
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

    static TlaIngredient toIngredient(ItemStack receptical, ItemFluids fluids) {
        return toTlaStack(receptical, fluids).asIngredient();
    }

    static TlaIngredient toIngredient(FluidIngredient ingredient, int amount) {
        return toIngredient(ingredient.getAsItemFluid(amount));
    }

    static TlaIngredient toIngredient(ItemFluids fluids) {
        return TlaIngredient.ofStacks(toTlaStack(fluids));
    }

    static Stream<TlaIngredient> grouped(Stream<TlaIngredient> ingredients) {
        Map<TlaIngredient, Integer> counts = new HashMap<>();
        ingredients.forEach(ingredient -> counts.compute(ingredient, (key, count) -> count == null ? 1 : (count + 1)));
        return counts.entrySet()
                .stream()
                .map(entry -> entry.getKey().withAmount(entry.getValue()));
    }

    record Contents(ItemFluids type, TlaIngredient empty, TlaIngredient filled, TlaIngredient fluid) {
        static Contents of(TlaStack baseReceptical, ItemFluids tankContents) {
            ItemStack emptyRecepticalStack = FluidRefillRegistry.toEmpty(((TlaItemStack)baseReceptical).toStack());
            return new Contents(
                    tankContents,
                    TlaStack.of(emptyRecepticalStack).asIngredient(),
                    toIngredient(emptyRecepticalStack, tankContents),
                    toIngredient(tankContents)
            );
        }
    }
}

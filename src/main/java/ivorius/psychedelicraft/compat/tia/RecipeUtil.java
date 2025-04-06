package ivorius.psychedelicraft.compat.tia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack.TlaFluidStack;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack.TlaItemStack;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.PSComponents;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import ivorius.psychedelicraft.util.compat.StackCompat;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.registry.Registries;

interface RecipeUtil {
    static TlaStack toTlaStack(ItemFluids fluids) {
        return TlaStack.of(fluids.toVariant(), fluids.amount() > 0 ? Math.max(1, fluids.amount() / 1000) : 0);
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

    static ItemFluids toItemFluids(TlaStack stack) {
        if (stack instanceof TlaFluidStack fl) {
            FluidVariant fluid = fl.getFluidVariant();
            return fluid.isBlank() ? ItemFluids.EMPTY : ItemFluids.of(fluid, Math.max(1, (int)stack.getAmount()));
        }

        if (stack instanceof TlaItemStack it) {
            Optional<? extends ItemFluids> fluids = Optional.ofNullable(StackCompat.get(it.getItemVariant(), PSComponents.FLUIDS));
            if (fluids != null && fluids.isPresent()) {
                return fluids.get();
            }
        }

        return ItemFluids.EMPTY;
    }

    static List<ItemFluids> getMatchingFluids(ItemFluids.Predicate predicate, int amount) {
        List<SimpleFluid> fluids = predicate.fluid().filter(l -> !l.isEmpty()).orElseGet(() -> Registries.FLUID.stream().map(SimpleFluid::of).toList());
        if (predicate.amount().max().isPresent()) {
            amount = Math.min(amount, predicate.amount().max().get());
        }
        if (predicate.amount().min().isPresent()) {
            amount = Math.max(amount, predicate.amount().min().get());
        }
        final int a = amount;
        return fluids.stream().map(fluid -> fluid.getDefaultStack(a)).toList();
    }

    static IntStream stream(IntRange range) {
        int from = range.min().orElse(0);
        int to = range.max().orElse(16);
        return IntStream.range(from, to + 1);
    }

    static Stream<TlaIngredient> grouped(Stream<TlaIngredient> ingredients) {
        Map<TlaIngredient, Integer> counts = new HashMap<>();
        ingredients.forEach(ingredient -> counts.compute(ingredient, (key, count) -> count == null ? 1 : (count + 1)));
        return counts.entrySet()
                .stream()
                .map(entry -> entry.getKey().withAmount(entry.getValue()));
    }

    record Contents(ItemFluids type, TlaIngredient contents, TlaIngredient empty, TlaIngredient filled) {
        static Contents of(TlaIngredient recepticals, ItemFluids fluid) {
            return new Contents(
                    fluid,
                    toIngredient(fluid),
                    TlaIngredient.join(recepticals.getStacks().stream().map(receptical -> toFilled(receptical, ItemFluids.EMPTY)).toList()),
                    TlaIngredient.join(recepticals.getStacks().stream().map(receptical -> toFilled(receptical, fluid)).toList())
            );
        }
    }
}

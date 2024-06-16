package ivorius.psychedelicraft.compat.emi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.PSComponents;
import ivorius.psychedelicraft.recipe.FluidIngredient;
import ivorius.psychedelicraft.recipe.OptionalFluidIngredient;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;

@Deprecated
interface RecipeUtil {
    static List<EmiIngredient> padIngredients(ShapedRecipe recipe) {
        List<EmiIngredient> list = Lists.newArrayList();
        int i = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (x >= recipe.getWidth() || y >= recipe.getHeight() || i >= recipe.getIngredients().size()) {
                    list.add(EmiStack.EMPTY);
                } else {
                    list.add(EmiIngredient.of(recipe.getIngredients().get(i++)));
                }
            }
        }
        return list;
    }

    static void replaceRecipe(EmiRegistry registry, EmiRecipe... rest) {
        Set<EmiRecipe> set = Set.of(rest);
        registry.removeRecipes(e -> e.getId().equals(rest[0].getId()) && !set.contains(e));
        for (var newRecipe : rest) {
            registry.addRecipe(newRecipe);
        }
    }

    static EmiIngredient convertIngredient(OptionalFluidIngredient ingredient) {
        return ingredient.receptical()
                .map(receptical -> mergeReceptical(receptical, ingredient.fluid()))
                .orElseGet(() -> ingredient.fluid().map(RecipeUtil::createFluidIngredient).orElse(EmiStack.EMPTY));
    }

    static EmiIngredient mergeReceptical(Ingredient receptical, Optional<FluidIngredient> contents) {
        return contents.map(fluid -> {
            var exploded = Arrays.stream(receptical.getMatchingStacks())
                    .map(stack -> mergeReceptical(stack, contents))
                    .toList();

            if (exploded.isEmpty()) {
                return EmiStack.EMPTY;
            }

            return new ListEmiIngredient(exploded, 1);
        }).orElseGet(() -> EmiIngredient.of(receptical));
    }

    static EmiStack mergeReceptical(ItemStack receptical, Optional<FluidIngredient> contents) {
        return contents.map(fluid -> {
            return EmiStack.of(ItemFluids.set(receptical.copy(), fluid.getAsItemFluid(FluidCapacity.get(receptical))));
        }).orElseGet(() -> EmiStack.of(receptical));
    }

    static SimpleFluid getFluid(EmiStack stack) {
        return getFluidStack(stack).fluid();
    }

    @SuppressWarnings("unchecked")
    static ItemFluids getFluidStack(EmiStack stack) {
        return ((Optional<ItemFluids>)stack.getComponentChanges().get(PSComponents.FLUIDS)).orElseGet(() -> {
            if (stack.getKey() instanceof Fluid f) {
                return SimpleFluid.forVanilla(f).getDefaultStack((int)stack.getAmount());
            }
            return ItemFluids.of(stack.getItemStack());
        });
    }

    static EmiStack createFluidIngredient(FluidIngredient ingredient) {
        return createFluidIngredient(ingredient.getAsItemFluid(FluidVolumes.BUCKET));
    }

    static EmiStack createFluidIngredient(ItemFluids fluids) {
        return EmiStack.of(getStackKey(fluids.fluid()), ComponentChanges.builder().add(PSComponents.FLUIDS, fluids).build(), 12);
    }

    static Fluid getStackKey(SimpleFluid fluid) {
        return fluid.getPhysical().getStandingFluid();
    }

    static Stream<EmiIngredient> grouped(Stream<EmiIngredient> ingredients) {
        Map<EmiIngredient, Integer> counts = new HashMap<>();
        ingredients.forEach(ingredient -> counts.compute(ingredient, (key, count) -> count == null ? 1 : (count + 1)));
        return counts.entrySet()
                .stream()
                .map(entry -> entry.getKey().copy().setAmount(entry.getValue()));
    }

}

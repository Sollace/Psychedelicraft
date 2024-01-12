package ivorius.psychedelicraft.compat.emi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;
import ivorius.psychedelicraft.recipe.FluidIngredient;
import ivorius.psychedelicraft.recipe.OptionalFluidIngredient;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;

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
            var exploded = Arrays.stream(receptical.getMatchingStacks()).map(stack -> {
                var container = FluidContainer.of(stack);
                var mutable = container.toMutable(stack);

                mutable.deposit(fluid.level(), fluid.fluid());

                return EmiStack.of(mutable.asStack());
            }).toList();

            if (exploded.isEmpty()) {
                return EmiStack.EMPTY;
            }

            return (EmiIngredient)new ListEmiIngredient(exploded, 1);
        }).orElseGet(() -> EmiIngredient.of(receptical));
    }

    static EmiStack mergeReceptical(ItemStack stack, Optional<FluidIngredient> contents) {
        return contents.map(fluid -> {
            var container = FluidContainer.of(stack);
            var mutable = container.toMutable(stack);

            mutable.deposit(fluid.level(), fluid.fluid());

            return EmiStack.of(mutable.asStack());
        }).orElseGet(() -> EmiStack.of(stack));
    }

    static SimpleFluid getFluid(EmiStack stack) {
        if (stack.getKey() instanceof Fluid f) {
            return SimpleFluid.forVanilla(f);
        }

        return FluidContainer.of(stack.getItemStack()).getFluid(stack.getItemStack());
    }

    static EmiStack createFluidIngredient(FluidIngredient ingredient) {
        return createFluidIngredient(ingredient.fluid(), ingredient.level(), ingredient.attributes().copy());
    }

    static EmiStack createFluidIngredient(SimpleFluid fluid, int level, @Nullable NbtCompound attributes) {
        return EmiStack.of(getStackKey(fluid), attributes, level <= 0 ? 12 : level / 12);
    }

    static EmiStack createFluidIngredient(ItemStack stack) {
        var container = MutableFluidContainer.of(stack);
        if (container.isEmpty()) {
            return EmiStack.of(stack);
        }
        return createFluidIngredient(container.getFluid(), container.getLevel(), container.getAttributes().copy());
    }

    static Fluid getStackKey(SimpleFluid fluid) {
        return fluid.getPhysical().getStandingFluid();
    }

    static Stream<EmiIngredient> grouped(Stream<EmiIngredient> ingredients) {
        Map<EmiIngredient, Integer> counts = new HashMap<>();
        ingredients.forEach(ingredient -> {
            counts.compute(ingredient, (key, count) -> {
                return count == null ? 1 : (count + 1);
            });
        });

        return counts.entrySet().stream().map(entry -> {
            return entry.getKey().copy().setAmount(entry.getValue());
        });
    }

}

package ivorius.psychedelicraft.compat.tia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaRecipe;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.FluidIngredient;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;

interface RecipeUtil {
    static List<TlaIngredient> padIngredients(ShapedRecipe recipe) {
        List<TlaIngredient> list = Lists.newArrayList();
        int i = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (x >= recipe.getWidth() || y >= recipe.getHeight() || i >= recipe.getIngredients().size()) {
                    list.add(TlaIngredient.EMPTY);
                } else {
                    list.add(TlaIngredient.ofIngredient(recipe.getIngredients().get(i++)));
                }
            }
        }
        return list;
    }

    static void replaceRecipe(PluginContext registry, TlaRecipe... rest) {
        Set<TlaRecipe> set = Set.of(rest);
        registry.addGenerator(client -> {
            return set.stream().toList();
        });
        //registry.removeRecipes(e -> e.getId().equals(rest[0].getId()) && !set.contains(e));
    }

    static TlaStack toTlaStack(ItemFluids fluids) {
        return TlaStack.of(fluids.toVariant(), fluids.amount() / 1000);
    }

    static TlaStack toTlaStack(ItemStack receptical, ItemFluids fluids) {
        return TlaStack.of(ItemFluids.set(receptical.copy(), fluids.ofAmount(FluidCapacity.get(receptical))));
    }

    static TlaIngredient toIngredient(ItemStack receptical, ItemFluids fluids) {
        return TlaIngredient.ofStacks(toTlaStack(receptical, fluids));
    }

    static TlaIngredient toIngredient(FluidIngredient ingredient, int amount) {
        return toIngredient(ingredient.getAsItemFluid(amount));
    }

    static TlaIngredient toIngredient(ItemFluids fluids) {
        return TlaIngredient.ofStacks(toTlaStack(fluids));
    }

    static Fluid getStackKey(SimpleFluid fluid) {
        return fluid.getPhysical().getStandingFluid();
    }

    static Stream<TlaIngredient> grouped(Stream<TlaIngredient> ingredients) {
        Map<TlaIngredient, Integer> counts = new HashMap<>();
        ingredients.forEach(ingredient -> counts.compute(ingredient, (key, count) -> count == null ? 1 : (count + 1)));
        return counts.entrySet()
                .stream()
                .map(entry -> entry.getKey().withAmount(entry.getValue()));
    }

}

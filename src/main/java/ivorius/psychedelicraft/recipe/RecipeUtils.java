/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.DefaultedList;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import ivorius.psychedelicraft.fluid.container.FluidContainer;

public interface RecipeUtils {
    Codec<DefaultedList<Ingredient>> SHAPELESS_RECIPE_INGREDIENTS_CODEC = Ingredient.DISALLOW_EMPTY_CODEC.listOf().flatXmap(ingredients -> {
        Ingredient[] ingredients2 = ingredients.stream().filter(ingredient -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
        if (ingredients2.length == 0) {
            return DataResult.error(() -> "No ingredients for shapeless recipe");
        }
        if (ingredients2.length > 9) {
            return DataResult.error(() -> "Too many ingredients for shapeless recipe");
        }
        return DataResult.success(DefaultedList.copyOf(Ingredient.EMPTY, ingredients2));
    }, DataResult::success);
    Codec<ItemStack> ITEM_STACK_CODEC = Registries.ITEM.getCodec().xmap(ItemStack::new, ItemStack::getItem);

    static Stream<Map.Entry<FluidContainer, ItemStack>> recepticals(Inventory inventory) {
        return stacks(inventory)
            .filter(stack -> stack.getItem() instanceof FluidContainer)
            .map(stack -> Map.entry(FluidContainer.of(stack), stack));
    }

    static Stream<ItemStack> stacks(Inventory inventory) {
        return IntStream.range(0, inventory.size())
                .mapToObj(inventory::getStack)
                .filter(s -> !s.isEmpty());
    }

    static Stream<Slot<Map.Entry<FluidContainer, ItemStack>>> recepticalSlots(Inventory inventory) {
        return slots(inventory, stack -> stack.getItem() instanceof FluidContainer, stack -> {
            return Map.entry(FluidContainer.of(stack), stack);
        });
    }

    static <T> Stream<Slot<T>> slots(Inventory inventory, Predicate<ItemStack> filter, Function<ItemStack, T> func) {
        return IntStream.range(0, inventory.size())
                .filter(i -> filter.test(inventory.getStack(i)))
                .mapToObj(i -> new Slot<>(inventory, func.apply(inventory.getStack(i)), i));
    }

    static DefaultedList<Ingredient> union(DefaultedList<Ingredient> list, Ingredient...additional) {
        for (Ingredient ingredient : additional) {
            if (ingredient.isEmpty()) continue;
            list.add(ingredient);
        }
        return list;
    }

    static <T> DefaultedList<T> checkLength(DefaultedList<T> ingredients) {
        if (ingredients.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
        }
        if (ingredients.size() > 9) {
            throw new JsonParseException("Too many ingredients for shapeless recipe");
        }
        return ingredients;
    }

    static Optional<ItemStack> consume(Inventory inventory, Predicate<ItemStack> filter) {
        return IntStream.range(0, inventory.size())
                .filter(i -> filter.test(inventory.getStack(i)))
                .mapToObj(i -> inventory.getStack(i).split(1))
                .findFirst();
    }

    record Slot<T>(Inventory inventory, T content, int slot) {
        public void set(ItemStack stack) {
            inventory.setStack(slot, stack);
        }

        public <V> V map(Function<T, V> func) {
            return func.apply(content);
        }
    }
}

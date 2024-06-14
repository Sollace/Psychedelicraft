/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.util.collection.DefaultedList;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.util.PacketCodecUtils;

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

    PacketCodec<RegistryByteBuf, CraftingRecipeCategory> CRAFTING_RECIPE_CATEGORY_PACKET_CODEC = PacketCodecUtils.ofEnum(CraftingRecipeCategory.class);
    PacketCodec<RegistryByteBuf, CookingRecipeCategory> COOKING_RECIPE_CATEGORY_PACKET_CODEC = PacketCodecUtils.ofEnum(CookingRecipeCategory.class);
    PacketCodec<RegistryByteBuf, DefaultedList<Ingredient>> INGREDIENTS_PACKET_CODEC = Ingredient.PACKET_CODEC.collect(PacketCodecUtils.toDefaultedList(Ingredient.empty()));

    static Stream<ItemStack> recepticals(Stream<ItemStack> stacks) {
        return stacks.filter(stack -> FluidCapacity.get(stack) > 0);
    }

    static Stream<Entry<ItemStack>> recepticalSlots(RecipeInput input) {
        return slots(input, stack -> FluidCapacity.get(stack) > 0, Function.identity());
    }

    static Stream<ItemStack> stacks(Inventory inventory) {
        return IntStream.range(0, inventory.size())
                .mapToObj(inventory::getStack)
                .filter(s -> !s.isEmpty());
    }

    static <T> Stream<Entry<T>> slots(RecipeInput input, Predicate<ItemStack> filter, Function<ItemStack, T> func) {
        return IntStream.range(0, input.getSize())
                .filter(i -> filter.test(input.getStackInSlot(i)))
                .mapToObj(i -> new Entry<>(func.apply(input.getStackInSlot(i)), i));
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

    static <T> T iDontCareWhich(Either<T, T> either) {
        return either.left().or(either::right).orElseThrow();
    }

    record Entry<T>(T content, int position) {}

    record Slot<T>(Inventory inventory, T content, int slot) {
        public void set(ItemStack stack) {
            inventory.setStack(slot, stack);
        }

        public <V> V map(Function<T, V> func) {
            return func.apply(content);
        }
    }
}

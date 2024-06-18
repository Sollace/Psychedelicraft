/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;

/**
 * Created from by Sollace on 7 Feb 2023
 *
 * Used by the mash table to produce a particular fluid from items dropped in.
 */
public record MashingRecipe (
        String group,
        CraftingRecipeCategory category,
        ItemFluids baseFluid,
        ItemFluids result,
        DefaultedList<Ingredient> ingredients,
        int stewTime) implements Recipe<MashingRecipe.Input> {
    public static final MapCodec<MashingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(MashingRecipe::group),
            CraftingRecipeCategory.CODEC.optionalFieldOf("category", CraftingRecipeCategory.MISC).forGetter(MashingRecipe::category),
            ItemFluids.CODEC.fieldOf("base_fluid").forGetter(MashingRecipe::baseFluid),
            ItemFluids.CODEC.fieldOf("result").forGetter(MashingRecipe::result),
            RecipeUtils.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(MashingRecipe::ingredients),
            Codec.INT.optionalFieldOf("stew_time", 0).forGetter(MashingRecipe::stewTime)
    ).apply(instance, MashingRecipe::new));
    public static final PacketCodec<RegistryByteBuf, MashingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, MashingRecipe::group,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, MashingRecipe::category,
            ItemFluids.PACKET_CODEC, MashingRecipe::baseFluid,
            ItemFluids.PACKET_CODEC, MashingRecipe::result,
            RecipeUtils.INGREDIENTS_PACKET_CODEC, MashingRecipe::ingredients,
            PacketCodecs.INTEGER, MashingRecipe::stewTime,
            MashingRecipe::new
    );

    @Override
    public RecipeType<?> getType() {
        return PSRecipes.MASHING_TYPE;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.MASHING;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public ItemStack createIcon() {
        return PSItems.MASH_TUB.getDefaultStack();
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return ingredients;
    }

    public boolean hasMinimumRequirements(Input input) {
        Object2IntMap<Item> unmatchedInputs = new Object2IntOpenHashMap<>(input.inputs());
        List<Ingredient> unmatchedIngredients = new ArrayList<>(ingredients);

        var iter = unmatchedIngredients.iterator();
        while (iter.hasNext()) {
            Ingredient ingredient = iter.next();

            for (Item item : unmatchedInputs.keySet()) {
                ItemStack stack = item.getDefaultStack();
                if (ingredient.test(stack)) {
                    iter.remove();
                    int count = unmatchedInputs.getInt(item);
                    if (count <= 1) {
                        unmatchedInputs.removeInt(item);
                    } else {
                        unmatchedInputs.put(item, count - 1);
                    }
                    break;
                }
            }
        }

        if (unmatchedIngredients.isEmpty()) {
            return true;
        }
        if (!unmatchedIngredients.isEmpty()) {
            return false;
        }

        return unmatchedIngredients.stream().allMatch(i -> {
            return input.inputs().keySet().stream().anyMatch(item -> i.test(item.getDefaultStack()));
        });
    }

    public boolean hasUndesiredIngredients(Input input) {
        return input.inputs().keySet().stream().anyMatch(item -> {
            ItemStack stack = item.getDefaultStack();
            return ingredients.stream().noneMatch(i -> i.test(stack));
        });
    }

    @Override
    public boolean matches(Input input, World world) {
        return !input.tankFluid().isEmpty() && baseFluid.canCombine(input.tankFluid()) && !hasUndesiredIngredients(input);
    }

    @Override
    public ItemStack craft(Input input, WrapperLookup lookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 0;
    }

    @Override
    public ItemStack getResult(WrapperLookup lookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(Input input) {
        Object2IntMap<Item> unmatchedInputs = new Object2IntOpenHashMap<>(input.inputs());

        for (Ingredient ingredient : ingredients) {
            for (Item item : unmatchedInputs.keySet()) {
                ItemStack stack = item.getDefaultStack();
                if (ingredient.test(stack)) {
                    unmatchedInputs.computeInt(item, (i, count) -> count == 1 ? null : (count - 1));
                    break;
                }
            }
        }

        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(unmatchedInputs.size());
        unmatchedInputs.forEach((item, count) -> {
            while (count > 0) {
                ItemStack stack = new ItemStack(item, Math.min(item.getMaxCount(), count));
                count-= stack.getCount();
                stacks.add(stack);
            }
        });
        return stacks;
    }

    public record Input(ItemFluids tankFluid, ItemStack solids, Object2IntMap<Item> inputs) implements RecipeInput {
        @Override
        public ItemStack getStackInSlot(int slot) {
            return solids;
        }

        @Override
        public int getSize() {
            return 1;
        }
    }

    public enum MatchResult {
        NONE,
        INPUTS_ONLY,
        INGREDIENTS_ONLY,
        BOTH;

        public boolean isMatch() {
            return this == INPUTS_ONLY || this == BOTH;
        }

        public boolean isCraftable() {
            return this == BOTH;
        }

        public static MatchResult of(boolean inputs, boolean ingredients) {
            return inputs && ingredients ? BOTH : inputs ? INPUTS_ONLY : ingredients ? INGREDIENTS_ONLY : NONE;
        }
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.component.ItemFluids;

/**
 * Created by Sollace on 7 Feb 2023
 *
 * Used by the mash table to produce a particular fluid from items dropped in.
 */
public record MashingRecipe (
        String mashingGroup,
        CraftingRecipeCategory category,
        ItemFluids.Predicate baseFluid,
        ItemFluids result,
        Ingredients ingredients,
        int stewTime) implements Recipe<MashingRecipe.Input> {
    public static final MapCodec<MashingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(MashingRecipe::mashingGroup),
            CraftingRecipeCategory.CODEC.optionalFieldOf("category", CraftingRecipeCategory.MISC).forGetter(MashingRecipe::category),
            ItemFluids.Predicate.CODEC.fieldOf("base_fluid").forGetter(MashingRecipe::baseFluid),
            ItemFluids.CODEC.fieldOf("result").forGetter(MashingRecipe::result),
            Ingredients.CODEC.fieldOf("ingredients").forGetter(MashingRecipe::ingredients),
            Codec.INT.optionalFieldOf("stew_time", 0).forGetter(MashingRecipe::stewTime)
    ).apply(instance, MashingRecipe::new));
    public static final PacketCodec<RegistryByteBuf, MashingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, MashingRecipe::mashingGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, MashingRecipe::category,
            ItemFluids.Predicate.PACKET_CODEC, MashingRecipe::baseFluid,
            ItemFluids.PACKET_CODEC, MashingRecipe::result,
            Ingredients.PACKET_CODEC, MashingRecipe::ingredients,
            PacketCodecs.INTEGER, MashingRecipe::stewTime,
            MashingRecipe::new
    );

    @Override
    public RecipeType<MashingRecipe> getType() {
        return PSRecipes.MASHING_TYPE;
    }

    @Override
    public RecipeSerializer<MashingRecipe> getSerializer() {
        return PSRecipes.MASHING;
    }

    @Override
    public String getGroup() {
        return mashingGroup;
    }

    @Override
    public boolean matches(Input input, World world) {
        return !input.tankFluid().isEmpty()
                && baseFluid.test(input.tankFluid())
                && ingredients.matches(input);
    }

    public boolean matchesPartially(Input input, World world) {
        return !input.tankFluid().isEmpty()
                && baseFluid.test(input.tankFluid())
                && ingredients.includes(input);
    }

    public boolean isAcceptableIngredient(ItemFluids vatFluids, ItemStack stack) {
        return baseFluid().test(vatFluids)
            && ingredients.ingredients().stream().anyMatch(i -> i.test(stack));
    }

    @Override
    public ItemStack craft(Input input, WrapperLookup lookup) {
        return ItemStack.EMPTY;
    }

    public DefaultedList<ItemStack> getRemainder(Input input) {
        ItemMound unmatchedInputs = new ItemMound(input.inputs());
        ingredients.removeMatches(unmatchedInputs);
        return unmatchedInputs.convertToItemStacks();
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public record Input(ItemFluids tankFluid, ItemStack solids, ItemMound inputs) implements RecipeInput {
        @Override
        public ItemStack getStackInSlot(int slot) {
            return solids;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return solids.isEmpty() && tankFluid.isEmpty() && inputs.isEmpty();
        }
    }

    public record Ingredients (List<Entry> counts, List<Ingredient> ingredients) {
        public static final Codec<Ingredients> CODEC = Entry.CODEC.listOf().xmap(Ingredients::new, Ingredients::counts);
        public static final PacketCodec<RegistryByteBuf, Ingredients> PACKET_CODEC = Entry.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(Ingredients::new, Ingredients::counts);

        public Ingredients(List<Entry> counts) {
            this(counts, counts.stream().flatMap(i -> i.ingredient().stream()).toList());
        }

        public boolean matches(Input input) {
            return removeMatches(new ItemMound(input.inputs()));
        }

        public boolean includes(Input input) {
            return !input.inputs().isEmpty()
                && ingredients.stream().anyMatch(i -> input.inputs().countMatches(i) > 0);
        }

        /**
         * @param inputs The mound of items to consume
         * @return True if all ingredients are satisfied
         */
        public boolean removeMatches(ItemMound inputs) {
            return counts().stream().filter(ingredient -> {
                return !inputs.removeWhere(ingredient.ingredient().orElse(null), ingredient.minimum());
            }).count() == 0;
        }

        public record Entry(Optional<Ingredient> ingredient, int minimum) {
            public static final Entry EMPTY = new Entry(Optional.empty(), 0);
            public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Ingredient.CODEC.optionalFieldOf("ingredient").forGetter(Entry::ingredient),
                    Codec.INT.fieldOf("count").forGetter(Entry::minimum)
            ).apply(i, Entry::new));
            public static final PacketCodec<RegistryByteBuf, Entry> PACKET_CODEC = PacketCodec.tuple(
                    PacketCodecs.optional(Ingredient.PACKET_CODEC), Entry::ingredient,
                    PacketCodecs.INTEGER, Entry::minimum,
                    Entry::new
            );
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

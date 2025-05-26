/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.CodecUtils;
import ivorius.psychedelicraft.util.PacketCodecUtils;
import ivorius.psychedelicraft.util.compat.IngredientCompat;
import ivorius.psychedelicraft.util.compat.PacketCodec;
import ivorius.psychedelicraft.util.compat.PacketCodecs;
import ivorius.psychedelicraft.util.compat.RecipeInput;

/**
 * Created by Sollace on 7 Feb 2023
 *
 * Used by the mash table to produce a particular fluid from items dropped in.
 */
public record MashingRecipe (
        Identifier id,
        String mashingGroup,
        CraftingRecipeCategory category,
        ItemFluids.Predicate baseFluid,
        ItemFluids result,
        Ingredients ingredients,
        int stewTime) implements Recipe<MashingRecipe.Input> {
    public static final MapCodec<MashingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(MashingRecipe::getId),
            Codec.STRING.optionalFieldOf("group", "").forGetter(MashingRecipe::mashingGroup),
            CraftingRecipeCategory.CODEC.optionalFieldOf("category", CraftingRecipeCategory.MISC).forGetter(MashingRecipe::category),
            ItemFluids.Predicate.CODEC.fieldOf("base_fluid").forGetter(MashingRecipe::baseFluid),
            ItemFluids.CODEC.fieldOf("result").forGetter(MashingRecipe::result),
            Ingredients.CODEC.fieldOf("ingredients").forGetter(MashingRecipe::ingredients),
            Codec.INT.optionalFieldOf("stew_time", 0).forGetter(MashingRecipe::stewTime)
    ).apply(instance, MashingRecipe::new));
    public static final PacketCodec<PacketByteBuf, MashingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.IDENTIFIER, MashingRecipe::id,
            PacketCodecs.STRING, MashingRecipe::mashingGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, MashingRecipe::category,
            ItemFluids.Predicate.PACKET_CODEC, MashingRecipe::baseFluid,
            ItemFluids.PACKET_CODEC, MashingRecipe::result,
            Ingredients.PACKET_CODEC, MashingRecipe::ingredients,
            PacketCodecs.INTEGER, MashingRecipe::stewTime,
            MashingRecipe::new
    );

    @Override
    public Identifier getId() {
        return id;
    }

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
        return mashingGroup;
    }

    @Override
    public ItemStack createIcon() {
        return PSItems.MASH_TUB.getDefaultStack();
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return ingredients.ingredients();
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

    @Override
    public ItemStack craft(Input input, DynamicRegistryManager lookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 0;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager lookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(Input input) {
        ItemMound unmatchedInputs = new ItemMound(input.inputs());
        ingredients.removeMatches(unmatchedInputs);
        return unmatchedInputs.convertToItemStacks();
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    public record Input(ItemFluids tankFluid, ItemStack solids, ItemMound inputs) implements RecipeInput {
        @Override
        public ItemStack getStackInSlot(int slot) {
            return solids;
        }

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return solids.isEmpty() && tankFluid.isEmpty() && inputs.isEmpty();
        }
    }

    public record Ingredients (DefaultedList<Entry> counts, DefaultedList<Ingredient> ingredients) {
        public static final Codec<Ingredients> CODEC = CodecUtils.toDefaultedList(Entry.CODEC, Entry.EMPTY).xmap(Ingredients::new, Ingredients::counts);
        public static final PacketCodec<PacketByteBuf, Ingredients> PACKET_CODEC = Entry.PACKET_CODEC.collect(PacketCodecUtils.toDefaultedList()).xmap(Ingredients::new, Ingredients::counts);

        public Ingredients(DefaultedList<Entry> counts) {
            this(counts, DefaultedList.copyOf(Ingredient.EMPTY, counts.stream().map(Entry::ingredient).toArray(Ingredient[]::new)));
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
                return !inputs.removeWhere(ingredient.ingredient(), ingredient.minimum());
            }).count() == 0;
        }

        public record Entry(Ingredient ingredient, int minimum) {
            public static final Entry EMPTY = new Entry(Ingredient.EMPTY, 0);
            public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                    IngredientCompat.DISALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(Entry::ingredient),
                    Codec.INT.fieldOf("count").forGetter(Entry::minimum)
            ).apply(i, Entry::new));
            public static final PacketCodec<PacketByteBuf, Entry> PACKET_CODEC = PacketCodec.tuple(
                    PacketCodecs.INGREDIENT, Entry::ingredient,
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

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

import java.util.*;

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

    public MatchResult matchPartially(Input input) {
        final List<Ingredient> expectedInputs = new ArrayList<>(ingredients);
        final Object2IntMap<Item> unmatchedInputs = new Object2IntOpenHashMap<>(input.inputs());

        for (Item item : input.inputs().keySet()) {
            ItemStack stack = item.getDefaultStack();

            if (expectedInputs.isEmpty()) {
                // fail match as the supplied ingredients exceeds the expected inputs
                return MatchResult.NONE;
            }

            Iterator<Ingredient> iter = expectedInputs.iterator();

            while (iter.hasNext()) {
                Ingredient ingredient = iter.next();
                if (ingredient.test(stack)) {
                    iter.remove();
                    if (!unmatchedInputs.containsKey(item)) {
                        return MatchResult.of(unmatchedInputs.isEmpty(), expectedInputs.isEmpty());
                    }

                    unmatchedInputs.computeInt(item, (s, i) -> i <= 1 ? null : i - 1);

                    if (unmatchedInputs.isEmpty()) {
                        // succeed if all supplied inputs are matched.
                        // The recipe might be expecting more additional inputs.
                        // We don't actually care. Crafting is done when only one recipe fits our current selection.
                        return MatchResult.of(true, expectedInputs.isEmpty());
                    }
                }
            }
        }

        return MatchResult.of(unmatchedInputs.isEmpty(), expectedInputs.isEmpty());
    }

    @Override
    public boolean matches(Input input, World world) {
        return baseFluid.canCombine(input.tankFluid());
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

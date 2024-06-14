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
import net.minecraft.util.collection.DefaultedList;
import java.util.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ivorius.psychedelicraft.item.PSItems;

/**
 * Created from by Sollace on 7 Feb 2023
 *
 * Used by the mash table to produce a particular fluid from items dropped in.
 */
public class MashingRecipe extends FillRecepticalRecipe {
    public static final MapCodec<MashingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(MashingRecipe::getGroup),
            CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(MashingRecipe::getCategory),
            FluidIngredient.CODEC.fieldOf("result").forGetter(MashingRecipe::getOutputFluid),
            FluidIngredient.CODEC.fieldOf("base_fluid").forGetter(MashingRecipe::getPoolFluid),
            RecipeUtils.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(MashingRecipe::getIngredients),
            Codec.INT.optionalFieldOf("stew_time", 0).forGetter(recipe -> recipe.stewTime)
    ).apply(instance, MashingRecipe::new));
    public static final PacketCodec<RegistryByteBuf, MashingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, MashingRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, MashingRecipe::getCategory,
            FluidIngredient.PACKET_CODEC, MashingRecipe::getOutputFluid,
            FluidIngredient.PACKET_CODEC, MashingRecipe::getPoolFluid,
            RecipeUtils.INGREDIENTS_PACKET_CODEC, MashingRecipe::getIngredients,
            PacketCodecs.INTEGER, recipe -> recipe.stewTime,
            MashingRecipe::new
    );

    private final int stewTime;

    private final FluidIngredient fluid;

    public MashingRecipe(
            String group,
            CraftingRecipeCategory category,
            FluidIngredient output,
            FluidIngredient fluid,
            DefaultedList<Ingredient> input,
            int stewTime) {
        super(group, category, output, Ingredient.ofItems(PSItems.MASH_TUB), input);
        this.fluid = fluid;
        this.stewTime = stewTime;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.MASHING;
    }

    @Override
    public RecipeType<?> getType() {
        return PSRecipes.MASHING_TYPE;
    }

    public int getStewTime() {
        return stewTime;
    }

    public FluidIngredient getPoolFluid() {
        return fluid;
    }

    public MatchResult matchPartially(Object2IntMap<Item> inputs) {
        final List<Ingredient> expectedInputs = new ArrayList<>(getIngredients());
        final Object2IntMap<Item> unmatchedInputs = new Object2IntOpenHashMap<>(inputs);

        for (Item item : inputs.keySet()) {
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

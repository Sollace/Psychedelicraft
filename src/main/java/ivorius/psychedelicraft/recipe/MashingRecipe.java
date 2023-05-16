/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

import com.google.gson.*;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * Created from by Sollace on 7 Feb 2023
 *
 * Used by the mash table to produce a particular fluid from items dropped in.
 */
public class MashingRecipe extends FillRecepticalRecipe {

    private final int stewTime;

    private final FluidIngredient fluid;

    public MashingRecipe(Identifier id, String group, CraftingRecipeCategory category, FluidIngredient output, FluidIngredient fluid, DefaultedList<Ingredient> input, int stewTime) {
        super(id, group, category, output, input);
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

    static class Serializer implements RecipeSerializer<MashingRecipe> {
        @SuppressWarnings("deprecation")
        @Override
        public MashingRecipe read(Identifier id, JsonObject json) {
            return new MashingRecipe(id,
                    JsonHelper.getString(json, "group", ""),
                    CraftingRecipeCategory.CODEC.byId(JsonHelper.getString(json, "category", null), CraftingRecipeCategory.MISC),
                    FluidIngredient.fromJson(JsonHelper.getObject(json, "result")),
                    FluidIngredient.fromJson(JsonHelper.getObject(json, "base_fluid")),
                    RecipeUtils.getIngredients(JsonHelper.getArray(json, "ingredients")),
                    JsonHelper.getInt(json, "stew_time", 0)
            );
        }

        @Override
        public MashingRecipe read(Identifier id, PacketByteBuf buffer) {
            return new MashingRecipe(id,
                    buffer.readString(),
                    buffer.readEnumConstant(CraftingRecipeCategory.class),
                    new FluidIngredient(buffer),
                    new FluidIngredient(buffer),
                    buffer.readCollection(DefaultedList::ofSize, Ingredient::fromPacket),
                    buffer.readVarInt()
            );
        }

        @Override
        public void write(PacketByteBuf buffer, MashingRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            recipe.getOutputFluid().write(buffer);
            recipe.getPoolFluid().write(buffer);
            buffer.writeCollection(recipe.getIngredients(), (b, c) -> c.write(b));
            buffer.writeVarInt(recipe.getStewTime());
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

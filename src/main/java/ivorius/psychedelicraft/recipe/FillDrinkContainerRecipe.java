/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.*;

import com.google.gson.*;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ivorius.psychedelicraft.fluid.FluidContainer;

/**
 * Created from "RecipeFillDrink" by Sollace on 5 Jan 2023
 * Original by lukas on 21.10.14.
 * Recipe that takes as a config:
 * - Input Ingrediences (unshaped)
 * - Input Container
 * - Preconfigured fluid+level
 *
 * Outputs:
 * - Original Container filled with assigned fluid and level
 *
 *
 *  {
 *    "type": "psychedelicraft:fill_drink_container",
 *    "ingredients": [
 *       { "item": "..." },
 *       ...
 *    ],
 *    "result": {
 *      "fluid": "psychedelicraft:coffee",
 *      "level": 500,
 *      "attributes": {}
 *    }
 *  }
 *
 */
public class FillDrinkContainerRecipe extends ShapelessRecipe {
    private final FluidIngredient output;

    public FillDrinkContainerRecipe(Identifier id, String group, CraftingRecipeCategory category, FluidIngredient output, DefaultedList<Ingredient> input) {
        super(id, group, category, ItemStack.EMPTY, input);
        this.output = output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.FILL_DRINK_CONTAINER;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        RecipeMatcher recipeMatcher = new RecipeMatcher();
        return RecipeUtils.recepticals(inventory).count() == 1
                && RecipeUtils.stacks(inventory).filter(stack -> {
                    recipeMatcher.addInput(stack, 1);
                    return true;
                }).count() == getIngredients().size()
                && recipeMatcher.match(this, null);
    }

    public MatchResult matchPartially(Object2IntMap<Item> inputs) {
        final List<Ingredient> expectedInputs = new ArrayList<>(getIngredients());
        final Object2IntMap<Item> unmatchedInputs = new Object2IntOpenHashMap<>(inputs);

        expectedInputs.removeIf(ingredient -> {
            return Arrays.stream(ingredient.getMatchingStacks()).anyMatch(stack -> FluidContainer.of(stack, null) != null);
        });

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

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        return RecipeUtils.recepticals(inventory).findFirst().map(receptical -> {
            ItemStack stack = output.fluid().getDefaultStack(receptical.getKey(), output.level() <= 0 ? receptical.getKey().getMaxCapacity(receptical.getValue()) : output.level());
            stack.getOrCreateSubNbt("fluid").copyFrom(output.attributes());
            return stack;
        }).orElse(ItemStack.EMPTY);
    }

    public FluidIngredient getOutputFluid() {
        return output;
    }

    static class Serializer implements RecipeSerializer<FillDrinkContainerRecipe> {
        @SuppressWarnings("deprecation")
        @Override
        public FillDrinkContainerRecipe read(Identifier id, JsonObject json) {
            return new FillDrinkContainerRecipe(id,
                    JsonHelper.getString(json, "group", ""),
                    CraftingRecipeCategory.CODEC.byId(JsonHelper.getString(json, "category", null), CraftingRecipeCategory.MISC),
                    FluidIngredient.fromJson(JsonHelper.getObject(json, "result")),
                    RecipeUtils.checkLength(RecipeUtils.getIngredients(JsonHelper.getArray(json, "ingredients")))
            );
        }

        @Override
        public FillDrinkContainerRecipe read(Identifier id, PacketByteBuf buffer) {
            return new FillDrinkContainerRecipe(id,
                    buffer.readString(),
                    buffer.readEnumConstant(CraftingRecipeCategory.class),
                    new FluidIngredient(buffer),
                    buffer.readCollection(i -> DefaultedList.ofSize(i, Ingredient.EMPTY), Ingredient::fromPacket)
            );
        }

        @Override
        public void write(PacketByteBuf buffer, FillDrinkContainerRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            recipe.output.write(buffer);
            buffer.writeCollection(recipe.getIngredients(), (b, c) -> c.write(b));
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

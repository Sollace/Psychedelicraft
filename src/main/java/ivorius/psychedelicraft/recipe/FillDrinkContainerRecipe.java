/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import com.google.gson.*;

import ivorius.psychedelicraft.fluid.FluidContainerItem;

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
class FillDrinkContainerRecipe extends ShapelessRecipe {
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
                    if (stack.getItem() instanceof FluidContainerItem) {
                        return false;
                    }
                    recipeMatcher.addInput(stack, 1);
                    return true;
                }).count() == getIngredients().size()
                && recipeMatcher.match(this, null);
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        return RecipeUtils.recepticals(inventory).findFirst().map(receptical -> {
            ItemStack stack = output.fluid().getDefaultStack(receptical.getKey(), output.level());
            stack.getOrCreateSubNbt("fluid").copyFrom(output.attributes());
            return stack;
        }).orElse(ItemStack.EMPTY);
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
}

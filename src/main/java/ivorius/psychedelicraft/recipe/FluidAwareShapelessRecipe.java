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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.*;

public class FluidAwareShapelessRecipe extends ShapelessRecipe {

    private final DefaultedList<OptionalFluidIngredient> ingredients;

    public FluidAwareShapelessRecipe(Identifier id, String group, CraftingRecipeCategory category, ItemStack output,
            DefaultedList<OptionalFluidIngredient> input) {
        super(id, group, category, output, input.stream().map(i -> i.receptical().orElse(Ingredient.EMPTY)).collect(Collectors.toCollection(DefaultedList::of)));
        this.ingredients = input;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.SHAPELESS_FLUID;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        List<OptionalFluidIngredient> unmatchedInputs = new ArrayList<>(ingredients);
        return RecipeUtils.stacks(inventory)
                    .filter(stack -> unmatchedInputs.stream()
                        .filter(ingredient -> ingredient.test(stack))
                        .findFirst()
                        .map(unmatchedInputs::remove)
                        .orElse(false)).count() == ingredients.size() && unmatchedInputs.isEmpty();
    }

    static class Serializer implements RecipeSerializer<FluidAwareShapelessRecipe> {
        @SuppressWarnings("deprecation")
        @Override
        public FluidAwareShapelessRecipe read(Identifier id, JsonObject json) {
            return new FluidAwareShapelessRecipe(id,
                    JsonHelper.getString(json, "group", ""),
                    CraftingRecipeCategory.CODEC.byId(JsonHelper.getString(json, "category", null), CraftingRecipeCategory.MISC),
                    ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result")),
                    RecipeUtils.checkLength(OptionalFluidIngredient.fromJsonArray(JsonHelper.getArray(json, "ingredients")))
            );
        }

        @Override
        public FluidAwareShapelessRecipe read(Identifier id, PacketByteBuf buffer) {
            return new FluidAwareShapelessRecipe(id,
                    buffer.readString(),
                    buffer.readEnumConstant(CraftingRecipeCategory.class),
                    buffer.readItemStack(),
                    buffer.readCollection(i -> DefaultedList.ofSize(i, OptionalFluidIngredient.EMPTY), OptionalFluidIngredient::new)
            );
        }

        @Override
        public void write(PacketByteBuf buffer, FluidAwareShapelessRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            buffer.writeItemStack(recipe.getOutput());
            buffer.writeCollection(recipe.getIngredients(), (b, c) -> c.write(b));
        }
    }
}

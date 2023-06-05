/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import com.google.gson.*;

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
 */
public class FillRecepticalRecipe extends ShapelessRecipe {
    private final FluidIngredient output;

    public FillRecepticalRecipe(Identifier id, String group, CraftingRecipeCategory category, FluidIngredient output, DefaultedList<Ingredient> input) {
        super(id, group, category, ItemStack.EMPTY, input);
        this.output = output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.FILL_RECEPTICAL;
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        RecipeMatcher recipeMatcher = new RecipeMatcher();
        return RecipeUtils.recepticals(inventory).count() == 1
                && RecipeUtils.stacks(inventory).filter(stack -> {
                    recipeMatcher.addInput(stack, 1);
                    return true;
                }).count() == getIngredients().size()
                && recipeMatcher.match(this, null);
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registries) {
        return RecipeUtils.recepticals(inventory).findFirst().map(receptical -> {
            ItemStack stack = output.fluid().getDefaultStack(receptical.getKey(), output.level() <= 0
                ? receptical.getKey().getMaxCapacity(receptical.getValue())
                : (output.level() + receptical.getKey().getLevel(receptical.getValue()))
            );
            stack.getOrCreateSubNbt("fluid").copyFrom(output.attributes());
            return stack;
        }).orElse(ItemStack.EMPTY);
    }

    public FluidIngredient getOutputFluid() {
        return output;
    }

    static class Serializer implements RecipeSerializer<FillRecepticalRecipe> {
        @SuppressWarnings("deprecation")
        @Override
        public FillRecepticalRecipe read(Identifier id, JsonObject json) {
            return new FillRecepticalRecipe(id,
                    JsonHelper.getString(json, "group", ""),
                    CraftingRecipeCategory.CODEC.byId(JsonHelper.getString(json, "category", null), CraftingRecipeCategory.MISC),
                    FluidIngredient.fromJson(JsonHelper.getObject(json, "result")),
                    RecipeUtils.checkLength(RecipeUtils.union(
                            RecipeUtils.getIngredients(JsonHelper.getArray(json, "ingredients")),
                            Ingredient.fromJson(json.get("receptical"))
                    ))
            );
        }

        @Override
        public FillRecepticalRecipe read(Identifier id, PacketByteBuf buffer) {
            return new FillRecepticalRecipe(id,
                    buffer.readString(),
                    buffer.readEnumConstant(CraftingRecipeCategory.class),
                    new FluidIngredient(buffer),
                    buffer.readCollection(DefaultedList::ofSize, Ingredient::fromPacket)
            );
        }

        @Override
        public void write(PacketByteBuf buffer, FillRecepticalRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            recipe.output.write(buffer);
            buffer.writeCollection(recipe.getIngredients(), (b, c) -> c.write(b));
        }

    }
}

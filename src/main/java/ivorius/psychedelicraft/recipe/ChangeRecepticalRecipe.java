package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.DyeableItem;
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

import ivorius.psychedelicraft.fluid.container.FluidContainer;

/**
 * Created by lukas on 10.11.14.
 * Updated by Sollace on 5 Jan 2023
 *
 * A shapeless recipe that preserves a drink bottle's contents between crafting.
 *
 * Used to change the container a fluid is in without losing any of its contents.
 *
 */
public class ChangeRecepticalRecipe extends ShapelessRecipe {
    private final ItemStack output;

    public ChangeRecepticalRecipe(Identifier id, String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<Ingredient> input) {
        super(id, group, category, output, input);
        this.output = output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.CHANGE_RECEPTICAL;
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        return RecipeUtils.recepticals(inventory).count() == 1 && super.matches(inventory, world);
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registries) {
        return RecipeUtils.recepticals(inventory).findFirst().map(pair -> {
            // copy bottle contents to the new stack
            ItemStack input = pair.getValue().copy();
            ItemStack output = getOutput(registries).copy();
            FluidContainer outputContainer = FluidContainer.of(output, null);
            if (outputContainer != null) {
                output = outputContainer.toMutable(output).fillFrom(pair.getKey().toMutable(input)).asStack();
            }
            if (output.getItem() instanceof DyeableItem outDyable && input.getItem() instanceof DyeableItem inDyable) {
                outDyable.setColor(output, inDyable.getColor(input));
            }
            return output;
        }).orElseGet(getOutput(registries)::copy);
    }

    static class Serializer implements RecipeSerializer<ChangeRecepticalRecipe> {
        @SuppressWarnings("deprecation")
        @Override
        public ChangeRecepticalRecipe read(Identifier id, JsonObject json) {
            return new ChangeRecepticalRecipe(id,
                    JsonHelper.getString(json, "group", ""),
                    CraftingRecipeCategory.CODEC.byId(JsonHelper.getString(json, "category", null), CraftingRecipeCategory.MISC),
                    ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result")),
                    RecipeUtils.checkLength(RecipeUtils.getIngredients(JsonHelper.getArray(json, "ingredients")))
            );
        }

        @Override
        public ChangeRecepticalRecipe read(Identifier id, PacketByteBuf buffer) {
            return new ChangeRecepticalRecipe(id,
                    buffer.readString(),
                    buffer.readEnumConstant(CraftingRecipeCategory.class),
                    buffer.readItemStack(),
                    buffer.readCollection(DefaultedList::ofSize, Ingredient::fromPacket)
            );
        }

        @Override
        public void write(PacketByteBuf buffer, ChangeRecepticalRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            buffer.writeItemStack(recipe.output);
            buffer.writeCollection(recipe.getIngredients(), (b, c) -> c.write(b));
        }
    }
}

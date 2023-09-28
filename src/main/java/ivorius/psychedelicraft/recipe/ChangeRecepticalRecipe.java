package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
class ChangeRecepticalRecipe extends ShapelessRecipe {
    private final ItemStack output;

    public ChangeRecepticalRecipe(String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<Ingredient> input) {
        super(group, category, output, input);
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
            ItemStack output = getResult(registries).copy();
            FluidContainer outputContainer = FluidContainer.of(output, null);
            if (outputContainer != null) {
                output = outputContainer.toMutable(output).fillFrom(pair.getKey().toMutable(input)).asStack();
            }
            if (output.getItem() instanceof DyeableItem outDyable && input.getItem() instanceof DyeableItem inDyable) {
                outDyable.setColor(output, inDyable.getColor(input));
            }
            return output;
        }).orElseGet(getResult(registries)::copy);
    }

    static class Serializer implements RecipeSerializer<ChangeRecepticalRecipe> {
        private static final Codec<ChangeRecepticalRecipe> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(Codecs.createStrictOptionalFieldCodec(Codec.STRING, "group", "").forGetter(ChangeRecepticalRecipe::getGroup),
                        CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(ChangeRecepticalRecipe::getCategory),
                        RecipeCodecs.CRAFTING_RESULT.fieldOf("result").forGetter(recipe -> recipe.output),
                        RecipeUtils.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(ChangeRecepticalRecipe::getIngredients)
                ).apply(instance, ChangeRecepticalRecipe::new)
        );

        @Override
        public Codec<ChangeRecepticalRecipe> codec() {
            return CODEC;
        }

        @Override
        public ChangeRecepticalRecipe read(PacketByteBuf buffer) {
            return new ChangeRecepticalRecipe(
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

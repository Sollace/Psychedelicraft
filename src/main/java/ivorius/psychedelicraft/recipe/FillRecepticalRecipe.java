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
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
    private final Ingredient receptical;
    private final FluidIngredient output;

    public FillRecepticalRecipe(
            String group,
            CraftingRecipeCategory category,
            FluidIngredient output,
            Ingredient receptical,
            DefaultedList<Ingredient> input) {
        super(group, category, ItemStack.EMPTY, RecipeUtils.checkLength(RecipeUtils.union(input, receptical)));
        this.receptical = receptical;
        this.output = output;
    }

    public FluidIngredient getOutputFluid() {
        return output;
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
    public final ItemStack getResult(DynamicRegistryManager registryManager) {
        return output.toVanillaIngredient(receptical).getMatchingStacks()[0];
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

    static class Serializer implements RecipeSerializer<FillRecepticalRecipe> {
        public static final Codec<FillRecepticalRecipe> CODEC = RecordCodecBuilder.<FillRecepticalRecipe>create(instance -> instance
                .group(Codecs.createStrictOptionalFieldCodec(Codec.STRING, "group", "").forGetter(FillRecepticalRecipe::getGroup),
                        CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(FillRecepticalRecipe::getCategory),
                        FluidIngredient.CODEC.fieldOf("result").forGetter(i -> i.output),
                        Ingredient.ALLOW_EMPTY_CODEC.fieldOf("receptical").forGetter(i -> i.receptical),
                        RecipeUtils.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(FillRecepticalRecipe::getIngredients)
                ).apply(instance, FillRecepticalRecipe::new)
        );

        @Override
        public Codec<FillRecepticalRecipe> codec() {
            return CODEC;
        }

        @Override
        public FillRecepticalRecipe read(PacketByteBuf buffer) {
            return new FillRecepticalRecipe(
                    buffer.readString(),
                    buffer.readEnumConstant(CraftingRecipeCategory.class),
                    new FluidIngredient(buffer),
                    Ingredient.fromPacket(buffer),
                    buffer.readCollection(DefaultedList::ofSize, Ingredient::fromPacket)
            );
        }

        @Override
        public void write(PacketByteBuf buffer, FillRecepticalRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            recipe.output.write(buffer);
            recipe.receptical.write(buffer);
            buffer.writeCollection(recipe.getIngredients(), (b, c) -> c.write(b));
        }
    }
}

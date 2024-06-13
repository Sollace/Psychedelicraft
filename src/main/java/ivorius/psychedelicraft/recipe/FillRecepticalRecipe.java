/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
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
    public static final MapCodec<FillRecepticalRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(FillRecepticalRecipe::getGroup),
            CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(FillRecepticalRecipe::getCategory),
            FluidIngredient.CODEC.fieldOf("result").forGetter(FillRecepticalRecipe::getOutputFluid),
            Ingredient.ALLOW_EMPTY_CODEC.fieldOf("receptical").forGetter(i -> i.receptical),
            RecipeUtils.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(i -> i.input)
    ).apply(instance, FillRecepticalRecipe::new));
    public static final PacketCodec<RegistryByteBuf, FillRecepticalRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, FillRecepticalRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, FillRecepticalRecipe::getCategory,
            FluidIngredient.PACKET_CODEC, FillRecepticalRecipe::getOutputFluid,
            Ingredient.PACKET_CODEC, recipe -> recipe.receptical,
            RecipeUtils.INGREDIENTS_PACKET_CODEC, i -> i.input,
            FillRecepticalRecipe::new
    );
    private final Ingredient receptical;
    private final DefaultedList<Ingredient> input;
    private final FluidIngredient output;

    public FillRecepticalRecipe(
            String group,
            CraftingRecipeCategory category,
            FluidIngredient output,
            Ingredient receptical,
            DefaultedList<Ingredient> input) {
        super(group, category, ItemStack.EMPTY, RecipeUtils.checkLength(RecipeUtils.union(input, receptical)));
        this.receptical = receptical;
        this.input = input;
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
    public boolean matches(CraftingRecipeInput inventory, World world) {
        RecipeMatcher recipeMatcher = new RecipeMatcher();
        return RecipeUtils.toRecepticals(inventory.getStacks().stream()).count() == 1
                && inventory.getStacks().stream().filter(stack -> {
                    recipeMatcher.addInput(stack, 1);
                    return true;
                }).count() == getIngredients().size()
                && recipeMatcher.match(this, null);
    }

    @Override
    public final ItemStack getResult(WrapperLookup registryManager) {
        return output.toVanillaIngredient(receptical).getMatchingStacks()[0];
    }

    @Override
    public ItemStack craft(CraftingRecipeInput inventory, WrapperLookup registries) {
        return RecipeUtils.toRecepticals(inventory.getStacks().stream()).findFirst().map(receptical -> {
            ItemStack stack = output.fluid().getDefaultStack(receptical.getKey(), output.level() <= 0
                ? receptical.getKey().getMaxCapacity(receptical.getValue())
                : (output.level() + receptical.getKey().getLevel(receptical.getValue()))
            );
            stack.getOrCreateSubNbt("fluid").copyFrom(output.attributes());
            return stack;
        }).orElse(ItemStack.EMPTY);
    }
}

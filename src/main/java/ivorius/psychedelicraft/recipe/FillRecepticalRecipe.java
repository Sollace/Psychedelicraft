/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;

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
            Ingredient.CODEC.fieldOf("receptical").forGetter(i -> i.receptical),
            Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(i -> i.input)
    ).apply(instance, FillRecepticalRecipe::new));
    public static final PacketCodec<RegistryByteBuf, FillRecepticalRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, FillRecepticalRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, FillRecepticalRecipe::getCategory,
            FluidIngredient.PACKET_CODEC, FillRecepticalRecipe::getOutputFluid,
            Ingredient.PACKET_CODEC, recipe -> recipe.receptical,
            Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()), i -> i.input,
            FillRecepticalRecipe::new
    );
    private final Ingredient receptical;
    private final List<Ingredient> input;
    private final FluidIngredient output;

    public FillRecepticalRecipe(
            String group,
            CraftingRecipeCategory category,
            FluidIngredient output,
            Ingredient receptical,
            List<Ingredient> input) {
        super(group, category, ItemStack.EMPTY, Stream.concat(input.stream(), Stream.of(receptical)).toList());
        this.receptical = receptical;
        this.input = input;
        this.output = output;
    }

    public FluidIngredient getOutputFluid() {
        return output;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public RecipeSerializer getSerializer() {
        return PSRecipes.FILL_RECEPTICAL;
    }

    @Override
    public boolean matches(CraftingRecipeInput inventory, World world) {
        List<ItemStack> recepticals = RecipeUtils.recepticals(inventory.getStacks()
                .stream())
                .filter(receptical -> input.stream().noneMatch(i -> i.test(receptical)))
                .toList();

        return recepticals.size() == 1
                && ItemFluids.of(recepticals.get(0)).isOf(Fluids.WATER)
                && FluidCapacity.getPercentage(recepticals.get(0)) >= 1
                && inventory.getRecipeMatcher().isCraftable(this, null);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput inventory, WrapperLookup registries) {
        return RecipeUtils.recepticals(inventory.getStacks().stream()).findFirst().map(receptical -> {
            return ItemFluids.set(receptical.copy(), output.getAsItemFluid(FluidCapacity.get(receptical)));
        }).orElse(ItemStack.EMPTY);
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;

/**
 * Created by Sollace on 19 Jul 2024
 *
 * Used by the bunsen burner to produce the correct fluid type for ingredients dropped into it
 */
public record ReducingRecipe (
        String group,
        CraftingRecipeCategory category,
        ItemFluids result,
        ItemStack remainder,
        Ingredient ingredient,
        int stewTime) implements Recipe<ReducingRecipe.Input> {
    public static final MapCodec<ReducingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ReducingRecipe::group),
            CraftingRecipeCategory.CODEC.optionalFieldOf("category", CraftingRecipeCategory.MISC).forGetter(ReducingRecipe::category),
            ItemFluids.CODEC.fieldOf("result").forGetter(ReducingRecipe::result),
            ItemStack.VALIDATED_CODEC.optionalFieldOf("remainder", ItemStack.EMPTY).forGetter(ReducingRecipe::remainder),
            Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(ReducingRecipe::ingredient),
            Codec.INT.optionalFieldOf("stew_time", 0).forGetter(ReducingRecipe::stewTime)
    ).apply(instance, ReducingRecipe::new));
    public static final PacketCodec<RegistryByteBuf, ReducingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ReducingRecipe::group,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, ReducingRecipe::category,
            ItemFluids.PACKET_CODEC, ReducingRecipe::result,
            ItemStack.PACKET_CODEC, ReducingRecipe::remainder,
            Ingredient.PACKET_CODEC, ReducingRecipe::ingredient,
            PacketCodecs.INTEGER, ReducingRecipe::stewTime,
            ReducingRecipe::new
    );

    @Override
    public RecipeType<?> getType() {
        return PSRecipes.REACTING_TYPE;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.REDUCING;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public ItemStack createIcon() {
        return PSItems.MASH_TUB.getDefaultStack();
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.copyOf(Ingredient.EMPTY, ingredient);
    }

    @Override
    public boolean matches(Input input, World world) {
        return ingredient.test(input.input());
    }

    @Override
    public ItemStack craft(Input input, WrapperLookup lookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 0;
    }

    @Override
    public ItemStack getResult(WrapperLookup lookup) {
        return ItemStack.EMPTY;
    }

    public record Input(ItemStack input) implements RecipeInput {
        @Override
        public ItemStack getStackInSlot(int slot) {
            return input;
        }

        @Override
        public int getSize() {
            return 1;
        }
    }
}

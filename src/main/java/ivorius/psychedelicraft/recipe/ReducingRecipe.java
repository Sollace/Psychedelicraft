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

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.CodecUtils;
import ivorius.psychedelicraft.util.PacketCodecUtils;

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
        DefaultedList<FluidIngredient> fluids,
        Ingredient ingredient,
        int stewTime) implements Recipe<ReducingRecipe.Input> {
    public static final MapCodec<ReducingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ReducingRecipe::group),
            CraftingRecipeCategory.CODEC.optionalFieldOf("category", CraftingRecipeCategory.MISC).forGetter(ReducingRecipe::category),
            ItemFluids.CODEC.fieldOf("result").forGetter(ReducingRecipe::result),
            ItemStack.VALIDATED_CODEC.optionalFieldOf("remainder", ItemStack.EMPTY).forGetter(ReducingRecipe::remainder),
            CodecUtils.toDefaultedList(FluidIngredient.CODEC, FluidIngredient.EMPTY).fieldOf("fluids").forGetter(ReducingRecipe::fluids),
            Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(ReducingRecipe::ingredient),
            Codec.INT.optionalFieldOf("stew_time", 0).forGetter(ReducingRecipe::stewTime)
    ).apply(instance, ReducingRecipe::new));
    public static final PacketCodec<RegistryByteBuf, ReducingRecipe> PACKET_CODEC = PacketCodecUtils.tuple(
            PacketCodecs.STRING, ReducingRecipe::group,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, ReducingRecipe::category,
            ItemFluids.PACKET_CODEC, ReducingRecipe::result,
            ItemStack.PACKET_CODEC, ReducingRecipe::remainder,
            FluidIngredient.PACKET_CODEC.collect(PacketCodecUtils.toDefaultedList()), ReducingRecipe::fluids,
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
        if (input.input().countMatches(ingredient) == 0) {
            return false;
        }

        List<ItemFluids> inputs = new ArrayList<>(input.fluids());
        return fluids().stream().allMatch(ingredient -> {
            return inputs.stream().filter(fluid -> ingredient.test(fluid)).findFirst().map(match -> {
                inputs.remove(match);
                return true;
            }).isPresent();
        });
    }

    public List<ItemFluids> getRemainingFluids(List<ItemFluids> fluids) {
        if (!fluids().isEmpty() && !fluids.isEmpty()) {
            List<ItemFluids> remainder = new ArrayList<>(fluids);
            List<FluidIngredient> ingredients = new ArrayList<>(fluids());
            for (int i = 0; i < fluids.size(); i++) {
                ItemFluids fluid = fluids.get(i);
                int index = i;
                ingredients.stream().filter(ingredient -> ingredient.test(fluid)).findFirst().ifPresent(ingredient -> {
                    ingredients.remove(ingredient);
                    remainder.set(index, fluid.ofAmount(fluid.amount() - ingredient.level().orElse(fluid.amount())));
                });
            }
            return remainder;
        }
        return fluids;
    }

    @Override
    public ItemStack craft(Input input, WrapperLookup lookup) {
        return remainder;
    }

    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 0;
    }

    @Override
    public ItemStack getResult(WrapperLookup lookup) {
        return remainder;
    }

    public record Input(List<ItemFluids> fluids, ItemMound input) implements RecipeInput {
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSize() {
            return 1;
        }
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.component.ItemFluids;

/**
 * Created by Sollace on 19 Jul 2024
 *
 * Used by the bunsen burner to produce the correct fluid type for ingredients dropped into it
 */
public record ReactingRecipe (
        String reducingGroup,
        CraftingRecipeCategory category,
        Result result,
        Ingredients ingredients,
        int stewTime) implements BunsenBurnerRecipe {
    public static final MapCodec<ReactingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ReactingRecipe::reducingGroup),
            CraftingRecipeCategory.CODEC.optionalFieldOf("category", CraftingRecipeCategory.MISC).forGetter(ReactingRecipe::category),
            Result.CODEC.fieldOf("result").forGetter(ReactingRecipe::result),
            Ingredients.CODEC.fieldOf("ingredients").forGetter(ReactingRecipe::ingredients),
            Codec.INT.optionalFieldOf("stew_time", 0).forGetter(ReactingRecipe::stewTime)
    ).apply(instance, ReactingRecipe::new));
    public static final PacketCodec<RegistryByteBuf, ReactingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ReactingRecipe::reducingGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, ReactingRecipe::category,
            Result.PACKET_CODEC, ReactingRecipe::result,
            Ingredients.PACKET_CODEC, ReactingRecipe::ingredients,
            PacketCodecs.INTEGER, ReactingRecipe::stewTime,
            ReactingRecipe::new
    );

    @Override
    public RecipeSerializer<ReactingRecipe> getSerializer() {
        return PSRecipes.REACTING;
    }

    @Override
    public String getGroup() {
        return reducingGroup;
    }

    @Override
    public boolean isAcceptableIngredient(ItemStack stack) {
        return ingredients.solids().stream().anyMatch(i -> i.test(stack));
    }

    @Override
    public boolean matches(Input input, World world) {
        return ingredients.matchSolids(new ItemMound(input.input())) && ingredients.matchFluids(input.fluids());
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public ItemStack craft(Input input, WrapperLookup lookup) {
        if (ingredients.matchSolids(input.input())) {
            int level = ingredients.consumeMatchingFluids(input.fluids());
            if (!result.fluid().isEmpty()) {
                input.consumer().accept(level == 0 ? result.fluid() : result.fluid().ofAmount(result.fluid().amount() * level));
            }
        }
        return result.byProduct();
    }

    public record Result (
            ItemFluids fluid,
            ItemStack byProduct
    ) {
        public static final MapCodec<Result> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemFluids.CODEC.fieldOf("fluid").forGetter(Result::fluid),
                ItemStack.OPTIONAL_CODEC.optionalFieldOf("by_product", ItemStack.EMPTY).forGetter(Result::byProduct)
        ).apply(instance, Result::new));
        public static final PacketCodec<RegistryByteBuf, Result> PACKET_CODEC = PacketCodec.tuple(
                ItemFluids.PACKET_CODEC, Result::fluid,
                ItemStack.OPTIONAL_PACKET_CODEC, Result::byProduct,
                Result::new
        );
    }

    public record Ingredients(
            /**
             * Required input fluids (optional)
             */
            List<FluidIngredient> fluids,
            /**
             * Required input item (optional)
             */
            List<Ingredient> solids
    ) {
        public static final MapCodec<Ingredients> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FluidIngredient.CODEC.listOf().optionalFieldOf("fluids", List.of()).forGetter(Ingredients::fluids),
                Ingredient.CODEC.listOf().optionalFieldOf("solids", List.of()).forGetter(Ingredients::solids)
        ).apply(instance, Ingredients::new));
        public static final PacketCodec<RegistryByteBuf, Ingredients> PACKET_CODEC = PacketCodec.tuple(
                FluidIngredient.PACKET_CODEC.collect(PacketCodecs.toList()), Ingredients::fluids,
                Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()), Ingredients::solids,
                Ingredients::new
        );

        public boolean matchSolids(ItemMound items) {
            if (solids().isEmpty()) {
                return items.isEmpty();
            }

            return solids().stream().allMatch(solid -> items.removeWhere(solid, 1));
        }

        public boolean matchFluids(FluidMound fluids) {
            return fluids().isEmpty() || fluids().stream().allMatch(ingredient -> fluids.removeMatch(ingredient) > 0);
        }

        public int consumeMatchingFluids(FluidMound fluids) {
            return fluids().isEmpty() ? 0 : Math.max(0, fluids().stream().mapToInt(ingredient -> fluids.removeMatch(ingredient)).min().orElse(0));
        }
    }

}

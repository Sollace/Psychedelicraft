/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import ivorius.psychedelicraft.util.compat.IngredientCompat;
import ivorius.psychedelicraft.util.compat.PacketCodec;
import ivorius.psychedelicraft.util.compat.PacketCodecs;
import ivorius.psychedelicraft.util.CodecUtils;
import ivorius.psychedelicraft.util.PacketCodecUtils;

/**
 * Created by Sollace on 19 Jul 2024
 *
 * Used by the bunsen burner to produce the correct fluid type for ingredients dropped into it
 */
public record ReactingRecipe (
        Identifier id,
        String reducingGroup,
        CraftingRecipeCategory category,
        Result result,
        Ingredients ingredients,
        int stewTime) implements BunsenBurnerRecipe {
    public static final MapCodec<ReactingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(ReactingRecipe::getId),
            Codec.STRING.optionalFieldOf("group", "").forGetter(ReactingRecipe::reducingGroup),
            CraftingRecipeCategory.CODEC.optionalFieldOf("category", CraftingRecipeCategory.MISC).forGetter(ReactingRecipe::category),
            Result.CODEC.fieldOf("result").forGetter(ReactingRecipe::result),
            Ingredients.CODEC.fieldOf("ingredients").forGetter(ReactingRecipe::ingredients),
            Codec.INT.optionalFieldOf("stew_time", 0).forGetter(ReactingRecipe::stewTime)
    ).apply(instance, ReactingRecipe::new));
    public static final PacketCodec<PacketByteBuf, ReactingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.IDENTIFIER, ReactingRecipe::getId,
            PacketCodecs.STRING, ReactingRecipe::reducingGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, ReactingRecipe::category,
            Result.PACKET_CODEC, ReactingRecipe::result,
            Ingredients.PACKET_CODEC, ReactingRecipe::ingredients,
            PacketCodecs.INTEGER, ReactingRecipe::stewTime,
            ReactingRecipe::new
    );

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.REACTING;
    }

    @Override
    public String getGroup() {
        return reducingGroup;
    }

    @Override
    public ItemStack createIcon() {
        return PSItems.BUNSEN_BURNER.getDefaultStack();
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return ingredients.solids();
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
    public ItemStack craft(Input input, DynamicRegistryManager lookup) {
        if (ingredients.matchSolids(input.input())) {
            int level = ingredients.consumeMatchingFluids(input.fluids());
            if (!result.fluid().isEmpty()) {
                input.consumer().accept(level == 0 ? result.fluid() : result.fluid().ofAmount(result.fluid().amount() * level));
                result.impurity.ifPresent(input.consumer()::accept);
            }
        }
        return result.byProduct();
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager lookup) {
        return result.byProduct();
    }

    public record Result (
            ItemFluids fluid,
            ItemStack byProduct,
            Optional<Impurities.Impurity> impurity
    ) {
        public static final MapCodec<Result> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemFluids.CODEC.fieldOf("fluid").forGetter(Result::fluid),
                ItemStack.CODEC.optionalFieldOf("by_product", ItemStack.EMPTY).forGetter(Result::byProduct),
                Impurities.Impurity.CODEC.optionalFieldOf("impurity").forGetter(Result::impurity)
        ).apply(instance, Result::new));
        public static final PacketCodec<PacketByteBuf, Result> PACKET_CODEC = PacketCodec.tuple(
                ItemFluids.PACKET_CODEC, Result::fluid,
                PacketCodecs.ITEM_STACK, Result::byProduct,
                PacketCodecs.optional(PacketCodecUtils.ofEnum(Impurities.Impurity.class)), Result::impurity,
                Result::new
        );
    }

    public record Ingredients(
            /**
             * Required input fluids (optional)
             */
            DefaultedList<FluidIngredient> fluids,
            /**
             * Required input item (optional)
             */
            DefaultedList<Ingredient> solids
    ) {
        public static final MapCodec<Ingredients> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CodecUtils.toDefaultedList(FluidIngredient.CODEC, FluidIngredient.EMPTY).optionalFieldOf("fluids", DefaultedList.of()).forGetter(Ingredients::fluids),
                CodecUtils.toDefaultedList(IngredientCompat.DISALLOW_EMPTY_CODEC, Ingredient.EMPTY).optionalFieldOf("solids", DefaultedList.of()).forGetter(Ingredients::solids)
        ).apply(instance, Ingredients::new));
        public static final PacketCodec<PacketByteBuf, Ingredients> PACKET_CODEC = PacketCodec.tuple(
                FluidIngredient.PACKET_CODEC.collect(PacketCodecUtils.toDefaultedList()), Ingredients::fluids,
                PacketCodecs.INGREDIENT.collect(PacketCodecUtils.toDefaultedList()), Ingredients::solids,
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

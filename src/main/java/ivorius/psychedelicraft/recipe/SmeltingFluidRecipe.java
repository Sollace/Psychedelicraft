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
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import ivorius.psychedelicraft.recipe.ingredient.OptionalFluidIngredient;

/**
 * Created by Sollace on 5 Jan 2023
 *
 * Recipe that alters a container's fluid when cooked in a furnace.
 *
 *  {
 *    "type": "psychedelicraft:smelting_fluid",
 *    "cookingtime": 200,
 *    "experience": 0.2,
 *    "input": {
 *      "fluid": "psychedelicraft:coffee"
 *    },
 *    "result": {
 *      "item": "minecraft:empty", <empty to keep as the same>
 *      "attributes": {
 *        "temperature": {
 *          "type": "add",
 *          "value": 1
 *        }
 *      }
 *    }
 *  }
 *
 */
public class SmeltingFluidRecipe extends SmeltingRecipe {
    public static final MapCodec<SmeltingFluidRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(SmeltingFluidRecipe::getGroup),
            CookingRecipeCategory.CODEC.fieldOf("category").orElse(CookingRecipeCategory.MISC).forGetter(SmeltingFluidRecipe::getCategory),
            OptionalFluidIngredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
            FluidModifyingResult.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.FLOAT.fieldOf("experience").forGetter(SmeltingFluidRecipe::getExperience),
            Codec.INT.optionalFieldOf("cookingTIme", 200).forGetter(SmeltingFluidRecipe::getCookingTime)
        ).apply(instance, SmeltingFluidRecipe::new));
    public static final PacketCodec<RegistryByteBuf, SmeltingFluidRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SmeltingFluidRecipe::getGroup,
            RecipeUtils.COOKING_RECIPE_CATEGORY_PACKET_CODEC, SmeltingFluidRecipe::getCategory,
            OptionalFluidIngredient.PACKET_CODEC, recipe -> recipe.input,
            FluidModifyingResult.PACKET_CODEC, recipe -> recipe.result,
            PacketCodecs.FLOAT, SmeltingFluidRecipe::getExperience,
            PacketCodecs.INTEGER, SmeltingFluidRecipe::getCookingTime,
            SmeltingFluidRecipe::new
    );

    private final OptionalFluidIngredient input;
    private final FluidModifyingResult result;

    public SmeltingFluidRecipe(
            String group, CookingRecipeCategory category,
            OptionalFluidIngredient input,
            FluidModifyingResult result,
            float experience, int cookingTime) {
        super(group, category, input.toVanilla(), result.result(), experience, cookingTime);
        this.input = input;
        this.result = result;
    }

    public FluidIngredient getFluid() {
        return input.fluid().orElseThrow();
    }

    public FluidModifyingResult getResult() {
        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public RecipeSerializer getSerializer() {
        return PSRecipes.SMELTING_RECEPTICAL;
    }

    @Override
    public boolean matches(SingleStackRecipeInput inventory, World world) {
        return input.test(inventory.item());
    }

    @Override
    public ItemStack craft(SingleStackRecipeInput inventory, WrapperLookup registries) {
        return result.applyTo(inventory.item());
    }
}

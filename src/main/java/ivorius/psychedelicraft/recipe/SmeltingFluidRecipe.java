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

import java.lang.ref.WeakReference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.util.PacketCodecUtils;

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
            FluidIngredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.fluid),
            Ingredient.ALLOW_EMPTY_CODEC.optionalFieldOf("item", Ingredient.empty()).forGetter(recipe -> recipe.ingredient),
            FluidModifyingResult.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.FLOAT.fieldOf("experience").forGetter(SmeltingFluidRecipe::getExperience),
            Codec.INT.optionalFieldOf("cookingTIme", 200).forGetter(SmeltingFluidRecipe::getCookingTime)
        ).apply(instance, SmeltingFluidRecipe::new));
    public static final PacketCodec<RegistryByteBuf, SmeltingFluidRecipe> PACKET_CODEC = PacketCodecUtils.tuple(
            PacketCodecs.STRING, SmeltingFluidRecipe::getGroup,
            RecipeUtils.COOKING_RECIPE_CATEGORY_PACKET_CODEC, SmeltingFluidRecipe::getCategory,
            FluidIngredient.PACKET_CODEC, recipe -> recipe.fluid,
            Ingredient.PACKET_CODEC, recipe -> recipe.ingredient,
            FluidModifyingResult.PACKET_CODEC, recipe -> recipe.result,
            PacketCodecs.FLOAT, SmeltingFluidRecipe::getExperience,
            PacketCodecs.INTEGER, SmeltingFluidRecipe::getCookingTime,
            SmeltingFluidRecipe::new
    );

    private final FluidIngredient fluid;
    private final FluidModifyingResult result;

    private WeakReference<SingleStackRecipeInput> lastQueriedInventory = new WeakReference<>(null);

    public SmeltingFluidRecipe(
            String group, CookingRecipeCategory category,
            FluidIngredient fluid, Ingredient inputStack,
            FluidModifyingResult result,
            float experience, int cookingTime) {
        super(group, category, inputStack, result.result(), experience, cookingTime);
        this.fluid = fluid;
        this.result = result;
    }

    public FluidIngredient getFluid() {
        return fluid;
    }

    public FluidModifyingResult getResult() {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.SMELTING_RECEPTICAL;
    }

    @Override
    public boolean matches(SingleStackRecipeInput inventory, World world) {
        lastQueriedInventory = new WeakReference<>(inventory);
        return (ingredient.isEmpty() || ingredient.test(inventory.item())) && fluid.test(inventory.item());
    }

    @Override
    public ItemStack getResult(WrapperLookup registries) {
        SingleStackRecipeInput inventory = lastQueriedInventory.get();
        if (inventory == null) {
            return super.getResult(registries);
        }
        return craft(inventory, registries);
    }

    @Override
    public ItemStack craft(SingleStackRecipeInput inventory, WrapperLookup registries) {
        lastQueriedInventory = new WeakReference<>(inventory);
        return result.applyTo(inventory.item());
    }
}

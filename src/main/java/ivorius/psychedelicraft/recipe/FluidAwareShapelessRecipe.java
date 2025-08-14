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
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.ingredient.OptionalFluidIngredient;
import ivorius.psychedelicraft.util.PacketCodecUtils;

public class FluidAwareShapelessRecipe extends ShapelessRecipe {
    public static final MapCodec<FluidAwareShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(FluidAwareShapelessRecipe::getGroup),
            CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(FluidAwareShapelessRecipe::getCategory),
            ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
            OptionalFluidIngredient.LIST_CODEC.fieldOf("ingredients").forGetter(recipe -> recipe.ingredients),
            Ingredient.ALLOW_EMPTY_CODEC.optionalFieldOf("destroy", Ingredient.empty()).forGetter(recipe -> recipe.destructedIngredient)
    ).apply(instance, FluidAwareShapelessRecipe::new));
    public static final PacketCodec<RegistryByteBuf, FluidAwareShapelessRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, FluidAwareShapelessRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, FluidAwareShapelessRecipe::getCategory,
            ItemStack.OPTIONAL_PACKET_CODEC, recipe -> recipe.output,
            OptionalFluidIngredient.PACKET_CODEC.collect(PacketCodecUtils.toDefaultedList()), recipe -> recipe.ingredients,
            Ingredient.PACKET_CODEC, recipe -> recipe.destructedIngredient,
            FluidAwareShapelessRecipe::new
    );

    private final ItemStack output;
    private final DefaultedList<OptionalFluidIngredient> ingredients;
    private final List<OptionalFluidIngredient> consumedFluids;
    private final Ingredient destructedIngredient;

    public FluidAwareShapelessRecipe(String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<OptionalFluidIngredient> input, Ingredient destructedIngredient) {
        super(group, category, output,
                // parent expects regular ingredients but we don't actually use them
                input.stream()
                .map(OptionalFluidIngredient::toVanilla)
                .collect(Collectors.toCollection(DefaultedList::of))
        );
        this.output = output;
        this.ingredients = input;
        this.consumedFluids = ingredients.stream().filter(i -> i.fluid().isPresent()).toList();
        this.destructedIngredient = destructedIngredient;
    }

    public DefaultedList<OptionalFluidIngredient> getFluidAwareIngredients() {
        return ingredients;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.CRAFTING_SHAPELESS_FLUID;
    }

    @Override
    public boolean matches(CraftingRecipeInput inventory, World world) {
        List<OptionalFluidIngredient> unmatchedInputs = new ArrayList<>(ingredients);
        long matches = inventory.getStacks().stream()
                    .filter(stack -> unmatchedInputs.stream()
                        .filter(ingredient -> ingredient.test(stack))
                        .findFirst()
                        .map(unmatchedInputs::remove)
                        .orElse(false)).count();
        return matches == ingredients.size()
                //&& matches == inventory.getStacks().size()
                && unmatchedInputs.isEmpty();
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput inventory) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(inventory.getSize(), ItemStack.EMPTY);

        boolean destroyed = false;

        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack stack = inventory.getStackInSlot(i);
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack);

            if (!t.fluids().isEmpty() && consumedFluids.stream()
                .filter(ingredient -> ingredient.test(stack))
                .map(OptionalFluidIngredient::fluid)
                .flatMap(Optional::stream)
                .anyMatch(fluid -> {
                    t.withdraw(fluid.level().orElse(t.fluids().amount()));
                    return true;
                })) {
                defaultedList.set(i, t.toItemStack());
            } else {
                ItemStack remainder = stack.getRecipeRemainder();
                if (!remainder.isEmpty()) {
                    if (!destroyed && destructedIngredient.test(remainder)) {
                        destroyed = true;
                    } else {
                        defaultedList.set(i, remainder);
                    }
                }
            }
        }
        return defaultedList;
    }

}

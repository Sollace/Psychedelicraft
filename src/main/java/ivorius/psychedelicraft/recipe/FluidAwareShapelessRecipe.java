/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;

public class FluidAwareShapelessRecipe extends ShapelessRecipe {

    private final ItemStack output;
    private final DefaultedList<OptionalFluidIngredient> ingredients;
    private final List<OptionalFluidIngredient> fluidRestrictions;

    public FluidAwareShapelessRecipe(String group, CraftingRecipeCategory category, ItemStack output,
            DefaultedList<OptionalFluidIngredient> input) {
        super(group, category, output,
                // parent expects regular ingredients but we don't actually use them
                input.stream()
                .map(i -> i.receptical().orElse(Ingredient.EMPTY))
                .collect(Collectors.toCollection(DefaultedList::of))
        );
        this.output = output;
        this.ingredients = input;
        this.fluidRestrictions = ingredients.stream().filter(i -> i.fluid().filter(f -> f.level() > 0).isPresent()).toList();
    }

    public DefaultedList<OptionalFluidIngredient> getFluidAwareIngredients() {
        return ingredients;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.SHAPELESS_FLUID;
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        List<OptionalFluidIngredient> unmatchedInputs = new ArrayList<>(ingredients);
        return RecipeUtils.stacks(inventory)
                    .filter(stack -> unmatchedInputs.stream()
                        .filter(ingredient -> ingredient.test(stack))
                        .findFirst()
                        .map(unmatchedInputs::remove)
                        .orElse(false)).count() == ingredients.size() && unmatchedInputs.isEmpty();
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory inventory) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            Item item = stack.getItem();

            defaultedList.set(i, item.hasRecipeRemainder()
                ? new ItemStack(item.getRecipeRemainder())
                : fluidRestrictions.stream()
                        .filter(t -> t.test(stack))
                        .findFirst()
                        .flatMap(OptionalFluidIngredient::fluid)
                        .map(fluid -> MutableFluidContainer.of(stack).decrement(fluid.level()))
                        .map(MutableFluidContainer::asStack)
                        .orElse(ItemStack.EMPTY)
            );
        }
        return defaultedList;
    }

    static class Serializer implements RecipeSerializer<FluidAwareShapelessRecipe> {
        public static final Codec<FluidAwareShapelessRecipe> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(Codecs.createStrictOptionalFieldCodec(Codec.STRING, "group", "").forGetter(FluidAwareShapelessRecipe::getGroup),
                        CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(FluidAwareShapelessRecipe::getCategory),
                        RecipeCodecs.CRAFTING_RESULT.fieldOf("result").forGetter(recipe -> recipe.getResult(null)),
                        OptionalFluidIngredient.LIST_CODEC.fieldOf("ingredients").forGetter(recipe -> recipe.ingredients)
                ).apply(instance, FluidAwareShapelessRecipe::new)
        );

        @Override
        public Codec<FluidAwareShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public FluidAwareShapelessRecipe read(PacketByteBuf buffer) {
            return new FluidAwareShapelessRecipe(
                    buffer.readString(),
                    buffer.readEnumConstant(CraftingRecipeCategory.class),
                    buffer.readItemStack(),
                    buffer.readCollection(DefaultedList::ofSize, OptionalFluidIngredient::new)
            );
        }

        @Override
        public void write(PacketByteBuf buffer, FluidAwareShapelessRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            buffer.writeItemStack(recipe.output);
            buffer.writeCollection(recipe.ingredients, (b, c) -> c.write(b));
        }

    }
}

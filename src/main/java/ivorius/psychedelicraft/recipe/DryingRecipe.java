package ivorius.psychedelicraft.recipe;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public record DryingRecipe(
        String group,
        CookingRecipeCategory category,
        Ingredient input,
        ItemStack output,
        float experience,
        int cookTime
    ) implements Recipe<DryingRecipe.Input> {
    public static final MapCodec<DryingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(DryingRecipe::group),
            CookingRecipeCategory.CODEC.fieldOf("category").orElse(CookingRecipeCategory.MISC).forGetter(DryingRecipe::category),
            Ingredient.ALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(DryingRecipe::input),
            ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(DryingRecipe::output),
            Codec.FLOAT.optionalFieldOf("experience", 0F).forGetter(DryingRecipe::experience),
            Codec.INT.optionalFieldOf("cookingTime", 200).forGetter(DryingRecipe::cookTime)
        ).apply(instance, DryingRecipe::new));
    public static final PacketCodec<RegistryByteBuf, DryingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, DryingRecipe::group,
            RecipeUtils.COOKING_RECIPE_CATEGORY_PACKET_CODEC, DryingRecipe::category,
            Ingredient.PACKET_CODEC, DryingRecipe::input,
            ItemStack.PACKET_CODEC, DryingRecipe::output,
            PacketCodecs.FLOAT, DryingRecipe::experience,
            PacketCodecs.INTEGER, DryingRecipe::cookTime,
            DryingRecipe::new
    );

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.DRYING;
    }

    @Override
    public RecipeType<?> getType() {
        return PSRecipes.DRYING_TYPE;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.copyOf(Ingredient.EMPTY, input);
    }

    @Override
    public ItemStack createIcon() {
        return PSItems.DRYING_TABLE.getDefaultStack();
    }

    @Override
    public boolean matches(Input input, World world) {
        return (input.result.isEmpty() || ItemStack.areItemsAndComponentsEqual(output, input.result())) && input.ingredients().stream()
                .filter(this.input)
                .mapToInt(ItemStack::getCount)
                .sum() >= 9;
    }

    // Suppress warnings being logged when Minecraft realises it doesn't know what category to put these recipes into
    // The mashing tub doesn't have a recipe book anyway
    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public ItemStack craft(Input input, WrapperLookup lookup) {
        return getResult(lookup);
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResult(WrapperLookup registriesLookup) {
        return output.copy();
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(DryingRecipe.Input input) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(input.getSize(), ItemStack.EMPTY);
        int toConsume = 9;

        for (int i = 0; i < defaultedList.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (toConsume > 0 && input().test(stack)) {
                stack = stack.copy();
                toConsume -= stack.split(toConsume).getCount();
            }

            if (!stack.isEmpty()) {
                defaultedList.set(i, stack);
            }
        }

        return defaultedList;
    }

    public record Input(ItemStack result, List<ItemStack> ingredients) implements RecipeInput {
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ingredients.get(slot);
        }

        @Override
        public int getSize() {
            return ingredients.size();
        }
    }
}

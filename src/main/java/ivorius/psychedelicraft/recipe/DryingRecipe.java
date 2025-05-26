package ivorius.psychedelicraft.recipe;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.util.compat.IngredientCompat;
import ivorius.psychedelicraft.util.compat.PacketCodec;
import ivorius.psychedelicraft.util.compat.PacketCodecs;
import ivorius.psychedelicraft.util.compat.RecipeInput;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public record DryingRecipe(
        Identifier id,
        String dryingGroup,
        Ingredient input,
        ItemStack output,
        float experience,
        float cookTime
    ) implements Recipe<DryingRecipe.Input> {
    public static final MapCodec<DryingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(DryingRecipe::getId),
            Codec.STRING.optionalFieldOf("group", "").forGetter(DryingRecipe::dryingGroup),
            IngredientCompat.ALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(DryingRecipe::input),
            ItemStack.CODEC.fieldOf("result").forGetter(DryingRecipe::output),
            Codec.FLOAT.optionalFieldOf("experience", 0F).forGetter(DryingRecipe::experience),
            Codec.FLOAT.optionalFieldOf("cookingTime", 1F).forGetter(DryingRecipe::cookTime)
        ).apply(instance, DryingRecipe::new));
    public static final PacketCodec<PacketByteBuf, DryingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.IDENTIFIER, DryingRecipe::getId,
            PacketCodecs.STRING, DryingRecipe::dryingGroup,
            PacketCodecs.INGREDIENT, DryingRecipe::input,
            PacketCodecs.ITEM_STACK, DryingRecipe::output,
            PacketCodecs.FLOAT, DryingRecipe::experience,
            PacketCodecs.FLOAT, DryingRecipe::cookTime,
            DryingRecipe::new
    );

    @Override
    public Identifier getId() {
        return id;
    }

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
        return dryingGroup;
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
        return (input.result.isEmpty() || ItemStack.canCombine(output, input.result())) && input.ingredients().stream()
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
    public ItemStack craft(Input input, DynamicRegistryManager lookup) {
        return getOutput(lookup);
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registriesLookup) {
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

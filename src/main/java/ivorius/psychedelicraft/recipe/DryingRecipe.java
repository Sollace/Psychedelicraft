package ivorius.psychedelicraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

public class DryingRecipe extends AbstractCookingRecipe {
    public DryingRecipe(String group, CookingRecipeCategory category, Ingredient input, ItemStack output, float experience, int cookTime) {
        super(PSRecipes.DRYING_TYPE, group, category, input, output, experience, cookTime);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.DRYING;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return RecipeUtils.stacks(inventory).filter(ingredient).count() == inventory.size() - 1;
    }

    protected Ingredient getInput() {
        return ingredient;
    }

    // Suppress warnings being logged when Minecraft realises it doesn't know what category to put these recipes into
    // The mashing tub doesn't have a recipe book anyway
    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<DryingRecipe> {
        private final Codec<DryingRecipe> codec;

        public Serializer(int cookingTime) {
            codec = RecordCodecBuilder.create(instance -> instance.group(
                Codecs.createStrictOptionalFieldCodec(Codec.STRING, "group", "").forGetter(DryingRecipe::getGroup),
                CookingRecipeCategory.CODEC.fieldOf("category").orElse(CookingRecipeCategory.MISC).forGetter(DryingRecipe::getCategory),
                Ingredient.ALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(DryingRecipe::getInput),
                RecipeUtils.ITEM_STACK_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                Codec.FLOAT.optionalFieldOf("experience", 0F).forGetter(DryingRecipe::getExperience),
                Codec.INT.optionalFieldOf("cookingTime", cookingTime).forGetter(DryingRecipe::getCookingTime)
            ).apply(instance, DryingRecipe::new));
        }

        @Override
        public Codec<DryingRecipe> codec() {
            return codec;
        }

        @Override
        public DryingRecipe read(PacketByteBuf buffer) {
            return new DryingRecipe(
                    buffer.readString(),
                    buffer.readEnumConstant(CookingRecipeCategory.class),
                    Ingredient.fromPacket(buffer),
                    buffer.readItemStack(),
                    buffer.readFloat(),
                    buffer.readVarInt()
            );
        }

        @Override
        public void write(PacketByteBuf buffer, DryingRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            recipe.getInput().write(buffer);
            buffer.writeItemStack(recipe.result);
            buffer.writeFloat(recipe.getExperience());
            buffer.writeVarInt(recipe.getCookingTime());
        }
    }
}

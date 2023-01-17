package ivorius.psychedelicraft.recipe;

import com.google.gson.JsonObject;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class DryingRecipe extends AbstractCookingRecipe {
    public DryingRecipe(Identifier id, String group, CookingRecipeCategory category,
            Ingredient input, ItemStack output, float experience, int cookTime) {
        super(PSRecipes.DRYING_TYPE, id, group, category, input, output, experience, cookTime);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.DRYING;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return RecipeUtils.stacks(inventory).filter(input).count() == inventory.size() - 1;
    }

    protected Ingredient getInput() {
        return input;
    }

    // Suppress warnings being logged when Minecraft realises it doesn't know what category to put these recipes into
    // The mashing tub doesn't have a recipe book anyway
    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<DryingRecipe> {
        private final int cookingTime;

        public Serializer(int cookingTime) {
            this.cookingTime = cookingTime;
        }

        @SuppressWarnings("deprecation")
        @Override
        public DryingRecipe read(Identifier id, JsonObject json) {
            return new DryingRecipe(id,
                    JsonHelper.getString(json, "group", ""),
                    CookingRecipeCategory.CODEC.byId(
                            JsonHelper.getString(json, "category", null),
                            CookingRecipeCategory.MISC
                    ),
                    Ingredient.fromJson(JsonHelper.hasArray(json, "ingredient")
                            ? JsonHelper.getArray(json, "ingredient")
                            : JsonHelper.getObject(json, "ingredient")),
                    ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result")),
                    JsonHelper.getFloat(json, "experience", 0),
                    JsonHelper.getInt(json, "cookingtime", cookingTime)
            );
        }

        @Override
        public DryingRecipe read(Identifier id, PacketByteBuf buffer) {
            return new DryingRecipe(id,
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
            buffer.writeItemStack(recipe.getOutput());
            buffer.writeFloat(recipe.getExperience());
            buffer.writeVarInt(recipe.getCookTime());
        }
    }
}

package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.function.Consumer;

import com.google.gson.JsonObject;

import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

public class BottleRecipeJsonBuilder extends ShapedRecipeJsonBuilder {
    public BottleRecipeJsonBuilder(RecipeCategory category, ItemConvertible output, int count) {
        super(category, output, count);
    }

    public static BottleRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output) {
        return create(category, output, 1);
    }

    public static BottleRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, int count) {
        return new BottleRecipeJsonBuilder(category, output, count);
    }

    @Override
    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId) {
        super.offerTo(new Consumer<RecipeJsonProvider>() {
            @Override
            public void accept(RecipeJsonProvider provider) {
                exporter.accept(new RecipeJsonProvider() {
                    @Override
                    public void serialize(JsonObject json) {
                        provider.serialize(json);
                    }

                    @Override
                    public Identifier getRecipeId() {
                        return provider.getRecipeId();
                    }

                    @Override
                    public RecipeSerializer<?> getSerializer() {
                        return PSRecipes.CRAFTING_SHAPED;
                    }

                    @Override
                    public JsonObject toAdvancementJson() {
                        return provider.toAdvancementJson();
                    }

                    @Override
                    public Identifier getAdvancementId() {
                        return provider.getAdvancementId();
                    }
                });
            }
        }, recipeId);
    }
}

package ivorius.psychedelicraft.datagen.providers.recipe;

import com.google.gson.JsonObject;

import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.advancement.Advancement.Builder;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.data.server.recipe.RecipeExporter;
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
    public void offerTo(RecipeExporter exporter, Identifier recipeId) {
        super.offerTo(new RecipeExporter() {
            @Override
            public void accept(RecipeJsonProvider provider) {
                exporter.accept(new RecipeJsonProvider() {
                    @Override
                    public void serialize(JsonObject json) {
                        provider.serialize(json);
                    }

                    @Override
                    public Identifier id() {
                        return provider.id();
                    }

                    @Override
                    public RecipeSerializer<?> serializer() {
                        return PSRecipes.CRAFTING_SHAPED;
                    }

                    @Override
                    public AdvancementEntry advancement() {
                        return provider.advancement();
                    }
                });
            }

            @Override
            public Builder getAdvancementBuilder() {
                return exporter.getAdvancementBuilder();
            }

        }, recipeId);
    }
}

package ivorius.psychedelicraft.datagen.providers.recipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.advancement.Advancement;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

public final class RecipeJsonBuilderCompat {
    static <T extends Recipe<?>> RecipeJsonProvider createProvider(PSRecipes.Serializer<T> serializer, T recipe, Advancement.Builder advancement, Identifier advancementId) {
        return new RecipeJsonProvider() {
            @Override
            public Identifier getRecipeId() {
                return recipe.getId();
            }

            @Override
            public RecipeSerializer<?> getSerializer() {
                return serializer;
            }

            @Override
            public JsonObject toAdvancementJson() {
                return advancement.toJson();
            }

            @Override
            public Identifier getAdvancementId() {
                return advancementId;
            }

            @Override
            public void serialize(JsonObject json) {
                json.asMap().putAll(serializer.codec().encodeStart(JsonOps.INSTANCE, recipe).result().orElseThrow().getAsJsonObject().asMap());
            }
        };
    }
}

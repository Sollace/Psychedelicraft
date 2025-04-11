package ivorius.psychedelicraft.datagen.providers.recipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

public final class RecipeJsonBuilderCompat {
    static <T extends Recipe<?>> RecipeJsonProvider createProvider(Identifier id, RecipeSerializer<T> serializer, T recipe, AdvancementEntry advancement) {
        return new RecipeJsonProvider() {
            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public RecipeSerializer<?> serializer() {
                return serializer;
            }

            @Override
            public AdvancementEntry advancement() {
                return advancement;
            }

            @Override
            public void serialize(JsonObject json) {
                serializer.codec().encode(recipe, JsonOps.INSTANCE, json);
            }
        };
    }
}

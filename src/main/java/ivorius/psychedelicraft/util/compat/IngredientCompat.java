package ivorius.psychedelicraft.util.compat;

import com.mojang.serialization.Codec;

import net.minecraft.recipe.Ingredient;
import net.minecraft.util.dynamic.Codecs;

public interface IngredientCompat {
    Codec<Ingredient> ALLOW_EMPTY_CODEC = Codecs.JSON_ELEMENT.xmap(Ingredient::fromJson, Ingredient::toJson);
    Codec<Ingredient> DISALLOW_EMPTY_CODEC = Codecs.JSON_ELEMENT.xmap(json -> Ingredient.fromJson(json, false), Ingredient::toJson);
}

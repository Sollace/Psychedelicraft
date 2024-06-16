package ivorius.psychedelicraft.compat.tia;

import io.github.mattidragon.tlaapi.api.recipe.TlaRecipe;

interface PSRecipe extends TlaRecipe {
    @Override
    RecipeCategory getCategory();
}

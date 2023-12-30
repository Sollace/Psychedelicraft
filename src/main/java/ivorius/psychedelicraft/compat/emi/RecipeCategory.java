package ivorius.psychedelicraft.compat.emi;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiIngredient;
import ivorius.psychedelicraft.Psychedelicraft;

public record RecipeCategory(EmiRecipeCategory category, EmiIngredient stations) {
    static final Map<RecipeCategory, Consumer<EmiRegistry>> REGISTRY = new HashMap<>();

    static RecipeCategory register(String name, EmiIngredient icon, Consumer<EmiRegistry> recipeConstructor) {
        return register(name, icon, icon, recipeConstructor);
    }

    static RecipeCategory register(String name, EmiRenderable icon, EmiIngredient stations, Consumer<EmiRegistry> recipeConstructor) {
        var id = Psychedelicraft.id(name);
        var category = new RecipeCategory(new EmiRecipeCategory(id, icon, icon), stations);

        REGISTRY.put(category, recipeConstructor);
        return category;
    }

    static void initialize(EmiRegistry registry) {
        REGISTRY.forEach((category, recipeConstructor) -> {
            registry.addCategory(category.category());
            registry.addWorkstation(category.category(), category.stations());
            try {
                recipeConstructor.accept(registry);
            } catch (Throwable t) {
                Psychedelicraft.LOGGER.fatal(t);
            }
        });
    }
}

package ivorius.psychedelicraft.compat.emi;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.recipe.PSRecipes;

@Deprecated
record RecipeCategory(EmiRecipeCategory category, EmiIngredient stations) {
    static final Map<RecipeCategory, Consumer<EmiRegistry>> REGISTRY = new HashMap<>();

    static final RecipeCategory DRYING_TABLE = RecipeCategory.register("drying_table", EmiStack.of(PSItems.DRYING_TABLE), registry -> {
        registry.getRecipeManager().listAllOfType(PSRecipes.DRYING_TYPE).forEach(recipe -> {
            registry.addRecipe(new DryingEmiRecipe(recipe.id(), recipe.value()));
        });
    });
    static final RecipeCategory VAT = RecipeCategory.register("wooden_vat", EmiStack.of(PSItems.MASH_TUB), registry -> {
        registry.getRecipeManager().listAllOfType(PSRecipes.MASHING_TYPE).forEach(recipe -> {
            registry.addRecipe(new MashingEmiRecipe(recipe.id(), recipe.value()));
        });
    });
    static final RecipeCategory BARREL = RecipeCategory.register("barrel", EmiStack.of(PSItems.OAK_BARREL), EmiIngredient.of(PSTags.BARRELS), registry -> {});
    static final RecipeCategory DISTILLERY = RecipeCategory.register("distillery", EmiStack.of(PSItems.DISTILLERY), registry -> {});
    static final RecipeCategory FLASK = RecipeCategory.register("flask", EmiStack.of(PSItems.FLASK), registry -> {});

    static final RecipeCategory FERMENTING = RecipeCategory.register("fermenting", EmiStack.of(PSItems.MASH_TUB), registry -> {});
    static final RecipeCategory MATURING = RecipeCategory.register("maturing", EmiStack.of(PSItems.OAK_BARREL), EmiIngredient.of(PSTags.BARRELS), registry -> {});
    static final RecipeCategory DISTILLING = RecipeCategory.register("distilling", EmiStack.of(PSItems.DISTILLERY), registry -> {});


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

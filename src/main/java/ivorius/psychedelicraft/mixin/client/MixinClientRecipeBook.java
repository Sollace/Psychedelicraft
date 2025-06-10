package ivorius.psychedelicraft.mixin.client;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ivorius.psychedelicraft.recipe.MultiResultRecipe;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.recipe.Recipe;

@Mixin(ClientRecipeBook.class)
abstract class MixinClientRecipeBook {
    @ModifyVariable(method = "toGroupedMap", at = @At("HEAD"), argsOnly = true)
    private static Iterable<Recipe<?>> explodeRecipes(Iterable<Recipe<?>> recipes) {
        return StreamSupport.stream(recipes.spliterator(), false).flatMap(recipe -> {
            if (recipe instanceof MultiResultRecipe<?> multi) {
                return multi.flatten();
            }
            return Stream.of(recipe);
        }).toList();
    }
}

@Mixin(RecipeBookWidget.class)
abstract class MixinRecipeBookWidget {
    @Shadow
    private @Final RecipeBookResults recipesArea;

    @ModifyVariable(method = "showGhostRecipe", at = @At("HEAD"), argsOnly = true)
    private Recipe<?> explodeRecipe(Recipe<?> recipe) {
        var lastClicked = recipesArea.getLastClickedRecipe();
        if (lastClicked != null
                && lastClicked.getId().equals(recipe.getId())
                && recipe instanceof MultiResultRecipe<?>
                && lastClicked instanceof MultiResultRecipe<?>) {
            return lastClicked;
        }
        return recipe;
    }
}
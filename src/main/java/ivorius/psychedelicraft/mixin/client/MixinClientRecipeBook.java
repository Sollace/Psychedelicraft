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
import net.minecraft.recipe.RecipeEntry;

@Mixin(ClientRecipeBook.class)
abstract class MixinClientRecipeBook {
    @ModifyVariable(method = "toGroupedMap", at = @At("HEAD"), argsOnly = true)
    private static Iterable<RecipeEntry<?>> explodeRecipes(Iterable<RecipeEntry<?>> recipes) {
        return StreamSupport.stream(recipes.spliterator(), false).flatMap(entry -> {
            if (entry.value() instanceof MultiResultRecipe<?> multi) {
                return multi.flatten().map(i -> new RecipeEntry<>(entry.id(), i));
            }
            return Stream.of(entry);
        }).toList();
    }
}

@Mixin(RecipeBookWidget.class)
abstract class MixinRecipeBookWidget {
    @Shadow
    private @Final RecipeBookResults recipesArea;

    @ModifyVariable(method = "showGhostRecipe", at = @At("HEAD"), argsOnly = true)
    private RecipeEntry<?> explodeRecipe(RecipeEntry<?> recipe) {
        var lastClicked = recipesArea.getLastClickedRecipe();
        if (lastClicked != null
                && lastClicked.id().equals(recipe.id())
                && recipe.value() instanceof MultiResultRecipe<?>
                && lastClicked.value() instanceof MultiResultRecipe<?>) {
            return lastClicked;
        }
        return recipe;
    }
}
package ivorius.psychedelicraft.recipe;

import java.util.stream.Stream;

import net.minecraft.recipe.Recipe;

public interface MultiResultRecipe<T extends Recipe<?>> {
    Stream<T> flatten();
}

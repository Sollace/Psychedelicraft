package ivorius.psychedelicraft.crafting;

import net.minecraft.recipe.RecipeSerializer;

/**
 * @author Sollace
 * @since 5 Jan 2023
 */
public interface PSRecipes {
    RecipeSerializer<FillDrinkContainerRecipe> FILL_DRINK_CONTAINER = RecipeSerializer.register("fill_drink_container", new FillDrinkContainerRecipe.Serializer());
    RecipeSerializer<ConvertDrinkContainerRecipe> CONVERT_DRINK_CONTAINER = RecipeSerializer.register("convert_drink_container", new ConvertDrinkContainerRecipe.Serializer());
    RecipeSerializer<SmeltingFluidRecipe> SMELTING_FLUID = RecipeSerializer.register("smelting_fluid", new SmeltingFluidRecipe.Serializer());

    static void bootstrap() { }
}

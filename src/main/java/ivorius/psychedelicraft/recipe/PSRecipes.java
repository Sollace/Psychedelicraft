package ivorius.psychedelicraft.recipe;

import net.minecraft.recipe.*;

/**
 * @author Sollace
 * @since 5 Jan 2023
 */
public interface PSRecipes {
    RecipeSerializer<FillDrinkContainerRecipe> FILL_DRINK_CONTAINER = RecipeSerializer.register("psychedelicraft:fill_drink_container", new FillDrinkContainerRecipe.Serializer());
    RecipeSerializer<ConvertDrinkContainerRecipe> CONVERT_DRINK_CONTAINER = RecipeSerializer.register("psychedelicraft:convert_drink_container", new ConvertDrinkContainerRecipe.Serializer());
    RecipeSerializer<SmeltingFluidRecipe> SMELTING_FLUID = RecipeSerializer.register("psychedelicraft:smelting_fluid", new SmeltingFluidRecipe.Serializer());

    RecipeType<DryingRecipe> DRYING_TYPE = RecipeType.register("psychedelicraft:drying");
    RecipeSerializer<DryingRecipe> DRYING = RecipeSerializer.register("psychedelicraft:drying", new DryingRecipe.Serializer(200));

    static void bootstrap() { }
}

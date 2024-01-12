package ivorius.psychedelicraft.recipe;

import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootTable;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;

/**
 * @author Sollace
 * @since 5 Jan 2023
 */
public interface PSRecipes {
    RecipeSerializer<FillRecepticalRecipe> FILL_RECEPTICAL = RecipeSerializer.register("psychedelicraft:fill_receptical", new FillRecepticalRecipe.Serializer());
    RecipeSerializer<ChangeRecepticalRecipe> CHANGE_RECEPTICAL = RecipeSerializer.register("psychedelicraft:change_receptical", new ChangeRecepticalRecipe.Serializer());
    RecipeSerializer<PouringRecipe> POUR_DRINK = RecipeSerializer.register("psychedelicraft:pour_drink", new SpecialRecipeSerializer<>(PouringRecipe::new));
    RecipeSerializer<SmeltingFluidRecipe> SMELTING_RECEPTICAL = RecipeSerializer.register("psychedelicraft:smelting_receptical", new SmeltingFluidRecipe.Serializer());
    RecipeSerializer<BottleRecipe> CRAFTING_SHAPED = RecipeSerializer.register("psychedelicraft:crafting_shaped", new BottleRecipe.Serializer());
    RecipeSerializer<FluidAwareShapelessRecipe> SHAPELESS_FLUID = RecipeSerializer.register("psychedelicraft:shapeless_fluid", new FluidAwareShapelessRecipe.Serializer());

    RecipeType<MashingRecipe> MASHING_TYPE = RecipeType.register("psychedelicraft:mashing");
    RecipeSerializer<MashingRecipe> MASHING = RecipeSerializer.register("psychedelicraft:mashing", new MashingRecipe.Serializer());

    RecipeType<DryingRecipe> DRYING_TYPE = RecipeType.register("psychedelicraft:drying");
    RecipeSerializer<DryingRecipe> DRYING = RecipeSerializer.register("psychedelicraft:drying", new DryingRecipe.Serializer(200));

    static void bootstrap() {
        LootTableEvents.MODIFY.register((res, manager, id, supplier, setter) -> {
            if (!"minecraft".contentEquals(id.getNamespace())) {
                return;
            }
            LootTable table = manager.getLootTable(new Identifier("psychedelicraftmc", id.getPath()));
            if (table != LootTable.EMPTY) {
                final boolean isVillagerChest = id.getPath().contains("village");
                if ((isVillagerChest || Psychedelicraft.getConfig().balancing.worldGeneration.villageChests)
                || (!isVillagerChest || Psychedelicraft.getConfig().balancing.worldGeneration.dungeonChests)) {
                    supplier.pools(table.pools);
                }
            }
        });
    }
}

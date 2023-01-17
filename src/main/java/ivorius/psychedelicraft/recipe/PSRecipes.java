package ivorius.psychedelicraft.recipe;

import java.util.List;

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
    RecipeSerializer<FillDrinkContainerRecipe> FILL_DRINK_CONTAINER = RecipeSerializer.register("psychedelicraft:fill_drink_container", new FillDrinkContainerRecipe.Serializer());
    RecipeSerializer<ConvertDrinkContainerRecipe> CONVERT_DRINK_CONTAINER = RecipeSerializer.register("psychedelicraft:convert_drink_container", new ConvertDrinkContainerRecipe.Serializer());
    RecipeSerializer<SmeltingFluidRecipe> SMELTING_FLUID = RecipeSerializer.register("psychedelicraft:smelting_fluid", new SmeltingFluidRecipe.Serializer());

    RecipeType<DryingRecipe> DRYING_TYPE = RecipeType.register("psychedelicraft:drying");
    RecipeSerializer<DryingRecipe> DRYING = RecipeSerializer.register("psychedelicraft:drying", new DryingRecipe.Serializer(200));

    static void bootstrap() {
        LootTableEvents.MODIFY.register((res, manager, id, supplier, setter) -> {
            if (!"minecraft".contentEquals(id.getNamespace())) {
                return;
            }
            LootTable table = manager.getTable(new Identifier("psychedelicraftmc", id.getPath()));
            if (table != LootTable.EMPTY) {
                final boolean isVillagerChest = id.getPath().contains("village");
                if ((isVillagerChest || Psychedelicraft.getConfig().balancing.worldGeneration.villageChests)
                || (!isVillagerChest || Psychedelicraft.getConfig().balancing.worldGeneration.dungeonChests)) {
                    supplier.pools(List.of(table.pools));
                }
            }
        });
    }
}

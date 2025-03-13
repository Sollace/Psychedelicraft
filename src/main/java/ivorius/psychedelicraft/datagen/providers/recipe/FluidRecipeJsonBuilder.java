package ivorius.psychedelicraft.datagen.providers.recipe;

import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public interface FluidRecipeJsonBuilder extends CraftingRecipeJsonBuilder {
    @Deprecated
    @Override
    default Item getOutputItem() {
        return Items.AIR;
    }

    ItemFluids getOutputFluids();

    @Override
    default void offerTo(RecipeExporter exporter) {
        offerTo(exporter, getOutputFluids().fluid().getId());
    }

    @Override
    default void offerTo(RecipeExporter exporter, String recipePath) {
        Identifier defaultId = getOutputFluids().fluid().getId();
        Identifier id = Identifier.of(recipePath);
        if (id.equals(defaultId)) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }
        offerTo(exporter, id);
    }

}

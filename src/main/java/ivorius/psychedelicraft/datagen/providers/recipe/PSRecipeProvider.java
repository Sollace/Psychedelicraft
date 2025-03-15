package ivorius.psychedelicraft.datagen.providers.recipe;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class PSRecipeProvider extends FabricRecipeProvider {

    public PSRecipeProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registries) {
        super(output, registries);
    }

    @Override
    public String getName() {
        return "Psychedelicraft Recipes";
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(WrapperLookup lookup, RecipeExporter exporter) {
        return new PSRecipeGenerator(lookup, exporter);
    }
}

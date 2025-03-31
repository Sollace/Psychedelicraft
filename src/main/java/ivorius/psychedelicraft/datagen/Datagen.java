package ivorius.psychedelicraft.datagen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.datagen.providers.PSAdvancementsProvider;
import ivorius.psychedelicraft.datagen.providers.PSDynamicRegistriesProvider;
import ivorius.psychedelicraft.datagen.providers.PSModelProvider;
import ivorius.psychedelicraft.datagen.providers.PlacedDrinksProvider;
import ivorius.psychedelicraft.datagen.providers.loot.PSBlockLootTableProvider;
import ivorius.psychedelicraft.datagen.providers.loot.PSChestAdditionsLootTableProvider;
import ivorius.psychedelicraft.datagen.providers.recipe.PSRecipeProvider;
import ivorius.psychedelicraft.datagen.providers.sound.PSSoundsProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSBiomeTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSBlockTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSDamageTypeTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSEntityTypeTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSFluidTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSItemTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSPointOfInterestTypeTagProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class Datagen implements DataGeneratorEntrypoint {
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        final var pack = fabricDataGenerator.createPack();
        final var blockTagProvider = pack.addProvider(PSBlockTagProvider::new);
        pack.addProvider((output, registries) -> new PSItemTagProvider(output, registries, blockTagProvider));
        pack.addProvider(PSFluidTagProvider::new);
        pack.addProvider(PSPointOfInterestTypeTagProvider::new);
        pack.addProvider(PSEntityTypeTagProvider::new);
        pack.addProvider(PSDamageTypeTagProvider::new);
        pack.addProvider(PSBiomeTagProvider::new);
        pack.addProvider(PSRecipeProvider::new);

        pack.addProvider(PSModelProvider::new);
        pack.addProvider(PSDynamicRegistriesProvider::new);

        pack.addProvider(PSAdvancementsProvider::new);
        pack.addProvider(PSBlockLootTableProvider::new);
        pack.addProvider(PSChestAdditionsLootTableProvider::new);
        pack.addProvider(PSSoundsProvider::new);
        pack.addProvider(PlacedDrinksProvider::new);
    }

    @Override
    public void buildRegistry(RegistryBuilder builder) {
        builder.addRegistry(RegistryKeys.DAMAGE_TYPE, registerable -> {
            PSDamageTypes.REGISTRY.forEach(key -> registerable.register(key, new DamageType(key.getValue().getNamespace() + "." + key.getValue().getPath(), 0)));
        });
        builder.addRegistry(RegistryKeys.TEMPLATE_POOL, CustomStructurePools::bootstrap);
        builder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, registerable -> PSWorldGenFeatures.bootstrapConfiguredFeatures(registerable));
        builder.addRegistry(RegistryKeys.PLACED_FEATURE, PSWorldGenFeatures::bootstrapPlacedFeatures);
    }
}

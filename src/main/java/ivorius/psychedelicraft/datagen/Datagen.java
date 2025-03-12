package ivorius.psychedelicraft.datagen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.datagen.providers.PSModelProvider;
import ivorius.psychedelicraft.datagen.providers.loot.PSBlockLootTableProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSBiomeTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSBlockTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSDamageTypeTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSEntityTypeTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSFluidTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSItemTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSPointOfInterestTypeTagProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
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

        pack.addProvider(PSModelProvider::new);

        pack.addProvider(PSBlockLootTableProvider::new);
    }

    @Override
    public void buildRegistry(RegistryBuilder builder) {
        builder.addRegistry(RegistryKeys.DAMAGE_TYPE, PSDamageTypes::bootstrap);
    }
}

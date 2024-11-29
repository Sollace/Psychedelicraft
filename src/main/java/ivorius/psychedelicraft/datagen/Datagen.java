package ivorius.psychedelicraft.datagen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ivorius.psychedelicraft.datagen.providers.tag.PSBlockTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSFluidTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSItemTagProvider;
import ivorius.psychedelicraft.datagen.providers.tag.PSPointOfInterestTypeTagProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;

public class Datagen implements DataGeneratorEntrypoint {
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        final var pack = fabricDataGenerator.createPack();

        var blockTagProvider = pack.addProvider(PSBlockTagProvider::new);
        pack.addProvider((output, registries) -> new PSItemTagProvider(output, registries, blockTagProvider));
        pack.addProvider(PSFluidTagProvider::new);
        pack.addProvider(PSPointOfInterestTypeTagProvider::new);
    }

    @Override
    public void buildRegistry(RegistryBuilder builder) {

    }
}

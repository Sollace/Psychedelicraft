package ivorius.psychedelicraft.datagen.providers;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.mojang.serialization.Codec;

import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.util.compat.DataProviderCompat;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class PlacedDrinksProvider implements DataProvider {
    private static final Codec<Map<String, Map<Identifier, PlacedDrinksModelProvider.Entry>>> CODEC = Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Identifier.CODEC, PlacedDrinksModelProvider.Entry.CODEC));
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture;
    private final DataOutput output;

    public PlacedDrinksProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        this.registryLookupFuture = registryLookupFuture;
        this.output = output;
    }

    @Override
    public String getName() {
        return "Psychedelicraft Placed Drinks";
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return registryLookupFuture.thenCompose(lookup -> {
            final Map<String, Map<Identifier, PlacedDrinksModelProvider.Entry>> data = new LinkedHashMap<>();
            generate((context, item, entry) -> {
                @SuppressWarnings("deprecation")
                Identifier id = item.asItem().getRegistryEntry().getKey().orElseThrow().getValue();
                if (data.computeIfAbsent(context, n -> new LinkedHashMap<>()).put(id, entry) != null) {
                    throw new IllegalStateException("Duplicate entry " + context + "/" + id);
                }
            });

            Path outputPath = output.resolvePath(DataOutput.OutputType.RESOURCE_PACK).resolve("psychedelicraft/placeable_drinks.json");
            return DataProviderCompat.writeCodecToPath(writer, CODEC, data, outputPath);
        });
    }

    protected void generate(Exporter exporter) {
        exporter.accept("ground", PSItems.WOODEN_MUG, new PlacedDrinksModelProvider.Entry(0.25F, 0.5F, true));
        exporter.accept("ground", PSItems.STONE_CUP, new PlacedDrinksModelProvider.Entry(0.25F, 0.5F, true));
        exporter.accept("ground", PSItems.SHOT_GLASS, new PlacedDrinksModelProvider.Entry(0.2F, 0.5F, true));
        exporter.accept("ground", PSItems.GLASS_CHALICE, new PlacedDrinksModelProvider.Entry(0.4F, 3.5F, true));
        exporter.accept("ground", PSItems.BOTTLE, new PlacedDrinksModelProvider.Entry(0.5F, 0.5F, true));
        exporter.accept("ground", PSItems.MOLOTOV_COCKTAIL, new PlacedDrinksModelProvider.Entry(0.5F, 0.5F, true));

        exporter.accept("burner", PSItems.BOTTLE, new PlacedDrinksModelProvider.Entry(0.5F, 0.5F, false));
        exporter.accept("burner", Items.GLASS_BOTTLE, new PlacedDrinksModelProvider.Entry(0.5F, 0.5F, false));
    }

    interface Exporter {
        void accept(String context, ItemConvertible item, PlacedDrinksModelProvider.Entry entry);
    }
}

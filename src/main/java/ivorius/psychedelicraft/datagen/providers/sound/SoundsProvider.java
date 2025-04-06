package ivorius.psychedelicraft.datagen.providers.sound;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;

import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundEvent;

public abstract class SoundsProvider implements DataProvider {
    private static final Codec<Map<String, SoundTypeBuilder.SoundType>> CODEC = Codec.unboundedMap(Codec.STRING, SoundTypeBuilder.SoundType.CODEC);
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture;
    private final DataOutput output;

    public SoundsProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        this.registryLookupFuture = registryLookupFuture;
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return registryLookupFuture.thenCompose(lookup -> {
            final Map<String, Map<String, SoundTypeBuilder.SoundType>> data = new LinkedHashMap<>();
            generate((event, builder) -> {
                if (data.computeIfAbsent(event.getId().getNamespace(), n -> new LinkedHashMap<>()).put(event.getId().getPath(), builder.build()) != null) {
                    throw new IllegalStateException("Duplicate sound for event " + event.getId());
                }
            });

            return CompletableFuture.allOf(data.entrySet().stream().map(file -> {
                Path outputPath = output.resolvePath(DataOutput.OutputType.RESOURCE_PACK).resolve(file.getKey() + "/sounds.json");
                return DataProvider.writeCodecToPath(writer, CODEC, file.getValue(), outputPath);
            }).toArray(CompletableFuture[]::new));
        });
    }

    protected abstract void generate(BiConsumer<SoundEvent, SoundTypeBuilder> exporter);
}

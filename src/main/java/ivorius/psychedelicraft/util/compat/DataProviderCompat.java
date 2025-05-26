package ivorius.psychedelicraft.util.compat;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;

public interface DataProviderCompat extends DataProvider {
    static <T> CompletableFuture<?> writeCodecToPath(DataWriter writer, Codec<T> codec, T value, Path path) {
        return DataProvider.writeToPath(writer, codec.encodeStart(JsonOps.INSTANCE, value).result().orElseThrow(), path);
    }
}

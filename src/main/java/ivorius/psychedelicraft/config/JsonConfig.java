package ivorius.psychedelicraft.config;

import java.io.IOException;
import java.nio.file.*;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.google.gson.*;

import net.fabricmc.loader.api.FabricLoader;

public interface JsonConfig {

    static <T> Supplier<JsonConfig.Loader<T>> create(String filename, Supplier<T> initializer) {
        return Suppliers.memoize(() -> new Loader<>(FabricLoader.getInstance().getConfigDir().resolve(filename), initializer));
    }

    static final class Loader<T> {
        private static final Gson GSON = new GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .create();

        private final Path path;
        private final Supplier<T> initializer;
        @Nullable
        private T data;

        public Loader(Path path, Supplier<T> initializer) {
            this.path = path;
            this.initializer = initializer;
        }

        public T getData() {
            if (data == null) {
                data = initializer.get();
            }
            return data;
        }

        @SuppressWarnings("unchecked")
        public void load() {
            if (Files.exists(path)) {
                try (var reader = Files.newBufferedReader(path)) {
                    data = (T) GSON.fromJson(reader, getData().getClass());
                } catch (IOException e) {
                    data = initializer.get();
                }
            }
            save();
        }

        public void save() {
            try (var writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
                GSON.toJson(getData(), writer);
            } catch (IOException e) {}
        }
    }
}

package ivorius.psychedelicraft.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.*;

import net.minecraft.util.JsonHelper;

// TODO: (Sollace) Only here for parity with forge's janked config design.
// TODO: (Sollace) Replace this when more of the mod is fixed
public class Configuration {
    public static final String CATEGORY_GENERAL = "general";

    private static final Gson GSON = new GsonBuilder().setLenient().create();

    private JsonObject data;

    public Configuration(Path configPath) {

        if (Files.exists(configPath)) {
            try (var reader = Files.newBufferedReader(configPath)) {
                data = GSON.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                data = new JsonObject();
            }
        }
    }

    public String get(String category, String key, String fallback, String ignore) {
        return JsonHelper.getString(JsonHelper.getObject(data, category, data), key, fallback);
    }

    public float get(String category, String key, float fallback, String ignore) {
        return JsonHelper.getFloat(JsonHelper.getObject(data, category, data), key, fallback);
    }

    public float get(String category, String key, float fallback) {
        return JsonHelper.getFloat(JsonHelper.getObject(data, category, data), key, fallback);
    }

    public int get(String category, String key, int fallback, String ignore) {
        return JsonHelper.getInt(JsonHelper.getObject(data, category, data), key, fallback);
    }

    public boolean get(String category, String key, boolean fallback) {
        return JsonHelper.getBoolean(JsonHelper.getObject(data, category, data), key, fallback);
    }

    public boolean get(String category, String key, boolean fallback, String ignore) {
        return JsonHelper.getBoolean(JsonHelper.getObject(data, category, data), key, fallback);
    }
}

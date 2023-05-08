package ivorius.psychedelicraft.client.render;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ivorius.psychedelicraft.Psychedelicraft;
import net.fabricmc.fabric.api.client.model.ExtraModelProvider;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class PlacedDrinksModelProvider implements ExtraModelProvider {
    private static final Identifier CONFIG_LOCATION = Psychedelicraft.id("placeable_drinks.json");
    private static final Gson GSON = new Gson();

    public static final PlacedDrinksModelProvider INSTANCE = new PlacedDrinksModelProvider();

    private final Map<Identifier, Entry> entries = new HashMap<>();

    @Override
    public void provideExtraModels(ResourceManager manager, Consumer<Identifier> out) {
        if (this != INSTANCE) {
            INSTANCE.provideExtraModels(manager, out);
            return;
        }
        entries.clear();
        manager.getResource(CONFIG_LOCATION).ifPresent(resource -> {
            try (BufferedReader reader = resource.getReader()) {
                JsonHelper.getArray(JsonHelper.asObject(JsonHelper.deserialize(GSON, reader, JsonElement.class), "root"), "values")
                .asList()
                .stream()
                .map(element -> {
                    try {
                        return JsonHelper.asObject(element, "root.values[i]");
                    } catch (Exception e) {
                        Psychedelicraft.LOGGER.error(e);
                    }
                    return new JsonObject();
                })
                .forEach(element -> {
                    if (!element.has("id")) {
                        return;
                    }
                    Identifier id = Identifier.tryParse(JsonHelper.getString(element, "id"));
                    if (id == null) {
                        Psychedelicraft.LOGGER.warn("Invalid identifier: " + element);
                    }
                    out.accept(getGroundModelId(id));
                    out.accept(getGroundModelFluidId(id));
                    entries.put(id, new Entry(element));
                });
            } catch (IOException e) {
                Psychedelicraft.LOGGER.error("Could not load client drinks file", e);
            }
        });
    }

    public Optional<Entry> get(Item item) {
        return Optional.ofNullable(entries.get(Registries.ITEM.getId(item)));
    }

    public static ModelIdentifier getGroundModelId(Item item) {
        return getGroundModelId(Registries.ITEM.getId(item));
    }

    public static ModelIdentifier getGroundModelFluidId(Item item) {
        return getGroundModelFluidId(Registries.ITEM.getId(item));
    }

    public static ModelIdentifier getGroundModelId(Identifier item) {
        return new ModelIdentifier(item.withPath(p -> p + "_on_ground"), "inventory");
    }

    public static ModelIdentifier getGroundModelFluidId(Identifier item) {
        return new ModelIdentifier(item.withPath(p -> p + "_on_ground_fluid"), "inventory");
    }

    public record Entry(float height, float fluidOrigin) {
        public static final Entry DEFAULT = new Entry(0.5F, 0F);
        Entry(JsonObject json) {
            this(JsonHelper.getFloat(json, "height"), JsonHelper.getFloat(json, "fluid_origin"));
        }

        public Entry(NbtCompound compound) {
            this(compound.getFloat("height"), compound.getFloat("fluidOrigin"));
        }


        public NbtCompound toNbt(NbtCompound compound) {
            compound.putFloat("height", height);
            compound.putFloat("fluidOrigin", fluidOrigin);
            return compound;
        }
    }
}

package ivorius.psychedelicraft.client.render;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.item.PlacementProperty;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.Context;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class PlacedDrinksModelProvider
        implements PreparableModelLoadingPlugin<Map<String, Map<Identifier, PlacedDrinksModelProvider.Entry>>>,
        PreparableModelLoadingPlugin.DataLoader<Map<String, Map<Identifier, PlacedDrinksModelProvider.Entry>>> {
    private static final Identifier CONFIG_LOCATION = Psychedelicraft.id("placeable_drinks.json");
    private static final Gson GSON = new Gson();

    public static final PlacedDrinksModelProvider INSTANCE = new PlacedDrinksModelProvider();

    private static final Codec<Map<String, Map<Identifier, Entry>>> CODEC = Codec.unboundedMap(Codec.STRING, Entry.MAP_CODEC);

    private Map<String, Map<Identifier, Entry>> entries = Map.of();

    @Override
    public CompletableFuture<Map<String, Map<Identifier, Entry>>> load(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            return resourceManager.getResource(CONFIG_LOCATION).map(resource -> {
                try (BufferedReader reader = resource.getReader()) {
                    return CODEC.decode(JsonOps.INSTANCE, JsonHelper.deserialize(GSON, reader, JsonElement.class)).getOrThrow().getFirst();
                } catch (IOException e) {
                    Psychedelicraft.LOGGER.error("Could not load client drinks file", e);
                }
                return null;
            }).orElseGet(Map::of);
        }, executor);
    }

    @Override
    public void initialize(Map<String, Map<Identifier, Entry>> data, Context context) {
        entries = data;
    }

    public Optional<Entry> get(String type, Item item) {
        return Optional.ofNullable(entries.get(type).get(Registries.ITEM.getId(item)));
    }

    public void renderDrink(String type, ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        renderDrinkModel(type, stack, matrices, vertices, light, overlay);

        float fillPercentage = FluidCapacity.getPercentage(stack);
        Entry entry = get(type, stack.getItem()).orElse(Entry.DEFAULT);
        if (fillPercentage > 0.01 && entry.showFluid()) {
            float origin = entry.fluidOrigin() / 16F;
            matrices.translate(0, origin, 0);
            matrices.scale(1, fillPercentage, 1);
            matrices.translate(0, -origin, 0);
            renderDrinkModel(type + "_fluid", stack, matrices, vertices, light, overlay);
        }
    }

    public void renderDrinkModel(String type, ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        try {
            if (stack.isOf(Items.GLASS_BOTTLE)) {
                stack = stack.withItem(PSItems.FILLED_GLASS_BOTTLE);
            }
            PlacementProperty.setCurrent(type);
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ItemDisplayContext.FIXED, light, overlay, matrices, vertices, null, 0);
        } finally {
            PlacementProperty.setCurrent(null);
        }
    }

    public record Entry(float height, float fluidOrigin, boolean showFluid) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("height").forGetter(Entry::height),
                Codec.FLOAT.fieldOf("fluid_origin").forGetter(Entry::fluidOrigin),
                Codec.BOOL.optionalFieldOf("show_fluid", false).forGetter(Entry::showFluid)
        ).apply(instance, Entry::new));
        public static final Codec<Map<Identifier, Entry>> MAP_CODEC = Codec.unboundedMap(Identifier.CODEC, CODEC);
        public static final Entry DEFAULT = new Entry(0.5F, 0F, false);
    }
}

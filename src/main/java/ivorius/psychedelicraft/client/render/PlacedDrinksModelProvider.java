package ivorius.psychedelicraft.client.render;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer.FluidAppearance;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.MathUtils;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.Context;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class PlacedDrinksModelProvider
        implements PreparableModelLoadingPlugin<Map<Identifier, PlacedDrinksModelProvider.Entry>>,
        PreparableModelLoadingPlugin.DataLoader<Map<Identifier, PlacedDrinksModelProvider.Entry>> {
    private static final Identifier CONFIG_LOCATION = Psychedelicraft.id("placeable_drinks.json");
    private static final Gson GSON = new Gson();
    private static final Random RNG = Random.create();
    private static final long SEED = 42L;

    public static final PlacedDrinksModelProvider INSTANCE = new PlacedDrinksModelProvider();

    private Map<Identifier, Entry> entries = Map.of();

    @Override
    public CompletableFuture<Map<Identifier, PlacedDrinksModelProvider.Entry>> load(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            return resourceManager.getResource(CONFIG_LOCATION).map(resource -> {
                try (BufferedReader reader = resource.getReader()) {
                    return JsonHelper.getArray(JsonHelper.asObject(JsonHelper.deserialize(GSON, reader, JsonElement.class), "root"), "values")
                        .asList()
                        .stream()
                        .map(element -> {
                            try {
                                return JsonHelper.asObject(element, "root.values[i]");
                            } catch (Exception e) {
                                Psychedelicraft.LOGGER.error(e);
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .map(element -> {
                            if (!element.has("id") || !element.get("id").isJsonPrimitive()) {
                                return null;
                            }
                            Identifier id = Identifier.tryParse(JsonHelper.getString(element, "id"));
                            if (id == null) {
                                Psychedelicraft.LOGGER.warn("Invalid identifier: " + element);
                                return null;
                            }
                            return Map.entry(id, new Entry(element));
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                } catch (IOException e) {
                    Psychedelicraft.LOGGER.error("Could not load client drinks file", e);
                }
                return null;
            }).orElseGet(Map::of);
        }, executor);
    }

    @Override
    public void onInitializeModelLoader(Map<Identifier, PlacedDrinksModelProvider.Entry> data, Context context) {
        entries = data;
        data.keySet().forEach(id -> {
            context.addModels(getGroundModelId(id).id(), getGroundModelFluidId(id).id());
        });
    }

    public Optional<Entry> get(Item item) {
        return Optional.ofNullable(entries.get(Registries.ITEM.getId(item)));
    }

    public void renderDrink(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        int dyeColor = DyedColorComponent.getColor(stack, Colors.WHITE);

        renderDrinkModel(stack, matrices, vertices, light, overlay, dyeColor, getGroundModelId(stack.getItem()));

        float fillPercentage = FluidCapacity.getPercentage(stack);
        if (fillPercentage > 0.01) {
            float origin = get(stack.getItem()).orElse(Entry.DEFAULT).fluidOrigin() / 16F;
            matrices.translate(0, origin, 0);
            matrices.scale(1, fillPercentage, 1);
            matrices.translate(0, -origin, 0);
            int color = FluidAppearance.getItemColor(ItemFluids.of(stack));
            renderDrinkModel(stack, matrices, vertices, light, overlay, color, getGroundModelFluidId(stack.getItem()));
        }
    }

    public void renderDrinkModel(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay, int color, ModelIdentifier modelId) {
        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();

        BakedModel model = renderer.getModels().getModelManager().getModel(modelId);

        boolean solid = !(stack.getItem() instanceof BlockItem bi) || !(bi.getBlock() instanceof TransparentBlock) && !(bi.getBlock() instanceof StainedGlassPaneBlock);
        RenderLayer renderLayer = RenderLayers.getItemLayer(stack, solid);

        renderBakedItemModel(model, matrices, vertices.getBuffer(renderLayer), light, overlay, color);
    }

    private void renderBakedItemModel(BakedModel model, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        for (Direction direction : Direction.values()) {
            RNG.setSeed(SEED);
            renderBakedItemQuads(matrices, vertices, model.getQuads(null, direction, RNG), light, overlay, color);
        }
        RNG.setSeed(SEED);
        renderBakedItemQuads(matrices, vertices, model.getQuads(null, null, RNG), light, overlay, color);
    }

    private void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, int light, int overlay, int color) {
        MatrixStack.Entry entry = matrices.peek();
        for (BakedQuad bakedQuad : quads) {
            vertices.quad(entry, bakedQuad, MathUtils.r(color), MathUtils.g(color), MathUtils.b(color), MathUtils.a(color), light, overlay);
        }
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

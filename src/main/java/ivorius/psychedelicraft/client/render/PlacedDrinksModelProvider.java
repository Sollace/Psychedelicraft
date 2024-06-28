package ivorius.psychedelicraft.client.render;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class PlacedDrinksModelProvider
        implements PreparableModelLoadingPlugin<Map<String, Map<Identifier, PlacedDrinksModelProvider.Entry>>>,
        PreparableModelLoadingPlugin.DataLoader<Map<String, Map<Identifier, PlacedDrinksModelProvider.Entry>>> {
    private static final Identifier CONFIG_LOCATION = Psychedelicraft.id("placeable_drinks.json");
    private static final Gson GSON = new Gson();
    private static final Random RNG = Random.create();
    private static final long SEED = 42L;

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
    public void onInitializeModelLoader(Map<String, Map<Identifier, PlacedDrinksModelProvider.Entry>> data, Context context) {
        entries = data;
        data.forEach((type, entries) -> {
            entries.keySet().forEach(id -> {
                context.addModels(getGroundModelId(type, id), getGroundModelFluidId(type, id));
            });
        });
    }

    public Optional<Entry> get(String type, Item item) {
        return Optional.ofNullable(entries.get(type).get(Registries.ITEM.getId(item)));
    }

    public void renderDrink(String type, ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        renderEmptyDrink(type, stack, matrices, vertices, light, overlay);
        float fillPercentage = FluidCapacity.getPercentage(stack);
        if (fillPercentage > 0.01) {
            float origin = get(type, stack.getItem()).orElse(Entry.DEFAULT).fluidOrigin() / 16F;
            matrices.translate(0, origin, 0);
            matrices.scale(1, fillPercentage, 1);
            matrices.translate(0, -origin, 0);
            int color = FluidAppearance.getItemColor(ItemFluids.of(stack));
            renderDrinkModel(stack, matrices, vertices, light, overlay, color, getGroundModelFluidId(type, stack.getItem()));
        }
    }

    public void renderEmptyDrink(String type, ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        int dyeColor = DyedColorComponent.getColor(stack, Colors.WHITE);

        renderDrinkModel(stack, matrices, vertices, light, overlay, dyeColor, getGroundModelId(type, stack.getItem()));
    }

    public void renderDrinkModel(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay, int color, Identifier modelId) {
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

    public static Identifier getGroundModelId(String type, Item item) {
        return getGroundModelId(type, Registries.ITEM.getId(item));
    }

    public static Identifier getGroundModelFluidId(String type, Item item) {
        return getGroundModelFluidId(type, Registries.ITEM.getId(item));
    }

    public static Identifier getGroundModelId(String type, Identifier item) {
        return item.withPath(p -> "item/" + p + "_on_" + type);
    }

    public static Identifier getGroundModelFluidId(String type, Identifier item) {
        return getGroundModelId(type, item).withSuffixedPath("_fluid");
    }

    public record Entry(float height, float fluidOrigin) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("height").forGetter(Entry::height),
                Codec.FLOAT.fieldOf("fluid_origin").forGetter(Entry::fluidOrigin)
        ).apply(instance, Entry::new));
        public static final Codec<Map<Identifier, Entry>> MAP_CODEC = Codec.unboundedMap(Identifier.CODEC, CODEC);
        public static final Entry DEFAULT = new Entry(0.5F, 0F);
    }
}

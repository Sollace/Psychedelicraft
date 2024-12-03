package ivorius.psychedelicraft.datagen.providers;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.spongepowered.include.com.google.common.base.Preconditions;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.BlockStateVariantMap;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.MultipartBlockStateSupplier;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.client.TexturedModel;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.VariantSettings.Rotation;
import net.minecraft.data.client.VariantsBlockStateSupplier;
import net.minecraft.data.client.When;
import net.minecraft.data.client.When.PropertyCondition;
import net.minecraft.data.client.BlockStateModelGenerator.TintType;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;

public interface BlockModels {
    TextureKey CONNECTION = TextureKey.of("connection");
    Model VINE_CONNECTION_TEMPLATE = block("vine_connection_template", CONNECTION);

    TextureKey LATTICE = TextureKey.of("lattice");
    Model CROP_LATTICE_TEMPLATE = block("crop_lattice_template", LATTICE, TextureKey.CROP);
    Model LATTICE_TEMPLATE = block("lattice_template", LATTICE);
    Model COMPLEX_BLOCK = block("complex_block");
    Model VAT_TEMPLATE = block("vat_template", TextureKey.ALL);

    static Model block(String parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(Psychedelicraft.id("block/" + parent)), Optional.empty(), requiredTextureKeys);
    }

    static void generateWoodset(BlockStateModelGenerator generator, BlockFamily family,
            Block log, Block wood,
            Block strippedLog, Block strippedWood,
            Block hangingSign, Block wallHangingSign,
            Block leaves,
            Block sapling, Block pottedSapling
    ) {
        generator.registerLog(log).log(log).wood(wood);
        generator.registerLog(strippedLog).log(strippedLog).wood(strippedWood);
        generator.registerCubeAllModelTexturePool(family.getBaseBlock()).family(family);
        generator.registerHangingSign(strippedLog, hangingSign, wallHangingSign);
        generator.registerSingleton(leaves, TexturedModel.LEAVES);
        generator.registerFlowerPotPlant(sapling, pottedSapling, TintType.NOT_TINTED);
    }

    static void registerBarrel(BlockStateModelGenerator generator, Block block) {
        Identifier planksId = Registries.BLOCK.getId(block).withPath(p -> p.replace("_barrel", "_planks"));
        generator.registerBuiltin(block, Registries.BLOCK.getOrEmpty(planksId).or(() -> {
            return Registries.BLOCK.getOrEmpty(Identifier.ofVanilla(planksId.getPath()));
        }).orElse(Blocks.OAK_PLANKS)).includeWithoutItem(block);
        generator.registerItemModel(block.asItem());
    }

    static BiConsumer<SimpleFluid, String> createFluidCollector(BlockStateModelGenerator generator) {
        var appearances = Util.memoize(appearance -> {
            Identifier id = Psychedelicraft.id("block/fluid/" + appearance);
            return Models.PARTICLE.upload(id, TextureMap.particle(id.withSuffixedPath("_still")), generator.modelCollector);
        });
        return (fluid, appearance) -> generator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(fluid.getPhysical().getBlock(), appearances.apply(appearance)));
    }

    static void registerCrossCrop(BlockStateModelGenerator generator, Block crop, Property<Integer> ageProperty, int... ageTextureIndices) {
        registerCrossCrop(generator, createCropModelSupplier(generator, crop), crop, ageProperty, ageTextureIndices);
    }

    static Function<Integer, Identifier> createCropModelSupplier(BlockStateModelGenerator generator, Block crop) {
        return createCropModelSupplier(generator, Models.CROSS, TextureMap::cross, crop);
    }

    static Function<Integer, Identifier> createCropModelSupplier(BlockStateModelGenerator generator, Model model, Function<Identifier, TextureMap> textures, Block crop) {
        return Util.memoize(i -> generator.createSubModel(crop, "_stage" + i, model, textures));
    }

    static void registerCrossCrop(BlockStateModelGenerator generator,
            Function<Integer, Identifier> models, Block crop,
            Property<Integer> ageProperty,
            int... ageTextureIndices) {
        Preconditions.checkArgument(ageProperty.getValues().size() == ageTextureIndices.length);
        generator.registerItemModel(crop.asItem());
        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(crop)
                .coordinate(BlockStateVariantMap.create(ageProperty)
                        .register(integer -> BlockStateVariant.create().put(VariantSettings.MODEL, models.apply(ageTextureIndices[integer])))));
    }

    static <T extends Comparable<T>> void registerCrossCrop(BlockStateModelGenerator generator,
            Block crop,
            Property<Integer> ageProperty,
            Property<T> partProperty, Function<T, String> partNameFunction,
            int... ageTextureIndices) {
        registerCrossCrop(generator, crop, ageProperty, partProperty, partNameFunction, part -> ageTextureIndices);
    }

    static <T extends Comparable<T>> void registerCrossCrop(BlockStateModelGenerator generator,
            Block crop,
            Property<Integer> ageProperty,
            Property<T> partProperty, Function<T, String> partNameFunction,
            Function<T, int[]> ageTextureIndicesFunction) {
        BiFunction<Integer, T, Identifier> models = Util.memoize((age, part) -> generator.createSubModel(crop, partNameFunction.apply(part) + "_stage" + age, Models.CROSS, TextureMap::cross));
        Function<T, int[]> textureIndices = Util.memoize(part -> {
            int[] indices = ageTextureIndicesFunction.apply(part);
            Preconditions.checkArgument(ageProperty.getValues().size() == indices.length);
            return indices;
        });
        var states = VariantsBlockStateSupplier.create(crop)
                .coordinate(BlockStateVariantMap.create(ageProperty, partProperty)
                        .register((age, part) -> BlockStateVariant.create().put(VariantSettings.MODEL, models.apply(textureIndices.apply(part)[age], part))));
        generator.registerItemModel(crop.asItem());
        generator.blockStateCollector.accept(states);
    }

    static void registerVineCrop(BlockStateModelGenerator generator, Block crop, Property<Integer> ageProperty, int... ageTextureIndices) {
        Preconditions.checkArgument(ageProperty.getValues().size() == ageTextureIndices.length);
        var models = createCropModelSupplier(generator, crop);
        var connectionModelId = generator.createSubModel(crop, "_connection", VINE_CONNECTION_TEMPLATE, id -> TextureMap.of(CONNECTION, id));
        generator.registerItemModel(crop.asItem());
        generator.blockStateCollector.accept(Util.make(MultipartBlockStateSupplier.create(crop), states ->
                ageProperty.getValues().forEach(age -> {
                    states.with(When.create().set(ageProperty, age), BlockStateVariant.create()
                            .put(VariantSettings.MODEL, models.apply(ageTextureIndices[age]))
                    );
                }))
                .with(When.create().set(Properties.NORTH, true), BlockStateVariant.create()
                    .put(VariantSettings.MODEL, connectionModelId)
                    .put(VariantSettings.Y, Rotation.R270)
                ).with(When.create().set(Properties.SOUTH, true), BlockStateVariant.create()
                    .put(VariantSettings.MODEL, connectionModelId)
                    .put(VariantSettings.Y, Rotation.R90)
                ).with(When.create().set(Properties.EAST, true), BlockStateVariant.create()
                    .put(VariantSettings.MODEL, connectionModelId)
                ).with(When.create().set(Properties.WEST, true), BlockStateVariant.create()
                    .put(VariantSettings.MODEL, connectionModelId)
                    .put(VariantSettings.Y, Rotation.R180)
                ));
    }

    static void registerLatticeCrop(BlockStateModelGenerator generator, Block lattice, Block crop, Property<Integer> ageProperty, int... ageTextureIndices) {
        var models = createCropModelSupplier(generator, CROP_LATTICE_TEMPLATE, id -> TextureMap.of(LATTICE, TextureMap.getId(lattice)).put(TextureKey.CROP, id), crop);
        generator.blockStateCollector.accept(Util.make(MultipartBlockStateSupplier.create(crop), states -> {
                ageProperty.getValues().forEach(age -> addLatticeStates(states, () -> When.create().set(ageProperty, age), models.apply(age)));
        }));
    }

    static void registerLattice(BlockStateModelGenerator generator, Block lattice) {
        TextureMap textures = TextureMap.of(LATTICE, TextureMap.getId(lattice));
        Identifier model = LATTICE_TEMPLATE.upload(lattice, textures, generator.modelCollector);
        generator.blockStateCollector.accept(addLatticeStates(MultipartBlockStateSupplier.create(lattice), When::create, model));
        ItemModels.LATTICE_TEMPLATE.upload(ModelIds.getItemModelId(lattice.asItem()), textures, generator.modelCollector);
    }

    static MultipartBlockStateSupplier addLatticeStates(MultipartBlockStateSupplier states, Supplier<PropertyCondition> when, Identifier model) {
        return states.with(when.get().set(Properties.NORTH, true), BlockStateVariant.create()
            .put(VariantSettings.MODEL, model)
            .put(VariantSettings.UVLOCK, true)
        ).with(when.get().set(Properties.SOUTH, true), BlockStateVariant.create()
            .put(VariantSettings.MODEL, model)
            .put(VariantSettings.Y, Rotation.R180)
            .put(VariantSettings.UVLOCK, true)
        ).with(when.get().set(Properties.EAST, true), BlockStateVariant.create()
            .put(VariantSettings.MODEL, model)
            .put(VariantSettings.Y, Rotation.R90)
            .put(VariantSettings.UVLOCK, true)
        ).with(when.get().set(Properties.WEST, true), BlockStateVariant.create()
            .put(VariantSettings.MODEL, model)
            .put(VariantSettings.Y, Rotation.R270)
            .put(VariantSettings.UVLOCK, true)
        );
    }

    static void registerCropPot(BlockStateModelGenerator generator, Block plantBlock, Block flowerPotBlock, BlockStateModelGenerator.TintType tintType, String suffix) {
        TextureMap textureMap = TextureMap.plant(TextureMap.getSubId(plantBlock, suffix));
        Identifier identifier = tintType.getFlowerPotCrossModel().upload(flowerPotBlock, textureMap, generator.modelCollector);
        generator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(flowerPotBlock, identifier));
    }

    static void registerDistillery(BlockStateModelGenerator generator, Block block) {
        Identifier modelId = ModelIds.getBlockModelId(block);
        Identifier condenserModelId = ModelIds.getBlockSubModelId(block, "_condenser");
        generator.blockStateCollector.accept(MultipartBlockStateSupplier.create(block)
                .with(BlockStateVariant.create().put(VariantSettings.MODEL, modelId))
                .with(When.create().set(Properties.FACING, Direction.EAST), BlockStateVariant.create()
                        .put(VariantSettings.MODEL, condenserModelId)
                        .put(VariantSettings.Y, Rotation.R270)
                ).with(When.create().set(Properties.FACING, Direction.NORTH), BlockStateVariant.create()
                        .put(VariantSettings.MODEL, condenserModelId)
                        .put(VariantSettings.Y, Rotation.R180)
                ).with(When.create().set(Properties.FACING, Direction.SOUTH), BlockStateVariant.create()
                        .put(VariantSettings.MODEL, condenserModelId)
                        .put(VariantSettings.Y, Rotation.R0)
                ).with(When.create().set(Properties.FACING, Direction.WEST), BlockStateVariant.create()
                        .put(VariantSettings.MODEL, condenserModelId)
                        .put(VariantSettings.Y, Rotation.R90)
                )
        );
        generator.registerParentedItemModel(block, modelId);
    }

    static void registerVat(BlockStateModelGenerator generator, Block core, Block edge, Block materialBase) {
        generator.registerBuiltinWithParticle(edge, ModelIds.getBlockModelId(materialBase));
        generator.registerSingleton(core, TextureMap.all(core), BlockModels.VAT_TEMPLATE);
    }
}

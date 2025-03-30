package ivorius.psychedelicraft.datagen.providers;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.BurnerBlock;
import ivorius.psychedelicraft.block.GlassTubeBlock;
import ivorius.psychedelicraft.block.GlassTubeBlock.IODirection;
import ivorius.psychedelicraft.block.ValveBlock;
import ivorius.psychedelicraft.client.item.VatItemModelRenderer;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.BlockStateModelGenerator.CrossType;
import net.minecraft.client.data.BlockStateVariantMap;
import net.minecraft.client.data.Model;
import net.minecraft.client.data.ModelIds;
import net.minecraft.client.data.Models;
import net.minecraft.client.data.MultipartBlockModelDefinitionCreator;
import net.minecraft.client.data.TextureKey;
import net.minecraft.client.data.TextureMap;
import net.minecraft.client.data.TexturedModel;
import net.minecraft.client.data.VariantsBlockModelDefinitionCreator;
import net.minecraft.client.render.model.json.ModelVariantOperator;
import net.minecraft.client.render.model.json.MultipartModelConditionBuilder;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisRotation;
import net.minecraft.util.math.Direction;

import static net.minecraft.client.data.BlockStateModelGenerator.*;
import static net.minecraft.client.data.ItemModels.*;

public interface BlockModels {
    TextureKey CONNECTION = TextureKey.of("connection");
    Model VINE_CONNECTION_TEMPLATE = block("vine_connection_template", CONNECTION);

    TextureKey LATTICE = TextureKey.of("lattice");
    Model CROP_LATTICE_TEMPLATE = block("crop_lattice_template", LATTICE, TextureKey.CROP);
    Model LATTICE_TEMPLATE = block("lattice_template", LATTICE);

    Identifier COMPLEX_BLOCK_ID = Psychedelicraft.id("block/complex_block");

    Model COMPLEX_BLOCK = block("complex_block", TextureKey.TEXTURE, TextureKey.PARTICLE);
    Model VAT_TEMPLATE = block("vat_template", TextureKey.ALL);
    Model TRAY_TEMPLATE = block("tray_template", TextureKey.ALL);

    Model DRYING_TABLE_TEMPLATE = block("drying_table_template", TextureKey.BOTTOM, TextureKey.SIDE, TextureKey.TOP);
    Model BUNSEN_BURNER = block("bunsen_burner", TextureKey.BOTTOM);

    TexturedModel.Factory VAT = TexturedModel.makeFactory(TextureMap::all, VAT_TEMPLATE);
    TexturedModel.Factory DRYING_TABLE = TexturedModel.makeFactory(block -> TextureMap.sideTopBottom(block), BlockModels.DRYING_TABLE_TEMPLATE);

    BlockStateVariantMap<ModelVariantOperator> NORTH_DEFAULT_ROTATION_OPERATIONS = BlockStateVariantMap.operations(Properties.FACING)
            .register(Direction.DOWN, ROTATE_X_90)
            .register(Direction.UP, ROTATE_X_270)
            .register(Direction.NORTH, NO_OP)
            .register(Direction.SOUTH, ROTATE_Y_180)
            .register(Direction.WEST, ROTATE_Y_270)
            .register(Direction.EAST, ROTATE_Y_90);

    static Model block(String parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(Psychedelicraft.id("block/" + parent)), Optional.empty(), requiredTextureKeys);
    }

    static void registerSpecialItemModel(BlockStateModelGenerator generator, Block block, Identifier parent, SpecialModelRenderer.Unbaked unbaked) {
        generator.itemModelOutput.accept(block.asItem(), special(parent, unbaked));
    }

    static void generateWoodset(BlockStateModelGenerator generator, BlockFamily family,
            Block log, Block wood,
            Block strippedLog, Block strippedWood,
            Block hangingSign, Block wallHangingSign,
            Block leaves,
            Block sapling, Block pottedSapling
    ) {
        generator.createLogTexturePool(log).log(log).wood(wood);
        generator.createLogTexturePool(strippedLog).log(strippedLog).wood(strippedWood);
        generator.registerCubeAllModelTexturePool(family.getBaseBlock()).family(family);
        generator.registerHangingSign(strippedLog, hangingSign, wallHangingSign);
        generator.registerSingleton(leaves, TexturedModel.LEAVES);
        generator.registerFlowerPotPlant(sapling, pottedSapling, CrossType.NOT_TINTED);
    }

    static void registerBarrel(BlockStateModelGenerator generator, Block block) {
        Identifier planksId = Registries.BLOCK.getId(block).withPath(p -> p.replace("_barrel", "_planks"));
        generator.registerBuiltinWithParticle(block, Registries.BLOCK.getOptionalValue(planksId).or(() -> {
            return Registries.BLOCK.getOptionalValue(Identifier.ofVanilla(planksId.getPath()));
        }).orElse(Blocks.OAK_PLANKS));
        generator.registerItemModel(block.asItem());
    }

    static BiConsumer<SimpleFluid, String> createFluidCollector(BlockStateModelGenerator generator) {
        var appearances = Util.memoize(appearance -> {
            Identifier id = Psychedelicraft.id("block/fluid/" + appearance);
            return createWeightedVariant(Models.PARTICLE.upload(id, TextureMap.particle(id.withSuffixedPath("_still")), generator.modelCollector));
        });
        return (fluid, appearance) -> generator.blockStateCollector.accept(createSingletonBlockState(fluid.getPhysical().getBlock(), appearances.apply(appearance)));
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
        generator.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(crop)
                .with(BlockStateVariantMap.models(ageProperty)
                        .generate(age -> createWeightedVariant(models.apply(ageTextureIndices[age])))));
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
        var states = VariantsBlockModelDefinitionCreator.of(crop)
                .with(BlockStateVariantMap.models(ageProperty, partProperty)
                        .generate((age, part) -> createWeightedVariant(models.apply(textureIndices.apply(part)[age], part))));
        generator.registerItemModel(crop.asItem());
        generator.blockStateCollector.accept(states);
    }

    static void registerVineCrop(BlockStateModelGenerator generator, Block crop, Property<Integer> ageProperty, int... ageTextureIndices) {
        Preconditions.checkArgument(ageProperty.getValues().size() == ageTextureIndices.length);
        var models = createCropModelSupplier(generator, crop);
        var connectionModelId = generator.createSubModel(crop, "_connection", VINE_CONNECTION_TEMPLATE, id -> TextureMap.of(CONNECTION, id));
        generator.registerItemModel(crop.asItem());
        generator.blockStateCollector.accept(Util.make(MultipartBlockModelDefinitionCreator.create(crop), states ->
                ageProperty.getValues().forEach(age -> {
                    states.with(createMultipartConditionBuilder().put(ageProperty, age), createWeightedVariant(models.apply(ageTextureIndices[age])));
                }))
                .with(createMultipartConditionBuilder().put(Properties.NORTH, true), createWeightedVariant(createModelVariant(connectionModelId).withRotationY(AxisRotation.R270)))
                .with(createMultipartConditionBuilder().put(Properties.SOUTH, true), createWeightedVariant(createModelVariant(connectionModelId).withRotationY(AxisRotation.R90)))
                .with(createMultipartConditionBuilder().put(Properties.EAST, true), createWeightedVariant(createModelVariant(connectionModelId)))
                .with(createMultipartConditionBuilder().put(Properties.WEST, true), createWeightedVariant(createModelVariant(connectionModelId).withRotationY(AxisRotation.R180))));
    }

    static void registerLatticeCrop(BlockStateModelGenerator generator, Block lattice, Block crop, Property<Integer> ageProperty, int... ageTextureIndices) {
        var models = createCropModelSupplier(generator, CROP_LATTICE_TEMPLATE, id -> TextureMap.of(LATTICE, TextureMap.getId(lattice)).put(TextureKey.CROP, id), crop);
        generator.blockStateCollector.accept(Util.make(MultipartBlockModelDefinitionCreator.create(crop), states -> {
                ageProperty.getValues().forEach(age -> addLatticeStates(states, () -> createMultipartConditionBuilder().put(ageProperty, age), models.apply(age)));
        }));
    }

    static void registerLattice(BlockStateModelGenerator generator, Block lattice) {
        TextureMap textures = TextureMap.of(LATTICE, TextureMap.getId(lattice));
        Identifier model = LATTICE_TEMPLATE.upload(lattice, textures, generator.modelCollector);
        generator.blockStateCollector.accept(addLatticeStates(MultipartBlockModelDefinitionCreator.create(lattice), BlockStateModelGenerator::createMultipartConditionBuilder, model));
        ItemModels.LATTICE_TEMPLATE.upload(ModelIds.getItemModelId(lattice.asItem()), textures, generator.modelCollector);
    }

    static MultipartBlockModelDefinitionCreator addLatticeStates(MultipartBlockModelDefinitionCreator states, Supplier<MultipartModelConditionBuilder> when, Identifier model) {
        return states
             .with(when.get().put(Properties.NORTH, true), createWeightedVariant(createModelVariant(model).withUVLock(true)))
             .with(when.get().put(Properties.SOUTH, true), createWeightedVariant(createModelVariant(model).withUVLock(true).withRotationY(AxisRotation.R180)))
             .with(when.get().put(Properties.EAST, true), createWeightedVariant(createModelVariant(model).withUVLock(true).withRotationY(AxisRotation.R90)))
             .with(when.get().put(Properties.WEST, true), createWeightedVariant(createModelVariant(model).withUVLock(true).withRotationY(AxisRotation.R270)));
    }

    static void registerCropPot(BlockStateModelGenerator generator, Block plantBlock, Block flowerPotBlock, CrossType tintType, String suffix) {
        TextureMap textureMap = TextureMap.plant(TextureMap.getSubId(plantBlock, suffix));
        Identifier identifier = tintType.getFlowerPotCrossModel().upload(flowerPotBlock, textureMap, generator.modelCollector);
        generator.blockStateCollector.accept(createSingletonBlockState(flowerPotBlock, createWeightedVariant(identifier)));
    }

    static void registerBunsenBurner(BlockStateModelGenerator generator, Block block) {
        Identifier modelId = ModelIds.getBlockModelId(block);
        Identifier litModelId = BUNSEN_BURNER.upload(ModelIds.getBlockSubModelId(block, "_lit"), TextureMap.of(TextureKey.BOTTOM, TextureMap.getSubId(block, "_base_lit")), generator.modelCollector);
        generator.registerParentedItemModel(block, modelId);
        generator.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block)
                .with(createBooleanModelMap(BurnerBlock.LIT, createWeightedVariant(litModelId), createWeightedVariant(modelId))
        ));
    }

    static void registerTray(BlockStateModelGenerator generator, Block tray) {
        Identifier modelId = TRAY_TEMPLATE.upload(tray, TextureMap.all(tray), generator.modelCollector);
        generator.registerParentedItemModel(tray, modelId);
        generator.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(tray, createWeightedVariant(modelId))
                .coordinate(BlockStateVariantMap.operations(Properties.HORIZONTAL_AXIS)
                        .register(Direction.Axis.X, ROTATE_Y_90)
                        .register(Direction.Axis.Z, NO_OP))
        );
    }

    static void registerDistillery(BlockStateModelGenerator generator, Block block) {
        Identifier modelId = ModelIds.getBlockModelId(block);
        Identifier condenserModelId = ModelIds.getBlockSubModelId(block, "_condenser");

        generator.blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(block)
                .with(createWeightedVariant(modelId))
                .with(createMultipartConditionBuilder().put(Properties.FACING, Direction.EAST), createWeightedVariant(createModelVariant(condenserModelId).withRotationY(AxisRotation.R270)))
                .with(createMultipartConditionBuilder().put(Properties.FACING, Direction.NORTH), createWeightedVariant(createModelVariant(condenserModelId).withRotationY(AxisRotation.R180)))
                .with(createMultipartConditionBuilder().put(Properties.FACING, Direction.SOUTH), createWeightedVariant(createModelVariant(condenserModelId).withRotationY(AxisRotation.R0)))
                .with(createMultipartConditionBuilder().put(Properties.FACING, Direction.WEST), createWeightedVariant(createModelVariant(condenserModelId).withRotationY(AxisRotation.R90)))
        );
        generator.registerParentedItemModel(block, modelId);
    }

    static void registerVat(BlockStateModelGenerator generator, Block core, Block edge, Block materialBase) {
        generator.registerBuiltinWithParticle(edge, ModelIds.getBlockModelId(materialBase));
        generator.registerSingleton(core, VAT);
        generator.registerSpecialItemModel(core, new VatItemModelRenderer.Unbaked());
    }

    static void registerDryingTable(BlockStateModelGenerator generator, Block block) {
        generator.registerSingleton(block, DRYING_TABLE);
        generator.registerParentedItemModel(block, ModelIds.getBlockModelId(block));
    }

    static void registerTubing(BlockStateModelGenerator generator, Block block) {
        MultipartBlockModelDefinitionCreator states = MultipartBlockModelDefinitionCreator.create(block);
        addPipeConnectionStates(states, GlassTubeBlock.IN, ModelIds.getBlockSubModelId(block, "_in"), BlockStateModelGenerator::createMultipartConditionBuilder);
        addPipeConnectionStates(states, GlassTubeBlock.OUT, ModelIds.getBlockSubModelId(block, "_out"), BlockStateModelGenerator::createMultipartConditionBuilder);
        addPipeExtensionStates(states, GlassTubeBlock.IN, GlassTubeBlock.EXTENDED_IN, ModelIds.getBlockModelId(block));
        addPipeExtensionStates(states, GlassTubeBlock.OUT, GlassTubeBlock.EXTENDED_OUT, ModelIds.getBlockModelId(block));
        generator.itemModelOutput.accept(block.asItem(), basic(
                Models.HANDHELD_ROD.upload(
                        ModelIds.getItemModelId(block.asItem()),
                        TextureMap.layer0(TextureMap.getId(block.asItem())),
                        generator.modelCollector)));
        generator.blockStateCollector.accept(states);
    }

    static void registerTubingWithTap(BlockStateModelGenerator generator, Block tube, Block block) {
        MultipartBlockModelDefinitionCreator states = MultipartBlockModelDefinitionCreator.create(block);
        addPipeConnectionStates(states, GlassTubeBlock.IN, ModelIds.getBlockSubModelId(tube, "_in"), BlockStateModelGenerator::createMultipartConditionBuilder);
        addPipeConnectionStates(states, GlassTubeBlock.OUT, ModelIds.getBlockSubModelId(tube, "_out"), BlockStateModelGenerator::createMultipartConditionBuilder);
        addPipeExtensionStates(states, GlassTubeBlock.IN, GlassTubeBlock.EXTENDED_IN, ModelIds.getBlockModelId(tube));
        addPipeExtensionStates(states, GlassTubeBlock.OUT, GlassTubeBlock.EXTENDED_OUT, ModelIds.getBlockModelId(tube));
        states
            .with(createMultipartConditionBuilder().put(ValveBlock.OPEN, true), createWeightedVariant(ModelIds.getBlockSubModelId(block, "_open")))
            .with(createMultipartConditionBuilder().put(ValveBlock.OPEN, false), createWeightedVariant(ModelIds.getBlockSubModelId(block, "_closed")));
        generator.itemModelOutput.accept(block.asItem(), basic(
                Models.HANDHELD_ROD.upload(ModelIds.getItemModelId(block.asItem()),
                TextureMap.layer0(TextureMap.getId(block.asItem())),
                generator.modelCollector)));
        generator.blockStateCollector.accept(states);
    }

    static void registerPump(BlockStateModelGenerator generator, Block block) {
        Identifier normal = ModelIds.getBlockModelId(block);
        Identifier powered = ModelIds.getBlockSubModelId(block, "_extended");
        generator.blockStateCollector.accept(
            VariantsBlockModelDefinitionCreator.of(block)
                .with(createBooleanModelMap(Properties.POWERED, createWeightedVariant(powered), createWeightedVariant(normal)))
                .coordinate(NORTH_DEFAULT_ROTATION_OPERATIONS)
        );
        generator.registerParentedItemModel(block, normal);
    }

    static void registerPumpHead(BlockStateModelGenerator generator, Block block) {
        Identifier normal = ModelIds.getBlockModelId(block);
        generator.blockStateCollector.accept(
            VariantsBlockModelDefinitionCreator.of(block, BlockStateModelGenerator.createWeightedVariant(normal))
                .coordinate(NORTH_DEFAULT_ROTATION_OPERATIONS)
        );
    }

    static MultipartBlockModelDefinitionCreator addPipeConnectionStates(MultipartBlockModelDefinitionCreator states, EnumProperty<IODirection> property, Identifier model, Supplier<MultipartModelConditionBuilder> when) {
        return states
             .with(when.get().put(property, IODirection.UP), createWeightedVariant(createModelVariant(model).withRotationX(AxisRotation.R180)))
             .with(when.get().put(property, IODirection.DOWN), createWeightedVariant(createModelVariant(model)))
             .with(when.get().put(property, IODirection.EAST), createWeightedVariant(createModelVariant(model).withRotationX(AxisRotation.R90).withRotationY(AxisRotation.R270)))
             .with(when.get().put(property, IODirection.WEST), createWeightedVariant(createModelVariant(model).withRotationX(AxisRotation.R90).withRotationY(AxisRotation.R90)))
             .with(when.get().put(property, IODirection.NORTH), createWeightedVariant(createModelVariant(model).withRotationX(AxisRotation.R90).withRotationY(AxisRotation.R180)))
             .with(when.get().put(property, IODirection.SOUTH), createWeightedVariant(createModelVariant(model).withRotationX(AxisRotation.R90)));
    }

    static MultipartBlockModelDefinitionCreator addPipeExtensionStates(MultipartBlockModelDefinitionCreator states, EnumProperty<IODirection> property, BooleanProperty extensionProperty, Identifier model) {
        return addPipeConnectionStates(states, property, model.withSuffixedPath("_extension"), () -> createMultipartConditionBuilder().put(extensionProperty, true));
    }
}

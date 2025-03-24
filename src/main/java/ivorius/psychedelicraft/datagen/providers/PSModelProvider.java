package ivorius.psychedelicraft.datagen.providers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.AgavePlantBlock;
import ivorius.psychedelicraft.block.BurdenedLatticeBlock;
import ivorius.psychedelicraft.block.NightshadeBlock;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.TobaccoPlantBlock;
import ivorius.psychedelicraft.block.VineStemBlock;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Blocks;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.BlockStateModelGenerator.CrossType;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.ModelIds;
import net.minecraft.client.data.Models;
import net.minecraft.client.data.TextureMap;
import net.minecraft.client.data.TexturedModel;
import net.minecraft.client.data.VariantsBlockModelDefinitionCreator;
import net.minecraft.client.render.item.tint.DyeTintSource;
import net.minecraft.item.Items;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class PSModelProvider extends FabricModelProvider {
    public PSModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        BlockModels.generateWoodset(generator, PSBlockFamilies.JUNIPER,
                PSBlocks.JUNIPER_LOG, PSBlocks.JUNIPER_WOOD,
                PSBlocks.STRIPPED_JUNIPER_LOG, PSBlocks.STRIPPED_JUNIPER_WOOD,
                PSBlocks.JUNIPER_HANGING_SIGN, PSBlocks.JUNIPER_WALL_HANGING_SIGN,
                PSBlocks.JUNIPER_LEAVES,
                PSBlocks.JUNIPER_SAPLING, PSBlocks.POTTED_JUNIPER_SAPLING
        );

        generator.registerSingleton(PSBlocks.FRUITING_JUNIPER_LEAVES, TexturedModel.LEAVES);
        generator.registerParentedItemModel(PSBlocks.FRUITING_JUNIPER_LEAVES, ModelIds.getBlockModelId(PSBlocks.FRUITING_JUNIPER_LEAVES));
        generator.registerSimpleCubeAll(PSBlocks.GLITCH);

        PSBlocks.ALL_BARRELS.forEach(block -> BlockModels.registerBarrel(generator, block));

        List.of(
                PSBlocks.FLASK,
                PSBlocks.BOTTLE_RACK
        ).forEach(block -> {
            generator.registerParentedItemModel(block, ModelIds.getBlockModelId(block));
        });

        BlockModels.registerBunsenBurner(generator, PSBlocks.BUNSEN_BURNER);
        BlockModels.registerTray(generator, PSBlocks.TRAY);
        BlockModels.registerDistillery(generator, PSBlocks.DISTILLERY);
        BlockModels.registerTubing(generator, PSBlocks.GLASS_TUBE);
        BlockModels.registerTubingWithTap(generator, PSBlocks.GLASS_TUBE, PSBlocks.GLASS_VALVE);
        BlockModels.registerPump(generator, PSBlocks.PUMP);
        BlockModels.registerPumpHead(generator, PSBlocks.PUMP_HEAD);

        generator.registerBuiltinWithParticle(PSBlocks.RIFT_JAR, Blocks.GLASS);

        generator.registerBuiltinWithParticle(PSBlocks.PEYOTE, ModelIds.getItemModelId(PSItems.PEYOTE));
        generator.registerBuiltinWithParticle(PSBlocks.PLACED_DRINK, ModelIds.getBlockModelId(Blocks.STONE));

        Function<Integer, Identifier> models = BlockModels.createCropModelSupplier(generator, PSBlocks.JIMSONWEED);
        Function<Integer, Identifier> tomatoModels = BlockModels.createCropModelSupplier(generator, PSBlocks.TOMATOES);
        Function<Integer, Identifier> belladonnaModels = BlockModels.createCropModelSupplier(generator, PSBlocks.BELLADONNA);

        BlockModels.registerCrossCrop(generator, PSBlocks.HOP, PSBlocks.HOP.getAgeProperty(), 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3);
        BlockModels.registerCrossCrop(generator, PSBlocks.CANNABIS, PSBlocks.CANNABIS.getAgeProperty(), 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3);
        BlockModels.registerCrossCrop(generator, PSBlocks.COCA, PSBlocks.COCA.getAgeProperty(), 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3);
        BlockModels.registerCrossCrop(generator, models, PSBlocks.JIMSONWEED, NightshadeBlock.AGE, 0, 1, 2, 3, 4, 5, 6, 7);
        BlockModels.registerCrossCrop(generator, age -> (age < 5 ? models : tomatoModels).apply(age), PSBlocks.TOMATOES, NightshadeBlock.AGE, 0, 1, 2, 3, 4, 5, 6, 7);
        BlockModels.registerCrossCrop(generator, age -> (age < 7 ? models : belladonnaModels).apply(age), PSBlocks.BELLADONNA, NightshadeBlock.AGE, 0, 1, 2, 3, 4, 5, 6, 7);
        BlockModels.registerCrossCrop(generator, PSBlocks.TOBACCO, PSBlocks.TOBACCO.getAgeProperty(), TobaccoPlantBlock.TOP, top -> top ? "_top" : "", 0, 0, 1, 1, 2, 2, 3, 3);
        BlockModels.registerCrossCrop(generator, PSBlocks.COFFEA, PSBlocks.COFFEA.getAgeProperty(), TobaccoPlantBlock.TOP, top -> top ? "_top" : "", top -> {
            return top ? new int[] { 0, 1, 2, 3, 3, 3, 3, 3 } : new int[] { 0, 1, 2, 3, 4, 5, 6, 7 };
        });
        BlockModels.registerCrossCrop(generator, i -> ModelIds.getBlockSubModelId(PSBlocks.AGAVE_PLANT, "_stage" + i), PSBlocks.AGAVE_PLANT, AgavePlantBlock.AGE, 0, 1, 2, 3, 4, 5);
        BlockModels.registerVineCrop(generator, PSBlocks.MORNING_GLORY, VineStemBlock.AGE, 0, 1, 2, 3, 4);
        BlockModels.registerLattice(generator, PSBlocks.LATTICE);
        BlockModels.registerLatticeCrop(generator, PSBlocks.LATTICE, PSBlocks.WINE_GRAPE_LATTICE, BurdenedLatticeBlock.AGE, 0, 1, 2, 3);
        BlockModels.registerLatticeCrop(generator, PSBlocks.LATTICE, PSBlocks.MORNING_GLORY_LATTICE, BurdenedLatticeBlock.AGE, 0, 1, 2, 3);

        BlockModels.registerCropPot(generator, PSBlocks.CANNABIS, PSBlocks.POTTED_CANNABIS, CrossType.NOT_TINTED, "_stage3");
        BlockModels.registerCropPot(generator, PSBlocks.COCA, PSBlocks.POTTED_COCA, CrossType.NOT_TINTED, "_stage3");
        BlockModels.registerCropPot(generator, PSBlocks.COFFEA, PSBlocks.POTTED_COFFEA, CrossType.NOT_TINTED, "_top_stage3");
        BlockModels.registerCropPot(generator, PSBlocks.HOP, PSBlocks.POTTED_HOP, CrossType.NOT_TINTED, "_stage3");
        BlockModels.registerCropPot(generator, PSBlocks.MORNING_GLORY, PSBlocks.POTTED_MORNING_GLORY, CrossType.NOT_TINTED, "_stage3");
        BlockModels.registerCropPot(generator, PSBlocks.TOBACCO, PSBlocks.POTTED_TOBACCO, CrossType.NOT_TINTED, "_top_stage3");

        BlockModels.registerVat(generator, PSBlocks.MASH_TUB, PSBlocks.MASH_TUB_EDGE, Blocks.OAK_PLANKS);

        BlockModels.registerDryingTable(generator, PSBlocks.DRYING_TABLE);
        BlockModels.registerDryingTable(generator, PSBlocks.IRON_DRYING_TABLE);

        List.of(PSBlocks.BOTTLE_RACK, PSBlocks.WALL_BOTTLE_RACK).forEach(block -> {
            generator.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(PSBlocks.BOTTLE_RACK)))
                    .coordinate(BlockModels.NORTH_DEFAULT_ROTATION_OPERATIONS));
        });
        generator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(PSBlocks.FLASK, BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(PSBlocks.FLASK))));
        generator.registerStateWithModelReference(PSBlocks.FLAMMABLE_GAS, Blocks.AIR);

        generateFluidModels(generator);
    }

    private void generateFluidModels(BlockStateModelGenerator generator) {
        Map<SimpleFluid, String> fluids = Util.make(new HashMap<>(), map -> {
            List.of(
                    PSFluids.ACID, PSFluids.AGAVE, PSFluids.ATROPINE,
                    PSFluids.BATH_SALTS, PSFluids.BELLADONA_EXTRACT, PSFluids.CAFFEINE,
                    PSFluids.COCAINE, PSFluids.ETHANOL, PSFluids.JIMSONWEED_EXTRACT, PSFluids.MORNING_GLORY_EXTRACT,
                    PSFluids.MORPHINE, PSFluids.PINEAPPLE, PSFluids.SUGAR_CANE
            ).forEach(i -> map.put(i, "clear"));
            List.of(PSFluids.CORN, PSFluids.GASOLINE, PSFluids.POTATO, PSFluids.WHEAT_HOP, PSFluids.WHEAT).forEach(i -> map.put(i, "beer"));
            List.of(PSFluids.CANNABIS_TEA, PSFluids.COCA_TEA, PSFluids.PEYOTE_JUICE).forEach(i -> map.put(i, "tea"));
            List.of(PSFluids.COFFEE, PSFluids.KAVA, PSFluids.PETROLIUM).forEach(i -> map.put(i, "coffee"));
            List.of(PSFluids.BANANA, PSFluids.HONEY).forEach(i -> map.put(i, "mead"));
            List.of(PSFluids.MILK, PSFluids.RICE).forEach(i -> map.put(i, "rice_wine"));

            map.put(PSFluids.APPLE, "cider");
            map.put(PSFluids.JUNIPER, "slurry");
            map.put(PSFluids.RED_GRAPES, "wine");
            map.put(PSFluids.SLURRY, "slurry");
            map.put(PSFluids.TOMATO, "tomato_juice");
        });

        var fluidCollector = BlockModels.createFluidCollector(generator);
        SimpleFluid.REGISTRY.forEach(fluid -> {
            if (!fluid.isEmpty()) {
                fluidCollector.accept(fluid, Objects.requireNonNull(fluids.get(fluid), fluid.getId() + " has no mapped appearance for its block"));
            }
        });
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        ItemModels.register(generator,
                PSItems.RED_MAGIC_MUSHROOMS, PSItems.BROWN_MAGIC_MUSHROOMS,
                PSItems.PEYOTE, PSItems.DRIED_PEYOTE,
                PSItems.COFFEE_BEANS,
                PSItems.COCA_LEAVES,
                PSItems.CANNABIS_LEAF, PSItems.CANNABIS_BUDS, PSItems.DRIED_CANNABIS_LEAF, PSItems.DRIED_CANNABIS_BUDS,
                PSItems.DRIED_COCA_LEAVES,
                PSItems.DRIED_JIMSONWEED_LEAF,
                PSItems.HOP_CONES,
                PSItems.MORNING_GLORY,
                PSItems.JIMSONWEED_SEED_POD, PSItems.JIMSONWEED_LEAF,
                PSItems.BELLADONNA_LEAF, PSItems.DRIED_BELLADONNA_LEAF, PSItems.BELLADONNA_BERRIES,
                PSItems.TOBACCO_LEAVES, PSItems.DRIED_TOBACCO,
                PSItems.TOMATO, PSItems.TOMATO_LEAF,
                PSItems.WINE_GRAPES, PSItems.VOMIT,
                PSItems.EXTACY, PSItems.PACIFIER,
                PSItems.LSA_SQUARE, PSItems.LSD_PILL,
                PSItems.CRYSTAL_METH,
                PSItems.MORPHINE_TABLET, PSItems.HEROINE_POWDER, PSItems.HASH_MUFFIN,

                PSItems.OBSIDIAN_BOTTLE,
                PSItems.BAG_O_VOMIT, PSItems.JOLLY_RANCHER, PSItems.BROKEN_GLASS,

                PSItems.JUNIPER_BOAT, PSItems.JUNIPER_CHEST_BOAT, PSItems.JUNIPER_BERRIES
        );

        List.of(
                PSItems.JOINT, PSItems.PEYOTE_JOINT,
                PSItems.CIGARETTE
        ).forEach(item -> ItemModels.registerSmokeable(generator, item));

        List.of(
                PSItems.SMOKING_PIPE,
                PSItems.CRACK_COCAINE,
                PSItems.COCAINE_POWDER,
                PSItems.OBSIDIAN_DUST
        ).forEach(item -> ItemModels.registerSniffable(generator, item));

        List.of(
                PSItems.GLASS_CHALICE,
                PSItems.SHOT_GLASS,
                PSItems.STONE_CUP,
                PSItems.WOODEN_MUG
        ).forEach(item -> ItemModels.registerDrinkHolder(generator, item, "ground", "ground_fluid"));
        ItemModels.registerDrinkHolder(generator, PSItems.SYRINGE);
        ItemModels.registerDyeableDrinkHolder(generator, PSItems.BOTTLE, "ground", "ground_fluid", "burner");
        ItemModels.registerParentedDrinkHolder(generator, PSItems.FILLED_BUCKET, Items.BUCKET,
                ModelIds.getItemModelId(Items.LAVA_BUCKET),
                ModelIds.getItemModelId(Items.WATER_BUCKET)
        );
        ItemModels.registerParentedDrinkHolder(generator, PSItems.FILLED_BOWL, Items.BOWL,
                Models.GENERATED.upload(Psychedelicraft.id("item/lava_bowl"), TextureMap.layer0(Psychedelicraft.id("item/lava_bowl")), generator.modelCollector)
        );
        ItemModels.registerParentedDrinkHolder(generator, PSItems.FILLED_GLASS_BOTTLE, Items.POTION,
                Models.GENERATED.upload(Psychedelicraft.id("item/lava_bottle"), TextureMap.layer0(Psychedelicraft.id("item/lava_bottle")), generator.modelCollector),
                ModelIds.getItemModelId(Items.POTION),
                "burner"
        );

        List.of(PSItems.WINE_GRAPE_LATTICE, PSItems.MORNING_GLORY_LATTICE).forEach(item -> {
            ItemModels.registerPlantLattice(generator, PSBlocks.LATTICE, item);
        });
        ItemModels.registerPaperBag(generator, PSItems.PAPER_BAG);
        ItemModels.registerCigar(generator, PSItems.CIGAR);
        ItemModels.registerBong(generator, PSItems.BONG);
        ItemModels.registerMolotov(generator, PSItems.MOLOTOV_COCKTAIL);
        ItemModels.registerLayered(generator, PSItems.HARMONIUM, "_glowstone", new DyeTintSource(Colors.RED));
    }
}

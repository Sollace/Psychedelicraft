/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.function.Function;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.Experimental;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.BlockEntityTypeSupportHelper;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.world.gen.PSSaplingGenerators;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;

public interface PSBlocks {
    Block MASH_TUB = register("mash_tub", s -> new MashTubBlock(s
            .sounds(BlockSoundGroup.WOOD)
            .hardness(2).nonOpaque().suffocates(BlockConstructionUtils::never).blockVision(BlockConstructionUtils::never)
            .pistonBehavior(PistonBehavior.BLOCK)
    ));
    Block MASH_TUB_EDGE = register("mash_tub_edge", Settings.copy(MASH_TUB), MashTubWallBlock::new);
    Block PLACED_DRINK = register("placed_drink", s -> new PlacedDrinksBlock(s
            .breakInstantly().nonOpaque().suffocates(BlockConstructionUtils::never).blockVision(BlockConstructionUtils::never)
            .pistonBehavior(PistonBehavior.DESTROY)
    ));

    Block OAK_BARREL = register("oak_barrel", BlockConstructionUtils.barrel(MapColor.OAK_TAN));
    @Experimental
    Block PALE_OAK_BARREL = register("pale_oak_barrel", BlockConstructionUtils.barrel(MapColor.GRAY));
    Block SPRUCE_BARREL = register("spruce_barrel", BlockConstructionUtils.barrel(MapColor.SPRUCE_BROWN));
    Block BIRCH_BARREL = register("birch_barrel", BlockConstructionUtils.barrel(MapColor.PALE_YELLOW));
    Block JUNGLE_BARREL = register("jungle_barrel", BlockConstructionUtils.barrel(MapColor.DIRT_BROWN));
    Block ACACIA_BARREL = register("acacia_barrel", BlockConstructionUtils.barrel(MapColor.ORANGE));
    Block DARK_OAK_BARREL = register("dark_oak_barrel", BlockConstructionUtils.barrel(MapColor.BROWN));
    Block MANGROVE_BARREL = register("mangrove_barrel", BlockConstructionUtils.barrel(MapColor.DARK_GREEN));
    Block CHERRY_BARREL = register("cherry_barrel", BlockConstructionUtils.barrel(MapColor.DULL_PINK));
    Block BAMBOO_BARREL = register("bamboo_barrel", BlockConstructionUtils.barrel(MapColor.YELLOW));
    Block WARPED_BARREL = register("warped_barrel", BlockConstructionUtils.barrel(MapColor.BRIGHT_TEAL));
    Block CRIMSON_BARREL = register("crimson_barrel", BlockConstructionUtils.barrel(MapColor.BRIGHT_RED));
    Block JUNIPER_BARREL = register("juniper_barrel", BlockConstructionUtils.barrel(MapColor.LIGHT_BLUE_GRAY));

    List<Block> ALL_BARRELS = List.of(
            OAK_BARREL, SPRUCE_BARREL, BIRCH_BARREL, JUNGLE_BARREL, ACACIA_BARREL, DARK_OAK_BARREL,
            MANGROVE_BARREL, CHERRY_BARREL, BAMBOO_BARREL, WARPED_BARREL, CRIMSON_BARREL, JUNIPER_BARREL, PALE_OAK_BARREL
    );

    Block FLASK = register("flask", s -> new FlaskBlock(s.sounds(BlockSoundGroup.COPPER).hardness(1).pistonBehavior(PistonBehavior.BLOCK)));
    Block DISTILLERY = register("distillery", s -> new DistilleryBlock(s.sounds(BlockSoundGroup.COPPER).hardness(1).pistonBehavior(PistonBehavior.BLOCK)));
    Block BOTTLE_RACK = register("bottle_rack", s -> new BottleRackBlock(0, s.mapColor(MapColor.OAK_TAN).sounds(BlockSoundGroup.WOOD).hardness(0.5F).burnable()));
    Block WALL_BOTTLE_RACK = register("wall_bottle_rack", s -> new BottleRackBlock(-3, s.mapColor(MapColor.OAK_TAN).sounds(BlockSoundGroup.WOOD).hardness(0.5F).burnable()));

    Block DRYING_TABLE = register("drying_table", s -> new DryingTableBlock(s.mapColor(MapColor.OAK_TAN).solid().sounds(BlockSoundGroup.WOOD).hardness(2).burnable()));
    Block IRON_DRYING_TABLE = register("iron_drying_table", s -> new DryingTableBlock(s.mapColor(MapColor.IRON_GRAY).sounds(BlockSoundGroup.METAL).hardness(5)));

    JuniperLeavesBlock JUNIPER_LEAVES = register("juniper_leaves", BlockConstructionUtils.leaves(BlockSoundGroup.GRASS), JuniperLeavesBlock::new);
    JuniperLeavesBlock FRUITING_JUNIPER_LEAVES = register("fruiting_juniper_leaves", BlockConstructionUtils.leaves(BlockSoundGroup.GRASS), JuniperLeavesBlock::new);
    Block JUNIPER_LOG = register("juniper_log", BlockConstructionUtils.log(MapColor.CYAN, MapColor.LIGHT_BLUE_GRAY));
    Block JUNIPER_WOOD = register("juniper_wood", BlockConstructionUtils.log(MapColor.CYAN, MapColor.LIGHT_BLUE_GRAY));
    Block STRIPPED_JUNIPER_LOG = register("stripped_juniper_log", BlockConstructionUtils.log(MapColor.CYAN, MapColor.LIGHT_BLUE_GRAY));
    Block STRIPPED_JUNIPER_WOOD = register("stripped_juniper_wood", BlockConstructionUtils.log(MapColor.CYAN, MapColor.LIGHT_BLUE_GRAY));
    Block JUNIPER_SAPLING = register("juniper_sapling", BlockConstructionUtils.plant(BlockSoundGroup.GRASS), s -> new SaplingBlock(PSSaplingGenerators.JUNIPER, s));

    Block JUNIPER_PLANKS = register("juniper_planks", s -> new Block(s.mapColor(MapColor.LIGHT_BLUE_GRAY).instrument(NoteBlockInstrument.BASS).strength(2, 3).sounds(BlockSoundGroup.WOOD).burnable()));
    Block JUNIPER_STAIRS = register("juniper_stairs", Settings.copy(JUNIPER_PLANKS), s -> new StairsBlock(JUNIPER_PLANKS.getDefaultState(), s));
    Block JUNIPER_SIGN = register("juniper_sign", s -> new SignBlock(PSWoodTypes.JUNIPER, s.mapColor(JUNIPER_PLANKS.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1).burnable().sounds(BlockSoundGroup.WOOD)));
    Block JUNIPER_DOOR = register("juniper_door", s -> new DoorBlock(PSWoodTypes.JUNIPER.setType(), s.mapColor(JUNIPER_PLANKS.getDefaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(3.0f).nonOpaque().burnable().pistonBehavior(PistonBehavior.DESTROY)));
    Block JUNIPER_WALL_SIGN = register("juniper_wall_sign", s -> new WallSignBlock(PSWoodTypes.JUNIPER, s.mapColor(JUNIPER_PLANKS.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1).lootTable(JUNIPER_SIGN.getLootTableKey()).overrideTranslationKey(JUNIPER_SIGN.getTranslationKey()).burnable()));
    Block JUNIPER_HANGING_SIGN = register("juniper_hanging_sign", s -> new HangingSignBlock(PSWoodTypes.JUNIPER, s.mapColor(JUNIPER_LOG.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1).burnable()));
    Block JUNIPER_WALL_HANGING_SIGN = register("juniper_wall_hanging_sign", s -> new WallHangingSignBlock(PSWoodTypes.JUNIPER, s.mapColor(JUNIPER_LOG.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0f).burnable().lootTable(JUNIPER_HANGING_SIGN.getLootTableKey()).overrideTranslationKey(JUNIPER_HANGING_SIGN.getTranslationKey())));
    Block JUNIPER_PRESSURE_PLATE = register("juniper_pressure_plate", s -> new PressurePlateBlock(PSWoodTypes.JUNIPER.setType(), s.mapColor(JUNIPER_PLANKS.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(0.5F).burnable().pistonBehavior(PistonBehavior.DESTROY)));
    Block JUNIPER_FENCE = register("juniper_fence", s -> new FenceBlock(s.mapColor(JUNIPER_PLANKS.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).strength(2, 3).sounds(BlockSoundGroup.WOOD).burnable()));
    Block JUNIPER_TRAPDOOR = register("juniper_trapdoor", s -> new TrapdoorBlock(PSWoodTypes.JUNIPER.setType(), s.mapColor(JUNIPER_PLANKS.getDefaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(3).nonOpaque().allowsSpawning(BlockConstructionUtils::never).burnable()));
    Block JUNIPER_FENCE_GATE = register("juniper_fence_gate", s -> new FenceGateBlock(PSWoodTypes.JUNIPER, s.mapColor(JUNIPER_PLANKS.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).strength(2, 3).burnable()));
    Block JUNIPER_BUTTON = register("juniper_button", BlockConstructionUtils.woodenButton(PSWoodTypes.JUNIPER.setType()));
    Block JUNIPER_SLAB = register("juniper_slab", Settings.copy(JUNIPER_PLANKS), SlabBlock::new);

    CannabisPlantBlock CANNABIS = register("cannabis", BlockConstructionUtils.plant(BlockSoundGroup.GRASS), CannabisPlantBlock::new);
    HopPlantBlock HOP = register("hop", BlockConstructionUtils.plant(BlockSoundGroup.GRASS), HopPlantBlock::new);
    TobaccoPlantBlock TOBACCO = register("tobacco", BlockConstructionUtils.plant(BlockSoundGroup.GRASS), TobaccoPlantBlock::new);
    CocaPlantBlock COCA = register("coca", BlockConstructionUtils.plant(BlockSoundGroup.GRASS), CocaPlantBlock::new);
    CoffeaPlantBlock COFFEA = register("coffea", BlockConstructionUtils.plant(BlockSoundGroup.GRASS), CoffeaPlantBlock::new);
    PeyoteBlock PEYOTE = register("peyote", BlockConstructionUtils.plant(BlockSoundGroup.GRASS), PeyoteBlock::new);
    AgavePlantBlock AGAVE_PLANT = register("agave_plant", BlockConstructionUtils.plant(BlockSoundGroup.GRASS), AgavePlantBlock::new);
    NightshadeBlock JIMSONWEED = register("jimsonweed", BlockConstructionUtils.plant(BlockSoundGroup.GRASS), s -> new NightshadeBlock(
            () -> PSItems.JIMSONWEED_SEED_POD,
            () -> PSItems.JIMSONWEED_LEAF, s));
    NightshadeBlock BELLADONNA = register("belladonna", BlockConstructionUtils.plant(BlockSoundGroup.GRASS), s -> new NightshadeBlock(
            () -> PSItems.BELLADONNA_BERRIES,
            () -> PSItems.BELLADONNA_LEAF, s));
    NightshadeBlock TOMATOES = register("tomatoes", BlockConstructionUtils.plant(BlockSoundGroup.GRASS), s -> new NightshadeBlock(
            () -> PSItems.TOMATO,
            () -> PSItems.TOMATO_LEAF, s));

    Block LATTICE = register("lattice", s -> new LatticeBlock(s.mapColor(MapColor.OAK_TAN)
            .sounds(BlockSoundGroup.WOOD).hardness(0.3F).nonOpaque().burnable()));
    Block WINE_GRAPE_LATTICE = register("wine_grape_lattice", s -> new BurdenedLatticeBlock(true, null, 1, s.mapColor(MapColor.OAK_TAN)
            .sounds(BlockSoundGroup.WOOD).hardness(0.3F).ticksRandomly().nonOpaque().burnable()
    ));
    Block MORNING_GLORY = register("morning_glory", s -> new VineStemBlock(() -> PSBlocks.MORNING_GLORY_LATTICE, s
            .mapColor(MapColor.DARK_GREEN)
            .noCollision()
            .breakInstantly()
            .ticksRandomly()
            .sounds(BlockSoundGroup.GRASS)
            .offset(AbstractBlock.OffsetType.XZ)
            .burnable()
    ));
    Block MORNING_GLORY_LATTICE = register("morning_glory_lattice", s -> new BurdenedLatticeBlock(true, MORNING_GLORY, 2, s
            .mapColor(MapColor.OAK_TAN)
            .sounds(BlockSoundGroup.WOOD)
            .hardness(0.3F)
            .ticksRandomly()
            .nonOpaque()
            .burnable()
    ));

    Block POTTED_MORNING_GLORY = register("potted_morning_glory", BlockConstructionUtils.pottedPlant(MORNING_GLORY));
    Block POTTED_JUNIPER_SAPLING = register("potted_juniper_sapling", BlockConstructionUtils.pottedPlant(JUNIPER_SAPLING));
    Block POTTED_CANNABIS = register("potted_cannabis", BlockConstructionUtils.pottedPlant(CANNABIS));
    Block POTTED_HOP = register("potted_hop", BlockConstructionUtils.pottedPlant(HOP));
    Block POTTED_TOBACCO = register("potted_tobacco", BlockConstructionUtils.pottedPlant(TOBACCO));
    Block POTTED_COCA = register("potted_coca", BlockConstructionUtils.pottedPlant(COCA));
    Block POTTED_COFFEA = register("potted_coffea", BlockConstructionUtils.pottedPlant(COFFEA));

    Block RIFT_JAR = register("rift_jar", s -> new RiftJarBlock(s.hardness(0.5F).sounds(BlockSoundGroup.GLASS).nonOpaque().pistonBehavior(PistonBehavior.DESTROY)));
    Block GLITCH = register("glitch", s -> new GlitchedBlock(s.mapColor(MapColor.BLACK).breakInstantly().hardness(0)
            .emissiveLighting(BlockConstructionUtils::always)
            .air().nonOpaque().noBlockBreakParticles().dropsNothing()
    ));

    Block FLAMMABLE_GAS = register("flammable_gas", s -> new FlammableGasBlock(s.replaceable().noCollision().dropsNothing().air()));

    Block TRAY = register("tray", s -> new TrayBlock(s.mapColor(MapColor.IRON_GRAY).hardness(0.7F).sounds(BlockSoundGroup.METAL).nonOpaque()));
    Block BUNSEN_BURNER = register("bunsen_burner", s -> new BurnerBlock(s.mapColor(MapColor.IRON_GRAY).hardness(0.7F).sounds(BlockSoundGroup.METAL).nonOpaque()));
    Block GLASS_TUBE = register("glass_tube", s -> new GlassTubeBlock(s.mapColor(MapColor.OFF_WHITE).strength(0.3F).sounds(BlockSoundGroup.GLASS).nonOpaque()
            .allowsSpawning(Blocks::never)
            .solidBlock(Blocks::never)
            .suffocates(Blocks::never)
            .blockVision(Blocks::never)));
    Block GLASS_VALVE = register("glass_valve", s -> new ValveBlock(s.mapColor(MapColor.OFF_WHITE).strength(0.3F).sounds(BlockSoundGroup.GLASS).nonOpaque()
            .allowsSpawning(Blocks::never)
            .solidBlock(Blocks::never)
            .suffocates(Blocks::never)
            .blockVision(Blocks::never)));
    Block PUMP = register("pump", s -> new PumpBlock(s.mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(3.5F)));
    Block PUMP_HEAD = register("pump_head", Settings.copy(PUMP), s -> new PumpHeadBlock(s.nonOpaque().dropsNothing().pistonBehavior(PistonBehavior.BLOCK)));

    static <T extends Block> T register(String name, Function<AbstractBlock.Settings, T> blockFactory) {
        return register(name, Settings.create(), blockFactory);
    }

    static <T extends Block> T register(String name, AbstractBlock.Settings settings, Function<AbstractBlock.Settings, T> blockFactory) {
        var key = RegistryKey.of(RegistryKeys.BLOCK, Psychedelicraft.id(name));
        return Registry.register(Registries.BLOCK, key, blockFactory.apply(settings.registryKey(key)));
    }

    static void bootstrap() {
        PSBlockEntities.bootstrap();

        BlockEntityTypeSupportHelper.of(BlockEntityType.SIGN).addSupportedBlocks(JUNIPER_SIGN, JUNIPER_WALL_SIGN);
        BlockEntityTypeSupportHelper.of(BlockEntityType.HANGING_SIGN).addSupportedBlocks(JUNIPER_HANGING_SIGN, JUNIPER_WALL_HANGING_SIGN);

        FlammableBlockRegistry.getDefaultInstance().add(JUNIPER_LOG, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(STRIPPED_JUNIPER_LOG, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(JUNIPER_WOOD, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(STRIPPED_JUNIPER_WOOD, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(JUNIPER_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(LATTICE, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(WINE_GRAPE_LATTICE, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(MORNING_GLORY_LATTICE, 5, 20);

        StrippableBlockRegistry.register(JUNIPER_LOG, STRIPPED_JUNIPER_LOG);
        StrippableBlockRegistry.register(JUNIPER_WOOD, STRIPPED_JUNIPER_WOOD);
    }
}

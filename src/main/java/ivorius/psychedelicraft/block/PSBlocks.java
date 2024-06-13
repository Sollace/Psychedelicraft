/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

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
import net.minecraft.sound.BlockSoundGroup;

public interface PSBlocks {
    Block MASH_TUB = register("mash_tub", new MashTubBlock(Settings.create()
            .sounds(BlockSoundGroup.WOOD)
            .hardness(2).nonOpaque().suffocates(BlockConstructionUtils::never).blockVision(BlockConstructionUtils::never)
            .pistonBehavior(PistonBehavior.BLOCK)
    ));
    Block MASH_TUB_EDGE = register("mash_tub_edge", new MashTubWallBlock(Settings.copy(MASH_TUB)));
    Block PLACED_DRINK = register("placed_drink", new PlacedDrinksBlock(Settings.create()
            .breakInstantly().nonOpaque().suffocates(BlockConstructionUtils::never).blockVision(BlockConstructionUtils::never)
            .pistonBehavior(PistonBehavior.DESTROY)
    ));

    Block OAK_BARREL = register("oak_barrel", BlockConstructionUtils.barrel(MapColor.OAK_TAN));
    Block SPRUCE_BARREL = register("spruce_barrel", BlockConstructionUtils.barrel(MapColor.SPRUCE_BROWN));
    Block BIRCH_BARREL = register("birch_barrel", BlockConstructionUtils.barrel(MapColor.PALE_YELLOW));
    Block JUNGLE_BARREL = register("jungle_barrel", BlockConstructionUtils.barrel(MapColor.DIRT_BROWN));
    Block ACACIA_BARREL = register("acacia_barrel", BlockConstructionUtils.barrel(MapColor.ORANGE));
    Block DARK_OAK_BARREL = register("dark_oak_barrel", BlockConstructionUtils.barrel(MapColor.BROWN));

    Block FLASK = register("flask", new FlaskBlock(Settings.create().sounds(BlockSoundGroup.COPPER).hardness(1).pistonBehavior(PistonBehavior.BLOCK)));
    Block DISTILLERY = register("distillery", new DistilleryBlock(Settings.create().sounds(BlockSoundGroup.COPPER).hardness(1).pistonBehavior(PistonBehavior.BLOCK)));
    Block BOTTLE_RACK = register("bottle_rack", new BottleRackBlock(Settings.create().mapColor(MapColor.OAK_TAN).sounds(BlockSoundGroup.WOOD).hardness(0.5F).burnable()));

    Block DRYING_TABLE = register("drying_table", new DryingTableBlock(Settings.create().mapColor(MapColor.OAK_TAN).solid().sounds(BlockSoundGroup.WOOD).hardness(2).burnable()));
    Block IRON_DRYING_TABLE = register("iron_drying_table", new DryingTableBlock(Settings.create().mapColor(MapColor.IRON_GRAY).sounds(BlockSoundGroup.METAL).hardness(5)));

    JuniperLeavesBlock JUNIPER_LEAVES = register("juniper_leaves", new JuniperLeavesBlock(BlockConstructionUtils.leaves(BlockSoundGroup.GRASS)));
    JuniperLeavesBlock FRUITING_JUNIPER_LEAVES = register("fruiting_juniper_leaves", new JuniperLeavesBlock(BlockConstructionUtils.leaves(BlockSoundGroup.GRASS)));
    Block JUNIPER_LOG = register("juniper_log", BlockConstructionUtils.log(MapColor.CYAN, MapColor.LIGHT_BLUE_GRAY));
    Block JUNIPER_WOOD = register("juniper_wood", BlockConstructionUtils.log(MapColor.CYAN, MapColor.LIGHT_BLUE_GRAY));
    Block STRIPPED_JUNIPER_LOG = register("stripped_juniper_log", BlockConstructionUtils.log(MapColor.CYAN, MapColor.LIGHT_BLUE_GRAY));
    Block STRIPPED_JUNIPER_WOOD = register("stripped_juniper_wood", BlockConstructionUtils.log(MapColor.CYAN, MapColor.LIGHT_BLUE_GRAY));
    Block JUNIPER_SAPLING = register("juniper_sapling", new SaplingBlock(PSSaplingGenerators.JUNIPER, BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));

    Block JUNIPER_PLANKS = register("juniper_planks", new Block(Settings.create().mapColor(MapColor.LIGHT_BLUE_GRAY).instrument(NoteBlockInstrument.BASS).strength(2, 3).sounds(BlockSoundGroup.WOOD).burnable()));
    Block JUNIPER_STAIRS = register("juniper_stairs", new StairsBlock(JUNIPER_PLANKS.getDefaultState(), Settings.copy(JUNIPER_PLANKS)));
    Block JUNIPER_SIGN = register("juniper_sign", new SignBlock(PSWoodTypes.JUNIPER, Settings.create().mapColor(JUNIPER_PLANKS.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1).burnable().sounds(BlockSoundGroup.WOOD)));
    Block JUNIPER_DOOR = register("juniper_door", new DoorBlock(PSWoodTypes.JUNIPER.setType(), Settings.create().mapColor(JUNIPER_PLANKS.getDefaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(3.0f).nonOpaque().burnable().pistonBehavior(PistonBehavior.DESTROY)));
    Block JUNIPER_WALL_SIGN = register("juniper_wall_sign", new WallSignBlock(PSWoodTypes.JUNIPER, Settings.create().mapColor(JUNIPER_PLANKS.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1).dropsLike(JUNIPER_SIGN).burnable()));
    Block JUNIPER_HANGING_SIGN = register("juniper_hanging_sign", new HangingSignBlock(PSWoodTypes.JUNIPER, Settings.create().mapColor(JUNIPER_LOG.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1).burnable()));
    Block JUNIPER_WALL_HANGING_SIGN = register("juniper_wall_hanging_sign", new WallHangingSignBlock(PSWoodTypes.JUNIPER, Settings.create().mapColor(JUNIPER_LOG.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0f).burnable().dropsLike(JUNIPER_HANGING_SIGN)));
    Block JUNIPER_PRESSURE_PLATE = register("juniper_pressure_plate", new PressurePlateBlock(PSWoodTypes.JUNIPER.setType(), Settings.create().mapColor(JUNIPER_PLANKS.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(0.5F).burnable().pistonBehavior(PistonBehavior.DESTROY)));
    Block JUNIPER_FENCE = register("juniper_fence", new FenceBlock(Settings.create().mapColor(JUNIPER_PLANKS.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).strength(2, 3).sounds(BlockSoundGroup.WOOD).burnable()));
    Block JUNIPER_TRAPDOOR = register("juniper_trapdoor", new TrapdoorBlock(PSWoodTypes.JUNIPER.setType(), Settings.create().mapColor(JUNIPER_PLANKS.getDefaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(3).nonOpaque().allowsSpawning(BlockConstructionUtils::never).burnable()));
    Block JUNIPER_FENCE_GATE = register("juniper_fence_gate", new FenceGateBlock(PSWoodTypes.JUNIPER, Settings.create().mapColor(JUNIPER_PLANKS.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).strength(2, 3).burnable()));
    Block JUNIPER_BUTTON = register("juniper_button", BlockConstructionUtils.woodenButton(PSWoodTypes.JUNIPER.setType()));
    Block JUNIPER_SLAB = register("juniper_slab", new SlabBlock(Settings.copy(JUNIPER_PLANKS)));

    CannabisPlantBlock CANNABIS = register("cannabis", new CannabisPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    HopPlantBlock HOP = register("hop", new HopPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    TobaccoPlantBlock TOBACCO = register("tobacco", new TobaccoPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    CocaPlantBlock COCA = register("coca", new CocaPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    CoffeaPlantBlock COFFEA = register("coffea", new CoffeaPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    PeyoteBlock PEYOTE = register("peyote", new PeyoteBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    AgavePlantBlock AGAVE_PLANT = register("agave_plant", new AgavePlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    NightshadeBlock JIMSONWEEED = register("jimsonweed", new NightshadeBlock(
            () -> PSItems.JIMSONWEED_SEED_POD,
            () -> PSItems.JIMSONWEED_LEAF, BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    NightshadeBlock BELLADONNA = register("belladonna", new NightshadeBlock(
            () -> PSItems.BELLADONNA_BERRIES,
            () -> PSItems.BELLADONNA_LEAF, BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    NightshadeBlock TOMATOES = register("tomatoes", new NightshadeBlock(
            () -> PSItems.TOMATO,
            () -> PSItems.TOMATO_LEAF, BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));

    Block LATTICE = register("lattice", new LatticeBlock(Settings.create().mapColor(MapColor.OAK_TAN)
            .sounds(BlockSoundGroup.WOOD).hardness(0.3F).nonOpaque().burnable()));
    Block WINE_GRAPE_LATTICE = register("wine_grape_lattice", new BurdenedLatticeBlock(true, null, 1, Settings.create().mapColor(MapColor.OAK_TAN)
            .sounds(BlockSoundGroup.WOOD).hardness(0.3F).ticksRandomly().nonOpaque().burnable()
    ));
    Block MORNING_GLORY = register("morning_glory", new VineStemBlock(() -> PSBlocks.MORNING_GLORY_LATTICE, Block.Settings.create()
            .mapColor(MapColor.DARK_GREEN)
            .noCollision()
            .breakInstantly()
            .ticksRandomly()
            .sounds(BlockSoundGroup.GRASS)
            .offset(AbstractBlock.OffsetType.XZ)
            .burnable()
    ));
    Block MORNING_GLORY_LATTICE = register("morning_glory_lattice", new BurdenedLatticeBlock(true, MORNING_GLORY, 2, Block.Settings.create()
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

    Block RIFT_JAR = register("rift_jar", new RiftJarBlock(Settings.create().hardness(0.5F).sounds(BlockSoundGroup.GLASS).nonOpaque().pistonBehavior(PistonBehavior.DESTROY)));
    Block GLITCH = register("glitch", new GlitchedBlock(Settings.create().mapColor(MapColor.BLACK).breakInstantly().hardness(0)
            .emissiveLighting(BlockConstructionUtils::always)
            .air().nonOpaque().noBlockBreakParticles().dropsNothing()
    ));

    Block TRAY = register("tray", new TrayBlock(Settings.create().mapColor(MapColor.IRON_GRAY).hardness(0.7F).sounds(BlockSoundGroup.METAL).nonOpaque()));
    Block BUNSEN_BURNER = register("bunsen_burner", new BurnerBlock(Settings.create().mapColor(MapColor.IRON_GRAY).hardness(0.7F).sounds(BlockSoundGroup.METAL).nonOpaque()));

    static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, Psychedelicraft.id(name), block);
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
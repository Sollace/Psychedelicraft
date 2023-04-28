/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.BlockEntityTypeSupportHelper;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.mixin.SignTypeAccessor;
import ivorius.psychedelicraft.world.gen.JuniperTreeSaplingGenerator;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.SignType;

public interface PSBlocks {
    Block MASH_TUB = register("mash_tub", new MashTubBlock(Settings.of(Material.WOOD)
            .sounds(BlockSoundGroup.WOOD)
            .hardness(2).nonOpaque().suffocates(BlockConstructionUtils::never).blockVision(BlockConstructionUtils::never)));
    Block MASH_TUB_EDGE = register("mash_tub_edge", new MashTubWallBlock(Settings.copy(MASH_TUB)));

    Block OAK_BARREL = register("oak_barrel", new BarrelBlock(BlockConstructionUtils.barrel()));
    Block SPRUCE_BARREL = register("spruce_barrel", new BarrelBlock(BlockConstructionUtils.barrel()));
    Block BIRCH_BARREL = register("birch_barrel", new BarrelBlock(BlockConstructionUtils.barrel()));
    Block JUNGLE_BARREL = register("jungle_barrel", new BarrelBlock(BlockConstructionUtils.barrel()));
    Block ACACIA_BARREL = register("acacia_barrel", new BarrelBlock(BlockConstructionUtils.barrel()));
    Block DARK_OAK_BARREL = register("dark_oak_barrel", new BarrelBlock(BlockConstructionUtils.barrel()));

    Block FLASK = register("flask", new FlaskBlock(Settings.of(Material.GLASS).sounds(BlockSoundGroup.WOOD).hardness(1)));
    Block DISTILLERY = register("distillery", new DistilleryBlock(Settings.of(Material.GLASS).sounds(BlockSoundGroup.GLASS).hardness(1)));
    Block BOTTLE_RACK = register("bottle_rack", new BottleRackBlock(Settings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(0.5F)));

    Block DRYING_TABLE = register("drying_table", new DryingTableBlock(Settings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(2)));
    Block IRON_DRYING_TABLE = register("iron_drying_table", new DryingTableBlock(Settings.of(Material.METAL).sounds(BlockSoundGroup.METAL).hardness(5)));

    JuniperLeavesBlock JUNIPER_LEAVES = register("juniper_leaves", new JuniperLeavesBlock(BlockConstructionUtils.leaves(BlockSoundGroup.GRASS)));
    JuniperLeavesBlock FRUITING_JUNIPER_LEAVES = register("fruiting_juniper_leaves", new JuniperLeavesBlock(BlockConstructionUtils.leaves(BlockSoundGroup.GRASS)));
    Block JUNIPER_LOG = register("juniper_log", BlockConstructionUtils.log(MapColor.CYAN, MapColor.LIGHT_BLUE_GRAY));
    Block JUNIPER_WOOD = register("juniper_wood", BlockConstructionUtils.log(MapColor.CYAN, MapColor.LIGHT_BLUE_GRAY));
    Block STRIPPED_JUNIPER_LOG = register("stripped_juniper_log", BlockConstructionUtils.log(MapColor.CYAN, MapColor.LIGHT_BLUE_GRAY));
    Block STRIPPED_JUNIPER_WOOD = register("stripped_juniper_wood", BlockConstructionUtils.log(MapColor.CYAN, MapColor.LIGHT_BLUE_GRAY));
    Block JUNIPER_SAPLING = register("juniper_sapling", new SaplingBlock(new JuniperTreeSaplingGenerator(), BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));

    SignType JUNIPER_SIGN_TYPE = SignTypeAccessor.callRegister(new SignType("juniper") {});

    Block JUNIPER_PLANKS = register("juniper_planks", new Block(Settings.of(Material.WOOD, MapColor.LIGHT_BLUE_GRAY).strength(2, 3).sounds(BlockSoundGroup.WOOD)));
    Block JUNIPER_STAIRS = register("juniper_stairs", new StairsBlock(JUNIPER_PLANKS.getDefaultState(), Settings.copy(JUNIPER_PLANKS)));
    Block JUNIPER_SIGN = register("juniper_sign", new SignBlock(Settings.of(Material.WOOD).noCollision().strength(1.0f).sounds(BlockSoundGroup.WOOD), JUNIPER_SIGN_TYPE));
    Block JUNIPER_DOOR = register("juniper_door", new DoorBlock(Settings.of(Material.WOOD, JUNIPER_PLANKS.getDefaultMapColor()).strength(3.0f).sounds(BlockSoundGroup.WOOD).nonOpaque(), SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, SoundEvents.BLOCK_WOODEN_DOOR_OPEN));
    Block JUNIPER_WALL_SIGN = register("juniper_wall_sign", new WallSignBlock(Settings.of(Material.WOOD).noCollision().strength(1.0f).sounds(BlockSoundGroup.WOOD).dropsLike(JUNIPER_SIGN), JUNIPER_SIGN_TYPE));
    Block JUNIPER_HANGING_SIGN = register("juniper_hanging_sign", new HangingSignBlock(AbstractBlock.Settings.of(Material.WOOD, JUNIPER_LOG.getDefaultMapColor()).noCollision().strength(1.0f).sounds(BlockSoundGroup.HANGING_SIGN).requires(FeatureFlags.UPDATE_1_20), JUNIPER_SIGN_TYPE));
    Block JUNIPER_WALL_HANGING_SIGN = register("juniper_wall_hanging_sign", new WallHangingSignBlock(AbstractBlock.Settings.of(Material.WOOD, JUNIPER_LOG.getDefaultMapColor()).noCollision().strength(1.0f).sounds(BlockSoundGroup.HANGING_SIGN).requires(FeatureFlags.UPDATE_1_20).dropsLike(JUNIPER_HANGING_SIGN), JUNIPER_SIGN_TYPE));
    Block JUNIPER_PRESSURE_PLATE = register("juniper_pressure_plate", new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, Settings.of(Material.WOOD, JUNIPER_PLANKS.getDefaultMapColor()).noCollision().strength(0.5f).sounds(BlockSoundGroup.WOOD), SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON));
    Block JUNIPER_FENCE = register("juniper_fence", new FenceBlock(Settings.of(Material.WOOD, JUNIPER_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD)));
    Block JUNIPER_TRAPDOOR = register("juniper_trapdoor", new TrapdoorBlock(Settings.of(Material.WOOD, JUNIPER_PLANKS.getDefaultMapColor()).strength(3.0f).sounds(BlockSoundGroup.WOOD).nonOpaque().allowsSpawning(BlockConstructionUtils::never), SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN));
    Block JUNIPER_FENCE_GATE = register("juniper_fence_gate", new FenceGateBlock(Settings.of(Material.WOOD, JUNIPER_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD), SoundEvents.BLOCK_FENCE_GATE_CLOSE, SoundEvents.BLOCK_FENCE_GATE_OPEN));
    Block JUNIPER_BUTTON = register("juniper_button", BlockConstructionUtils.woodenButton());
    Block JUNIPER_SLAB = register("juniper_slab", new SlabBlock(Settings.of(Material.WOOD, JUNIPER_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD)));

    CannabisPlantBlock CANNABIS = register("cannabis", new CannabisPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    HopPlantBlock HOP = register("hop", new HopPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    TobaccoPlantBlock TOBACCO = register("tobacco", new TobaccoPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    CocaPlantBlock COCA = register("coca", new CocaPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    CoffeaPlantBlock COFFEA = register("coffea", new CoffeaPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    PeyoteBlock PEYOTE = register("peyote", new PeyoteBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    NightshadeBlock JIMSONWEEED = register("jimsonweed", new NightshadeBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS),
            () -> PSItems.JIMSONWEED_SEED_POD,
            () -> PSItems.JIMSONWEED_LEAF
    ));
    NightshadeBlock BELLADONNA = register("belladonna", new NightshadeBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS),
            () -> PSItems.BELLADONNA_BERRIES,
            () -> PSItems.BELLADONNA_LEAF
    ));

    Block LATTICE = register("lattice", new LatticeBlock(Block.Settings.of(Material.WOOD)
            .sounds(BlockSoundGroup.WOOD).hardness(0.3F).nonOpaque()));
    Block WINE_GRAPE_LATTICE = register("wine_grape_lattice", new BurdenedLatticeBlock(true, null, 1, Block.Settings.of(Material.WOOD)
            .sounds(BlockSoundGroup.WOOD).hardness(0.3F).ticksRandomly().nonOpaque()
    ));
    Block MORNING_GLORY = register("morning_glory", new VineStemBlock(() -> PSBlocks.MORNING_GLORY_LATTICE, Block.Settings.of(Material.PLANT)
            .noCollision()
            .breakInstantly()
            .ticksRandomly()
            .sounds(BlockSoundGroup.GRASS)
            .offsetType(AbstractBlock.OffsetType.XZ)
    ));
    Block MORNING_GLORY_LATTICE = register("morning_glory_lattice", new BurdenedLatticeBlock(true, MORNING_GLORY, 2, Block.Settings.of(Material.WOOD)
            .sounds(BlockSoundGroup.WOOD).hardness(0.3F).ticksRandomly().nonOpaque()
    ));

    Block POTTED_MORNING_GLORY = register("potted_morning_glory", new FlowerPotBlock(MORNING_GLORY, BlockConstructionUtils.pottedPlant()));
    Block POTTED_JUNIPER_SAPLING = register("potted_juniper_sapling", new FlowerPotBlock(JUNIPER_SAPLING, BlockConstructionUtils.pottedPlant()));
    Block POTTED_CANNABIS = register("potted_cannabis", new FlowerPotBlock(CANNABIS, BlockConstructionUtils.pottedPlant()));
    Block POTTED_HOP = register("potted_hop", new FlowerPotBlock(HOP, BlockConstructionUtils.pottedPlant()));
    Block POTTED_TOBACCO = register("potted_tobacco", new FlowerPotBlock(TOBACCO, BlockConstructionUtils.pottedPlant()));
    Block POTTED_COCA = register("potted_coca", new FlowerPotBlock(COCA, BlockConstructionUtils.pottedPlant()));
    Block POTTED_COFFEA = register("potted_coffea", new FlowerPotBlock(COFFEA, BlockConstructionUtils.pottedPlant()));

    Block RIFT_JAR = register("rift_jar", new RiftJarBlock(Settings.of(Material.ORGANIC_PRODUCT).hardness(0.5F).sounds(BlockSoundGroup.GLASS).nonOpaque()));
    Block GLITCH = register("glitch", new GlitchedBlock(Settings.of(Material.DECORATION).breakInstantly().hardness(0)
            .emissiveLighting(BlockConstructionUtils::always)
            .air().nonOpaque().noBlockBreakParticles().dropsNothing()
    ));

    Block TRAY = register("tray", new TrayBlock(Settings.of(Material.METAL).hardness(0.7F).sounds(BlockSoundGroup.METAL).nonOpaque()));
    Block BUNSEN_BURNER = register("bunsen_burner", new BurnerBlock(Settings.of(Material.METAL).hardness(0.7F).sounds(BlockSoundGroup.METAL).nonOpaque()));

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
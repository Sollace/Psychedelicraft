/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.worldgen.JuniperTreeSaplingGenerator;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public interface PSBlocks {
    Block MASH_TUB = register("mash_tub", new MashTubBlock(Settings.of(Material.WOOD)
            .sounds(BlockSoundGroup.WOOD)
            .hardness(2).nonOpaque().suffocates(BlockConstructionUtils::never).blockVision(BlockConstructionUtils::never)));

    Block OAK_BARREL = register("oak_barrel", new BarrelBlock(Settings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(2)));
    Block SPRUCE_BARREL = register("spruce_barrel", new BarrelBlock(Settings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(2)));
    Block BIRCH_BARREL = register("birch_barrel", new BarrelBlock(Settings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(2)));
    Block JUNGLE_BARREL = register("jungle_barrel", new BarrelBlock(Settings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(2)));
    Block ACACIA_BARREL = register("acacia_barrel", new BarrelBlock(Settings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(2)));
    Block DARK_OAK_BARREL = register("dark_oak_barrel", new BarrelBlock(Settings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(2)));

    Block FLASK = register("flask", new FlaskBlock(Settings.of(Material.GLASS).sounds(BlockSoundGroup.WOOD).hardness(1)));
    Block DISTILLERY = register("distillery", new DistilleryBlock(Settings.of(Material.GLASS).sounds(BlockSoundGroup.GLASS).hardness(1)));
    Block BOTTLE_RACK = register("bottle_rack", new BottleRackBlock(Settings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(0.5F)));

    Block DRYING_TABLE = register("drying_table", new DryingTableBlock(Settings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(2)));
    Block IRON_DRYING_TABLE = register("iron_drying_table", new DryingTableBlock(Settings.of(Material.METAL).sounds(BlockSoundGroup.METAL).hardness(5)));

    JuniperLeavesBlock JUNIPER_LEAVES = register("juniper_leaves", new JuniperLeavesBlock(BlockConstructionUtils.leaves(BlockSoundGroup.GRASS)));
    JuniperLeavesBlock FRUITING_JUNIPER_LEAVES = register("fruiting_juniper_leaves", new JuniperLeavesBlock(BlockConstructionUtils.leaves(BlockSoundGroup.GRASS)));
    Block JUNIPER_LOG = register("juniper_log", BlockConstructionUtils.log(MapColor.CYAN, MapColor.BLUE));
    Block JUNIPER_WOOD = register("juniper_wood", BlockConstructionUtils.log(MapColor.CYAN, MapColor.BLUE));
    Block STRIPPED_JUNIPER_LOG = register("stripped_juniper_log", BlockConstructionUtils.log(MapColor.CYAN, MapColor.BLUE));
    Block STRIPPED_JUNIPER_WOOD = register("stripped_juniper_wood", BlockConstructionUtils.log(MapColor.CYAN, MapColor.BLUE));
    Block JUNIPER_SAPLING = register("juniper_sapling", new SaplingBlock(new JuniperTreeSaplingGenerator(), BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));

    Block CANNABIS = register("cannabis", new CannabisPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    Block HOP = register("hop", new HopPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    Block TOBACCO = register("tobacco", new TobaccoPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    Block COCA = register("coca", new CocaPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    Block COFFEA = register("coffea", new CoffeaPlantBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    Block PEYOTE = register("peyote", new PeyoteBlock(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));

    Block LATTICE = register("lattice", new LatticeBlock(Block.Settings.of(Material.WOOD)
            .sounds(BlockSoundGroup.WOOD).hardness(0.3F).nonOpaque()));
    Block WINE_GRAPE_LATTICE = register("wine_grape_lattice", new WineGrapeLatticeBlock(Block.Settings.of(Material.WOOD)
            .sounds(BlockSoundGroup.WOOD).hardness(0.3F).ticksRandomly().nonOpaque()
    ));

    Block RIFT_JAR = register("rift_jar", new RiftJarBlock(Settings.of(Material.ORGANIC_PRODUCT).hardness(0.5F).sounds(BlockSoundGroup.GLASS).nonOpaque()));
    Block GLITCH = register("glitch", new GlitchedBlock(Settings.of(Material.DECORATION).breakInstantly().hardness(0)
            .emissiveLighting(BlockConstructionUtils::always)
            .air().nonOpaque().noBlockBreakParticles().dropsNothing()
    ));

    static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, Psychedelicraft.id(name), block);
    }

    static void bootstrap() {
        PSBlockEntities.bootstrap();

        FlammableBlockRegistry.getDefaultInstance().add(JUNIPER_LOG, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(STRIPPED_JUNIPER_LOG, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(JUNIPER_WOOD, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(STRIPPED_JUNIPER_WOOD, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(JUNIPER_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(LATTICE, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(WINE_GRAPE_LATTICE, 5, 20);

        StrippableBlockRegistry.register(JUNIPER_LOG, STRIPPED_JUNIPER_LOG);
        StrippableBlockRegistry.register(JUNIPER_WOOD, STRIPPED_JUNIPER_WOOD);
    }
}
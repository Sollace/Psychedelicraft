/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.worldgen.JuniperTreeSaplingGenerator;
import net.minecraft.block.*;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public interface PSBlocks {
    Block MASH_TUB = register("mash_tub", new BlockMashTub(Settings.of(Material.WOOD).hardness(2)));

    Block OAK_BARREL = register("oak_barrel", new BarrelBlock(Settings.of(Material.WOOD).hardness(2)));
    Block SPRUCE_BARREL = register("spruce_barrel", new BarrelBlock(Settings.of(Material.WOOD).hardness(2)));
    Block BIRCH_BARREL = register("birch_barrel", new BarrelBlock(Settings.of(Material.WOOD).hardness(2)));
    Block JUNGLE_BARREL = register("jungle_barrel", new BarrelBlock(Settings.of(Material.WOOD).hardness(2)));
    Block ACACIA_BARREL = register("acacia_barrel", new BarrelBlock(Settings.of(Material.WOOD).hardness(2)));
    Block DARK_OAK_BARREL = register("dark_oak_barrel", new BarrelBlock(Settings.of(Material.WOOD).hardness(2)));

    Block FLASK = register("flask", new BlockFlask(Settings.of(Material.GLASS).hardness(1)));
    Block DISTILLERY = register("distillery", new BlockDistillery(Settings.of(Material.GLASS).hardness(1)));
    Block BOTTLE_RACK = register("bottle_rack", new BlockBottleRack(Settings.of(Material.WOOD).hardness(0.5F)));

    Block DRYING_TABLE = register("drying_table", new BlockDryingTable(Settings.of(Material.WOOD).hardness(2)));
    Block IRON_DRYING_TABLE = register("iron_drying_table", new BlockDryingTable(Settings.of(Material.METAL).hardness(5)));

    JuniperLeavesBlock JUNIPER_LEAVES = register("juniper_leaves", new JuniperLeavesBlock(BlockConstructionUtils.leaves(BlockSoundGroup.GRASS)));
    JuniperLeavesBlock JUNIPER_BERRIES = register("juniper_berries", new JuniperLeavesBlock(BlockConstructionUtils.leaves(BlockSoundGroup.GRASS)));
    Block JUNIPER_LOG = register("juniper_log", BlockConstructionUtils.log(MapColor.CYAN, MapColor.BLUE));
    Block JUNIPER_SAPLING = register("juniper_sapling", new SaplingBlock(new JuniperTreeSaplingGenerator(), BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));

    Block CANNABIS = register("cannabis", new BlockCannabisPlant(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    Block HOP = register("hop", new BlockHopPlant(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    Block TOBACCO = register("tobacco", new BlockTobaccoPlant(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    Block COCA = register("coca", new BlockCocaPlant(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    Block COFFEA = register("coffea", new BlockCoffea(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    Block PEYOTE = register("peyote", new BlockPeyote(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));

    Block LATTICE = register("lattice", new LatticeBlock(Block.Settings.of(Material.WOOD).hardness(0.3F).nonOpaque()));
    Block WINE_GRAPE_LATTICE = register("wine_grape_lattice", new BlockWineGrapeLattice(Block.Settings.of(Material.WOOD)
            .hardness(0.3F).ticksRandomly().nonOpaque()
    ));

    Block RIFT_JAR = register("rift_jar", new BlockRiftJar(Settings.of(Material.ORGANIC_PRODUCT).hardness(0.5F).sounds(BlockSoundGroup.STONE).nonOpaque()));
    Block GLITCH = register("glitch", new BlockGlitched(Settings.of(Material.DECORATION).breakInstantly().hardness(0)
            .emissiveLighting(BlockConstructionUtils::always)
            .air().nonOpaque().noBlockBreakParticles().dropsNothing()
    ));

    static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, Psychedelicraft.id(name), block);
    }

    static void bootstrap() {
        PSBlockEntities.bootstrap();
    }
}
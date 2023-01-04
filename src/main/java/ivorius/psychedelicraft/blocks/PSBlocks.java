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

public class PSBlocks {
    public static Block mashTub = register("mash_tub", new BlockMashTub(Settings.of(Material.WOOD).hardness(2)));

    public static Block oak_barrel = register("oak_barrel", new BarrelBlock(Settings.of(Material.WOOD).hardness(2)));
    public static Block spruce_barrel = register("spruce_barrel", new BarrelBlock(Settings.of(Material.WOOD).hardness(2)));
    public static Block birch_barrel = register("birch_barrel", new BarrelBlock(Settings.of(Material.WOOD).hardness(2)));
    public static Block jungle_barrel = register("jungle_barrel", new BarrelBlock(Settings.of(Material.WOOD).hardness(2)));
    public static Block acacia_barrel = register("acacia_barrel", new BarrelBlock(Settings.of(Material.WOOD).hardness(2)));
    public static Block dark_oak_barrel = register("dark_oak_barrel", new BarrelBlock(Settings.of(Material.WOOD).hardness(2)));

    public static Block flask = register("flask", new BlockFlask(Settings.of(Material.GLASS).hardness(1)));
    public static Block distillery = register("distillery", new BlockDistillery(Settings.of(Material.GLASS).hardness(1)));
    public static Block bottleRack = register("bottle_rack", new BlockBottleRack(Settings.of(Material.WOOD).hardness(0.5F)));

    public static Block dryingTable = register("oak_drying_table", new BlockDryingTable(Settings.of(Material.WOOD).hardness(2)));
    public static Block spruceDryingTable = register("spruce_drying_table", new BlockDryingTable(Settings.of(Material.WOOD).hardness(2)));
    public static Block birchDryingTable = register("birch_drying_table", new BlockDryingTable(Settings.of(Material.WOOD).hardness(2)));
    public static Block jungleDryingTable = register("jungle_drying_table", new BlockDryingTable(Settings.of(Material.WOOD).hardness(2)));
    public static Block acaciaDryingTable = register("acacia_drying_table", new BlockDryingTable(Settings.of(Material.WOOD).hardness(2)));
    public static Block darkOakDryingTable = register("dark_oak_drying_table", new BlockDryingTable(Settings.of(Material.WOOD).hardness(2)));
    public static Block dryingTableIron = register("iron_drying_table", new BlockDryingTable(Settings.of(Material.METAL).hardness(5)));

    public static JuniperLeavesBlock juniper_leaves = register("juniper_leaves", new JuniperLeavesBlock(BlockConstructionUtils.leaves(BlockSoundGroup.GRASS)));
    public static JuniperLeavesBlock juniper_berries = register("juniper_berries", new JuniperLeavesBlock(BlockConstructionUtils.leaves(BlockSoundGroup.GRASS)));
    public static JuniperLeavesBlock psycheLeaves = register("psyche_leaves", new JuniperLeavesBlock(BlockConstructionUtils.leaves(BlockSoundGroup.GRASS)));
    public static Block psycheLog = register("juniper_log", BlockConstructionUtils.log(MapColor.CYAN, MapColor.BLUE));
    public static Block psycheSapling = register("juniper_sapling", new SaplingBlock(new JuniperTreeSaplingGenerator(), BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));

    public static Block cannabisPlant = register("cannabis_plant", new BlockCannabisPlant(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    public static Block hopPlant = register("hop_plant", new BlockHopPlant(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    public static Block tobaccoPlant = register("tobacco_plant", new BlockTobaccoPlant(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    public static Block cocaPlant = register("coca_plant", new BlockCocaPlant(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    public static Block coffea = register("coffea_plant", new BlockCoffea(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));
    public static Block peyote = register("peyote_plant", new BlockPeyote(BlockConstructionUtils.plant(BlockSoundGroup.GRASS)));

    public static Block emptyLattice = register("empty_lattice", new LatticeBlock(Block.Settings.of(Material.WOOD).hardness(0.3F).nonOpaque()));
    public static Block wineGrapeLattice = register("wine_grape_lattice", new BlockWineGrapeLattice(Block.Settings.of(Material.WOOD)
            .hardness(0.3F).ticksRandomly().nonOpaque()
    ));

    public static Block riftJar = register("rift_jar", new BlockRiftJar(Settings.of(Material.ORGANIC_PRODUCT).hardness(0.5F).sounds(BlockSoundGroup.STONE).nonOpaque()));
    public static Block glitched = register("glitch", new BlockGlitched(Settings.of(Material.DECORATION).breakInstantly().hardness(0)
            .emissiveLighting(BlockConstructionUtils::always)
            .air().nonOpaque().noBlockBreakParticles().dropsNothing()
    ));

    static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, Psychedelicraft.id(name), block);
    }

    public static void bootstrap() {
        PSBlockEntities.bootstrap();
    }
}
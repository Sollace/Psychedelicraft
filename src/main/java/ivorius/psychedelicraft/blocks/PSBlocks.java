/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import net.minecraft.block.*;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public class PSBlocks {
    public static Block emptyLattice = register("empty_lattice", new Block(
            Block.Settings.of(Material.WOOD).hardness(0.3F)
    ));
    public static Block wineGrapeLattice = register("wine_grape_lattice", new BlockWineGrapeLattice(
            Block.Settings.of(Material.WOOD).hardness(0.3F).ticksRandomly().nonOpaque()
    ));

    public static Block mashTub = register("mash_tub", new BlockMashTub(Settings.of(Material.WOOD).hardness(2)));

    public static Block oak_barrel = register("oak_barrel", new BlockBarrel(Settings.of(Material.WOOD).hardness(2)));
    public static Block spruce_barrel = register("spruce_barrel", new BlockBarrel(Settings.of(Material.WOOD).hardness(2)));
    public static Block birch_barrel = register("birch_barrel", new BlockBarrel(Settings.of(Material.WOOD).hardness(2)));
    public static Block jungle_barrel = register("jungle_barrel", new BlockBarrel(Settings.of(Material.WOOD).hardness(2)));
    public static Block acacia_barrel = register("acacia_barrel", new BlockBarrel(Settings.of(Material.WOOD).hardness(2)));
    public static Block dark_oak_barrel = register("dark_oak_barrel", new BlockBarrel(Settings.of(Material.WOOD).hardness(2)));

    public static Block flask = register("flask", new BlockFlask(Settings.of(Material.GLASS).hardness(1)));
    public static Block distillery = register("distillery", new BlockDistillery(Settings.of(Material.GLASS).hardness(1)));

    public static Block dryingTable = register("oak_drying_table", new BlockDryingTable(Settings.of(Material.WOOD).hardness(2)));
    public static Block spruceDryingTable = register("spruce_drying_table", new BlockDryingTable(Settings.of(Material.WOOD).hardness(2)));
    public static Block birchDryingTable = register("birch_drying_table", new BlockDryingTable(Settings.of(Material.WOOD).hardness(2)));
    public static Block jungleDryingTable = register("jungle_drying_table", new BlockDryingTable(Settings.of(Material.WOOD).hardness(2)));
    public static Block acaciaDryingTable = register("acacia_drying_table", new BlockDryingTable(Settings.of(Material.WOOD).hardness(2)));
    public static Block darkOakDryingTable = register("dark_oak_drying_table", new BlockDryingTable(Settings.of(Material.WOOD).hardness(2)));
    public static Block dryingTableIron = register("iron_drying_table", new BlockDryingTable(Settings.of(Material.METAL).hardness(5)));

    public static Block cannabisPlant = register("cannabis_plant", new BlockCannabisPlant().setHardness(0.5f));
    public static Block hopPlant = register("hop_plant", new BlockHopPlant().setHardness(0.5f));

    public static Block tobaccoPlant = register("tobacco_plant", new BlockTobaccoPlant().setHardness(0.5f));

    public static Block cocaPlant = register("coca_plant", new BlockCocaPlant().setHardness(0.5f));

    public static BlockPsycheLeaves psycheLeaves = register("psyche_leaves", new BlockPsycheLeaves());
    public static Block psycheLog = register("psyche_log", new BlockPsycheLog().setHardness(1.0F));
    public static Block psycheSapling = register("psyche_sapling", new BlockPsycheSapling().setHardness(1.0F));

    public static Block coffea = register("coffea", new BlockCoffea().setHardness(0.5f));

    public static Block peyote = register("peyote", new BlockPeyote().setHardness(0.5f));
    public static Block riftJar = register("rift_jar", new BlockRiftJar().setHardness(0.5f).setBlockName("riftJar"));
    public static Block glitched = register("glitched", new BlockGlitched().setBlockName("glitched"));

    public static Block bottleRack = register("bottle_rack", new BlockBottleRack(Settings.of(Material.WOOD).hardness(0.5F)));

    static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, Psychedelicraft.id(name), block);
    }

    public static void bootstrap() {
        dryingTableIron.setCreativeTab(Psychedelicraft.creativeTab);
        psycheLeaves.setCreativeTab(Psychedelicraft.creativeTab);
        psycheLog.setCreativeTab(Psychedelicraft.creativeTab);
        psycheSapling.setCreativeTab(Psychedelicraft.creativeTab);
        bottleRack.setCreativeTab(creativeTab);
        wineGrapeLattice.setCreativeTab(Psychedelicraft.creativeTab);
        dryingTable.setCreativeTab(Psychedelicraft.creativeTab);

        PSBlockEntities.bootstrap();
    }
}
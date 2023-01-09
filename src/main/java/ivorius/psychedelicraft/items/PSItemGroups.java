/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.config.PSConfig;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSItemGroups {
    ItemGroup creativeTab = FabricItemGroup.builder(Psychedelicraft.id("general"))
            .icon(PSItems.CANNABIS_LEAF::getDefaultStack)
            .entries((features, entries, search) -> {
                entries.add(PSItems.TOBACCO_SEEDS);
                entries.add(PSItems.TOBACCO_LEAVES);
                entries.add(PSItems.DRIED_TOBACCO);
                entries.add(PSItems.CIGARETTE);
                entries.add(PSItems.CIGAR);
                entries.add(PSItems.JOINT);
                entries.add(PSItems.COCA_SEEDS);
                entries.add(PSItems.COCA_LEAVES);
                entries.add(PSItems.DRIED_COCA_LEAVES);
                entries.add(PSItems.COCAINE_POWDER);
                entries.add(PSItems.SYRINGE);
                entries.add(PSItems.WINE_GRAPES);
                entries.add(PSItems.HOP_CONES);
                entries.add(PSItems.HOP_SEEDS);
                entries.add(PSItems.CANNABIS_SEEDS);
                entries.add(PSItems.CANNABIS_LEAF);
                entries.add(PSItems.CANNABIS_BUDS);
                entries.add(PSItems.DRIED_CANNABIS_LEAF);
                entries.add(PSItems.SMOKING_PIPE);
                entries.add(PSItems.BONG);
                entries.add(PSItems.HASH_MUFFIN);
                entries.add(PSItems.BROWN_MAGIC_MUSHROOMS);
                entries.add(PSItems.RED_MAGIC_MUSHROOMS);
                entries.add(PSItems.JUNIPER_BERRIES);
                entries.add(PSItems.COFFEA_CHERRIES);
                entries.add(PSItems.COFFEE_BEANS);
                entries.add(PSItems.PEYOTE);
                entries.add(PSItems.DRIED_PEYOTE);
                entries.add(PSItems.PEYOTE_JOINT);
                entries.add(PSItems.FLASK);
                entries.add(PSItems.DISTILLERY);
                if (PSConfig.getInstance().balancing.enableHarmonium) {
                    entries.add(PSItems.HARMONIUM);
                }
                if (PSConfig.getInstance().balancing.enableRiftJars) {
                    entries.add(ItemRiftJar.createFilledRiftJar(0.0F, PSItems.RIFT_JAR));
                    entries.add(ItemRiftJar.createFilledRiftJar(0.25F, PSItems.RIFT_JAR));
                    entries.add(ItemRiftJar.createFilledRiftJar(0.55F, PSItems.RIFT_JAR));
                    entries.add(ItemRiftJar.createFilledRiftJar(0.75F, PSItems.RIFT_JAR));
                    entries.add(ItemRiftJar.createFilledRiftJar(0.9F, PSItems.RIFT_JAR));
                }
            })
            .build();
    ItemGroup drinksTab = FabricItemGroup.builder(Psychedelicraft.id("drinks"))
            .icon(PSItems.OAK_BARREL::getDefaultStack)
            .entries((features, entries, search) -> {
                entries.add(PSItems.STONE_CUP);
                entries.add(PSItems.WOODEN_MUG);
                entries.add(PSItems.GLASS_CHALICE);
                entries.add(PSItems.bottle);
                entries.add(PSItems.MASH_TUB);
            })
            .build();
    ItemGroup weaponsTab = FabricItemGroup.builder(Psychedelicraft.id("weapons"))
            .icon(PSItems.molotovCocktail::getDefaultStack)
            .entries((features, entries, search) -> {
                entries.add(PSItems.molotovCocktail);
            })
            .build();

    static void bootstrap() {
       /* dryingTableIron.setCreativeTab(Psychedelicraft.creativeTab);
        psycheLeaves.setCreativeTab(Psychedelicraft.creativeTab);
        psycheLog.setCreativeTab(Psychedelicraft.creativeTab);
        psycheSapling.setCreativeTab(Psychedelicraft.creativeTab);
        bottleRack.setCreativeTab(creativeTab);
        wineGrapeLattice.setCreativeTab(Psychedelicraft.creativeTab);
        dryingTable.setCreativeTab(Psychedelicraft.creativeTab);*/

    }
}

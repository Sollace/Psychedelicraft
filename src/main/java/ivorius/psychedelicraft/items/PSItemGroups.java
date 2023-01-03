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
            .icon(PSItems.cannabisLeaf::getDefaultStack)
            .entries((features, entries, search) -> {
                entries.add(PSItems.tobaccoSeeds);
                entries.add(PSItems.tobaccoLeaf);
                entries.add(PSItems.driedTobacco);
                entries.add(PSItems.cigarette);
                entries.add(PSItems.cigar);
                entries.add(PSItems.joint);
                entries.add(PSItems.cocaSeeds);
                entries.add(PSItems.cocaLeaf);
                entries.add(PSItems.driedCocaLeaves);
                entries.add(PSItems.cocainePowder);
                entries.add(PSItems.syringe);
                entries.add(PSItems.wineGrapes);
                entries.add(PSItems.hopCones);
                entries.add(PSItems.hopSeeds);
                entries.add(PSItems.cannabisSeeds);
                entries.add(PSItems.cannabisLeaf);
                entries.add(PSItems.cannabisBuds);
                entries.add(PSItems.driedCannabisLeaves);
                entries.add(PSItems.pipe);
                entries.add(PSItems.bong);
                entries.add(PSItems.hashMuffin);
                entries.add(PSItems.magicMushroomsBrown);
                entries.add(PSItems.magicMushroomsRed);
                entries.add(PSItems.juniperBerries);
                entries.add(PSItems.coffeaCherries);
                entries.add(PSItems.coffeeBeans);
                entries.add(PSItems.peyote);
                entries.add(PSItems.driedPeyote);
                entries.add(PSItems.peyoteJoint);
                entries.add(PSItems.itemFlask);
                entries.add(PSItems.itemDistillery);
                if (PSConfig.enableHarmonium) {
                    entries.add(PSItems.harmonium);
                }
                if (PSConfig.enableRiftJars) {
                    entries.add(ItemRiftJar.createFilledRiftJar(0.0F, PSItems.itemRiftJar));
                    entries.add(ItemRiftJar.createFilledRiftJar(0.25F, PSItems.itemRiftJar));
                    entries.add(ItemRiftJar.createFilledRiftJar(0.55F, PSItems.itemRiftJar));
                    entries.add(ItemRiftJar.createFilledRiftJar(0.75F, PSItems.itemRiftJar));
                    entries.add(ItemRiftJar.createFilledRiftJar(0.9F, PSItems.itemRiftJar));
                }
            })
            .build();
    ItemGroup drinksTab = FabricItemGroup.builder(Psychedelicraft.id("drinks"))
            .icon(PSItems.itemBarrel::getDefaultStack)
            .entries((features, entries, search) -> {
                entries.add(PSItems.stoneCup);
                entries.add(PSItems.woodenMug);
                entries.add(PSItems.glassChalice);
                entries.add(PSItems.bottle);
                entries.add(PSItems.itemMashTub);
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

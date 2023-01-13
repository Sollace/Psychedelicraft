/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.world.gen;

/**
 * Created by lukas on 25.04.14.
 */
public class PSWorldGen {
    public static void bootstrap() {
        JuniperTreeGenerationConfigs.bootstrap();
/*
        // TODO: Tree generator
        if (PSConfig.genJuniper)
            registerWorldGenerator(new GeneratorGeneric(new JuniperTreeSaplingGenerator(false),
                    new EntryBiomeTypes(0.1f, 1, HILLS, COLD),
                    new EntryBiomeTypes(0.05f, 1, FOREST, COLD),
                    new EntryBiomeTypes(0.05f, 1, SNOWY, WASTELAND)
            ), 10);

        if (PSConfig.genCannabis)
            registerWorldGenerator(new GeneratorGeneric(new WorldGenTilledPatch(false, false, cannabisPlant),
                    new EntryBiomeTypes(0.0f, 1, COLD),
                    new EntryBiomeTypes(0.0f, 1, HILLS),
                    new EntryBiomeTypes(0.04f, 1, PLAINS),
                    new EntryBiomeTypes(0.04f, 1, FOREST)
            ), 10);

        if (PSConfig.genHop)
            registerWorldGenerator(new GeneratorGeneric(new WorldGenTilledPatch(false, false, hopPlant),
                    new EntryBiomeTypes(0.0f, 1, COLD),
                    new EntryBiomeTypes(0.0f, 1, HILLS),
                    new EntryBiomeTypes(0.06f, 1, PLAINS),
                    new EntryBiomeTypes(0.06f, 1, FOREST)
            ), 10);

        if (PSConfig.genTobacco)
            registerWorldGenerator(new GeneratorGeneric(new WorldGenTilledPatch(false, false, tobaccoPlant),
                    new EntryBiomeTypes(0.0f, 1, COLD),
                    new EntryBiomeTypes(0.0f, 1, HILLS),
                    new EntryBiomeTypes(0.04f, 1, PLAINS),
                    new EntryBiomeTypes(0.04f, 1, FOREST)
            ), 10);

        if (PSConfig.genCoffea)
            registerWorldGenerator(new GeneratorGeneric(new WorldGenTilledPatch(false, false, coffea),
                    new EntryBiomeTypes(0.0f, 1, COLD),
                    new EntryBiomeTypes(0.0f, 1, HILLS),
                    new EntryBiomeTypes(0.05f, 1, PLAINS),
                    new EntryBiomeTypes(0.05f, 1, FOREST)
            ), 10);

        if (PSConfig.genCoca)
            registerWorldGenerator(new GeneratorGeneric(new WorldGenTilledPatch(false, true, cocaPlant),
                    new EntryBiomeTypes(0.0f, 1, COLD),
                    new EntryBiomeTypes(0.0f, 1, HILLS),
                    new EntryBiomeTypes(0.0f, 1, SPARSE),
                    new EntryBiomeTypes(0.04f, 1, PLAINS),
                    new EntryBiomeTypes(0.04f, 1, FOREST),
                    new EntryBiomeTypes(0.04f, 1, WATER)
            ), 10);

        if (PSConfig.genPeyote)
            registerWorldGenerator(new GeneratorGeneric(new WorldGenPeyote(false),
                    new EntryBiomeTypes(0.04f, 1, SANDY, HOT),
                    new EntryBiomeTypes(0.04f, 1, MOUNTAIN, HOT)
            ), 10);

        if (PSConfig.dungeonChests)
        {
            // TODO: Loot tables
            addItem(DUNGEON_CHEST, new WeightedRandomChestContent(wineGrapes, 0, 1, 8, 10));
            addItem(DUNGEON_CHEST, new WeightedRandomChestContent(glassChalice, 0, 1, 4, 5));
            addItem(DUNGEON_CHEST, new WeightedRandomChestContent(woodenMug, 0, 1, 16, 5));
            addItem(DUNGEON_CHEST, new WeightedRandomChestContent(juniperBerries, 0, 1, 8, 10));
            addItem(DUNGEON_CHEST, new WeightedRandomChestContent(driedTobacco, 0, 1, 16, 5));

            addItem(MINESHAFT_CORRIDOR, new WeightedRandomChestContent(wineGrapes, 0, 1, 8, 10));
            addItem(MINESHAFT_CORRIDOR, new WeightedRandomChestContent(woodenMug, 0, 1, 16, 5));
            addItem(MINESHAFT_CORRIDOR, new WeightedRandomChestContent(juniperBerries, 0, 1, 8, 10));
            addItem(MINESHAFT_CORRIDOR, new WeightedRandomChestContent(driedTobacco, 0, 1, 16, 5));
            addItem(MINESHAFT_CORRIDOR, new WeightedRandomChestContent(pipe, 0, 1, 1, 3));
            addItem(MINESHAFT_CORRIDOR, new WeightedRandomChestContent(cigarette, 0, 1, 8, 5));
        }

        if (PSConfig.villageChests)
        {
            // TODO: Loot tables
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(wineGrapes, 0, 1, 8, 10));
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(juniperBerries, 0, 1, 8, 10));
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(woodenMug, 0, 1, 16, 5));
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(cigarette, 0, 1, 16, 1));
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(cigar, 0, 1, 1, 1));
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(joint, 0, 1, 1, 1));

            ItemStack coffeeStack = new ItemStack(woodenMug);
            woodenMug.fill(coffeeStack, new FluidStack(PSFluids.coffee, 1), true);
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(coffeeStack, 1, 4, 1));

            ItemStack peyoteStack = new ItemStack(stoneCup);
            stoneCup.fill(coffeeStack, new FluidStack(PSFluids.peyoteJuice, 1), true);
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(peyoteStack, 1, 1, 1));

            ItemStack cocaineStack = new ItemStack(syringe);
            syringe.fill(coffeeStack, new FluidStack(PSFluids.cocaineFluid, 1), true);
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(cocaineStack, 1, 1, 1));

            ItemStack caffeineStack = new ItemStack(syringe);
            syringe.fill(coffeeStack, new FluidStack(PSFluids.caffeineFluid, 1), true);
            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(caffeineStack, 1, 1, 1));

            addItem(VILLAGE_BLACKSMITH, new WeightedRandomChestContent(hashMuffin, 0, 1, 8, 1));
        }*/
    }
}

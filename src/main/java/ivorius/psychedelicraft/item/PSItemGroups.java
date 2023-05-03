/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.*;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.*;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.MathHelper;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSItemGroups {
    ItemGroup creativeTab = FabricItemGroup.builder(Psychedelicraft.id("general"))
            .icon(PSItems.CANNABIS_LEAF::getDefaultStack)
            .entries((features, entries, search) -> {
                entries.add(PSItems.DRYING_TABLE);
                entries.add(PSItems.IRON_DRYING_TABLE);
                entries.add(PSItems.FLASK);
                entries.add(PSItems.DISTILLERY);
                entries.add(PSItems.BOTTLE_RACK);
                entries.add(PSItems.MASH_TUB);

                entries.add(PSItems.OAK_BARREL);
                entries.add(PSItems.BIRCH_BARREL);
                entries.add(PSItems.SPRUCE_BARREL);
                entries.add(PSItems.ACACIA_BARREL);
                entries.add(PSItems.JUNGLE_BARREL);
                entries.add(PSItems.DARK_OAK_BARREL);

                if (Psychedelicraft.getConfig().balancing.enableRiftJars) {
                    entries.add(RiftJarItem.createFilledRiftJar(0.0F, PSItems.RIFT_JAR));
                    entries.add(RiftJarItem.createFilledRiftJar(0.25F, PSItems.RIFT_JAR));
                    entries.add(RiftJarItem.createFilledRiftJar(0.55F, PSItems.RIFT_JAR));
                    entries.add(RiftJarItem.createFilledRiftJar(0.75F, PSItems.RIFT_JAR));
                    entries.add(RiftJarItem.createFilledRiftJar(0.9F, PSItems.RIFT_JAR));
                }

                entries.add(PSItems.TRAY);
                entries.add(PSItems.BUNSEN_BURNER);

                entries.add(PSItems.SMOKING_PIPE);
                entries.add(PSItems.CIGARETTE);
                entries.add(PSItems.CIGAR);
                entries.add(PSItems.JOINT);

                entries.add(PSItems.BONG);
                entries.add(PSItems.SYRINGE);
                entries.add(PSItems.SYRINGE.getDefaultStack(PSFluids.COCAINE));
                entries.add(PSItems.SYRINGE.getDefaultStack(PSFluids.CAFFEINE));
                entries.add(PSItems.SYRINGE.getDefaultStack(PSFluids.BATH_SALTS));
                entries.add(ChemicalExtractFluid.DISTILLATION.set(PSItems.SYRINGE.getDefaultStack(PSFluids.MORNING_GLORY_EXTRACT), 2));

                entries.add(PSItems.COFFEA_CHERRIES);
                entries.add(PSItems.COFFEE_BEANS);

                entries.add(PSItems.TOMATO_LEAF);
                entries.add(PSItems.TOMATO_SEEDS);
                entries.add(PSItems.TOMATO);

                entries.add(PSItems.OBSIDIAN_BOTTLE);
                entries.add(PSItems.OBSIDIAN_DUST);

               // entries.add(PSItems.KAVA_SEEDS);
               // entries.add(PSItems.KAVA_ROOT);

                entries.add(PSItems.MORNING_GLORY);
                entries.add(PSItems.MORNING_GLORY_SEEDS);
                entries.add(PSItems.LSA_SQUARE);
                entries.add(PSItems.LSD_PILL);

                entries.add(PSItems.JIMSONWEED_SEEDS);
                entries.add(PSItems.JIMSONWEED_SEED_POD);
                entries.add(PSItems.JIMSONWEED_LEAF);
                entries.add(PSItems.DRIED_JIMSONWEED_LEAF);

                entries.add(PSItems.BELLADONNA_SEEDS);
                entries.add(PSItems.BELLADONNA_BERRIES);
                entries.add(PSItems.BELLADONNA_LEAF);
                entries.add(PSItems.DRIED_BELLADONNA_LEAF);

                entries.add(PSItems.TOBACCO_SEEDS);
                entries.add(PSItems.TOBACCO_LEAVES);
                entries.add(PSItems.DRIED_TOBACCO);

                entries.add(PSItems.COCA_SEEDS);
                entries.add(PSItems.COCA_LEAVES);
                entries.add(PSItems.DRIED_COCA_LEAVES);
                entries.add(PSItems.COCAINE_POWDER);

                entries.add(PSItems.PEYOTE);
                entries.add(PSItems.DRIED_PEYOTE);
                entries.add(PSItems.PEYOTE_JOINT);

                entries.add(PSItems.HOP_SEEDS);
                entries.add(PSItems.HOP_CONES);

                entries.add(PSItems.CANNABIS_SEEDS);
                entries.add(PSItems.CANNABIS_LEAF);
                entries.add(PSItems.DRIED_CANNABIS_LEAF);
                entries.add(PSItems.CANNABIS_BUDS);
                entries.add(PSItems.DRIED_CANNABIS_BUDS);

                entries.add(PSItems.HASH_MUFFIN);

                entries.add(PSItems.BROWN_MAGIC_MUSHROOMS);
                entries.add(PSItems.RED_MAGIC_MUSHROOMS);

                entries.add(PSItems.LATTICE);
                entries.add(PSItems.WINE_GRAPES);

                entries.add(PSItems.JUNIPER_LEAVES);
                entries.add(PSItems.FRUITING_JUNIPER_LEAVES);
                entries.add(PSItems.JUNIPER_LOG);
                entries.add(PSItems.JUNIPER_WOOD);
                entries.add(PSItems.STRIPPED_JUNIPER_LOG);
                entries.add(PSItems.STRIPPED_JUNIPER_WOOD);
                entries.add(PSItems.JUNIPER_SAPLING);
                entries.add(PSItems.JUNIPER_BERRIES);

                entries.add(PSItems.JUNIPER_PLANKS);
                entries.add(PSItems.JUNIPER_STAIRS);
                entries.add(PSItems.JUNIPER_SIGN);
                entries.add(PSItems.JUNIPER_DOOR);
                entries.add(PSItems.JUNIPER_HANGING_SIGN);
                entries.add(PSItems.JUNIPER_PRESSURE_PLATE);
                entries.add(PSItems.JUNIPER_FENCE);
                entries.add(PSItems.JUNIPER_TRAPDOOR);
                entries.add(PSItems.JUNIPER_FENCE_GATE);
                entries.add(PSItems.JUNIPER_BUTTON);
                entries.add(PSItems.JUNIPER_SLAB);
                entries.add(PSItems.JUNIPER_BOAT);
                entries.add(PSItems.JUNIPER_CHEST_BOAT);

                entries.add(PSItems.PAPER_BAG);

                if (Psychedelicraft.getConfig().balancing.enableHarmonium) {
                    for (DyeColor dye : DyeColor.values()) {
                        float[] color = dye.getColorComponents();
                        ItemStack harmonium = PSItems.HARMONIUM.getDefaultStack();
                        PSItems.HARMONIUM.setColor(harmonium, MathHelper.packRgb(color[0], color[1], color[2]));
                        entries.add(harmonium);
                    }
                }
            })
            .build();
    ItemGroup drinksTab = FabricItemGroup.builder(Psychedelicraft.id("drinks"))
            .icon(PSItems.OAK_BARREL::getDefaultStack)
            .entries((features, entries, search) -> {
                appendAllFluids(PSItems.STONE_CUP, entries);
                appendAllFluids(PSItems.WOODEN_MUG, entries);
                appendAllFluids(PSItems.GLASS_CHALICE, entries);
                appendAllFluids(PSItems.BOTTLE, entries);
                appendAllFluids(PSItems.FILLED_BUCKET, entries);
                appendAllFluids(PSItems.FILLED_BOWL, entries);
                appendAllFluids(PSItems.FILLED_GLASS_BOTTLE, entries);
            })
            .build();
    ItemGroup weaponsTab = FabricItemGroup.builder(Psychedelicraft.id("weapons"))
            .icon(PSItems.MOLOTOV_COCKTAIL::getDefaultStack)
            .entries((features, entries, search) -> {
                appendAllFluids(PSItems.MOLOTOV_COCKTAIL, entries);
            })
            .build();

    private static void appendAllFluids(FluidContainer item, ItemGroup.Entries entries) {
        SimpleFluid.all().forEach(fluid -> {
            if (fluid.isSuitableContainer(item)) {
                fluid.getDefaultStacks(item, entries::add);
            }
        });
    }

    static void bootstrap() { }
}

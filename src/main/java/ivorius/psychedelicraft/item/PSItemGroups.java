/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import java.util.List;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.RiftFractionComponent;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSItemGroups {
    RegistryKey<ItemGroup> GENERAL = register("general", FabricItemGroup.builder()
            .icon(PSItems.CANNABIS_LEAF::getDefaultStack)
            .entries((context, entries) -> {
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
                    entries.add(PSItems.RIFT_JAR.getDefaultStack());
                    entries.add(RiftFractionComponent.set(PSItems.RIFT_JAR.getDefaultStack(), 0.25F));
                    entries.add(RiftFractionComponent.set(PSItems.RIFT_JAR.getDefaultStack(), 0.55F));
                    entries.add(RiftFractionComponent.set(PSItems.RIFT_JAR.getDefaultStack(), 0.75F));
                    entries.add(RiftFractionComponent.set(PSItems.RIFT_JAR.getDefaultStack(), 0.9F));
                }

                entries.add(PSItems.TRAY);
                entries.add(PSItems.BUNSEN_BURNER);

                entries.add(PSItems.SMOKING_PIPE);
                entries.add(PSItems.CIGARETTE);
                entries.add(PSItems.CIGAR);
                entries.add(PSItems.JOINT);

                entries.add(PSItems.BONG);
                entries.add(PSItems.SYRINGE);
                List.of(PSFluids.COCAINE, PSFluids.CAFFEINE, PSFluids.BATH_SALTS).forEach(fluid -> {
                    entries.add(ItemFluids.set(PSItems.SYRINGE.getDefaultStack(), fluid.getDefaultStack(FluidVolumes.SYRINGE)));
                });
                entries.add(ItemFluids.set(PSItems.SYRINGE.getDefaultStack(), PSFluids.MORNING_GLORY_EXTRACT.getDefaultStack(FluidVolumes.SYRINGE)));
                entries.add(ItemFluids.set(PSItems.SYRINGE.getDefaultStack(), ChemicalExtractFluid.DISTILLATION.set(PSFluids.MORNING_GLORY_EXTRACT.getDefaultStack(FluidVolumes.SYRINGE), 2)));

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

                entries.add(PSItems.AGAVE_LEAF);
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
                        ItemStack harmonium = PSItems.HARMONIUM.getDefaultStack();
                        harmonium.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(dye.getSignColor(), true));
                        entries.add(harmonium);
                    }
                }
            }));
    RegistryKey<ItemGroup> DRINKS = register("drinks", FabricItemGroup.builder()
            .icon(PSItems.OAK_BARREL::getDefaultStack)
            .entries((context, entries) -> {
                appendAllFluids(PSItems.STONE_CUP.getDefaultStack(), entries);
                entries.add(PSItems.SHOT_GLASS);
                PSFluids.AGAVE.getDefaultStacks(PSItems.SHOT_GLASS.getDefaultStack(), entries::add);
                appendAllFluids(PSItems.WOODEN_MUG.getDefaultStack(), entries);
                appendAllFluids(PSItems.GLASS_CHALICE.getDefaultStack(), entries);
                appendAllFluids(PSItems.BOTTLE.getDefaultStack(), entries);
                appendAllFluids(PSItems.FILLED_BUCKET.getDefaultStack(), entries);
                appendAllFluids(PSItems.FILLED_BOWL.getDefaultStack(), entries);
                appendAllFluids(PSItems.FILLED_GLASS_BOTTLE.getDefaultStack(), entries);
            }));
    RegistryKey<ItemGroup> WEAPONS = register("weapons", FabricItemGroup.builder()
            .icon(PSItems.MOLOTOV_COCKTAIL::getDefaultStack)
            .entries((context, entries) -> {
                if (!Psychedelicraft.getConfig().balancing.disableMolotovs) {
                    appendAllFluids(PSItems.MOLOTOV_COCKTAIL.getDefaultStack(), entries);
                }
            }));

    private static void appendAllFluids(ItemStack item, ItemGroup.Entries entries) {
        int capacity = FluidCapacity.get(item);
        if (capacity > 0) {
            SimpleFluid.REGISTRY.forEach(fluid -> {
                if (fluid.isSuitableContainer(item)) {
                    fluid.getDefaultStacks(item, entries::add);
                }
            });
        }
    }

    static RegistryKey<ItemGroup> register(String name, ItemGroup.Builder builder) {
        RegistryKey<ItemGroup> key = RegistryKey.of(RegistryKeys.ITEM_GROUP, Psychedelicraft.id(name));
        Registry.register(Registries.ITEM_GROUP, key.getValue(), builder
                .displayName(Text.translatable(Util.createTranslationKey("itemGroup", key.getValue())))
                .build()
        );
        return key;
    }

    static void bootstrap() { }
}

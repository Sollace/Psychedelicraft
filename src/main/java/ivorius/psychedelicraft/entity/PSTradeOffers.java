/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.fluid.AlcoholicFluid;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.alcohol.DrinkTypes;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.*;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSTradeOffers {
    RegistryKey<PointOfInterestType> DRUG_DEALER_POI = poi("drug_dealer");
    VillagerProfession DRUG_DEALER_PROFESSION = register("drug_dealer",
            type -> type.matchesKey(DRUG_DEALER_POI),
            type -> type.matchesKey(DRUG_DEALER_POI),
            ImmutableSet.of(
                    PSItems.CANNABIS_SEEDS, PSItems.HOP_SEEDS, PSItems.TOBACCO_SEEDS,
                    PSItems.COCA_SEEDS, PSItems.COFFEA_CHERRIES, PSItems.MORNING_GLORY_SEEDS,
                    PSItems.CANNABIS_BUDS, PSItems.CANNABIS_LEAF,
                    PSItems.TOBACCO_LEAVES, PSItems.COCA_LEAVES,
                    PSItems.AGAVE_LEAF,
                    PSItems.PEYOTE, PSItems.COFFEA_CHERRIES,
                    Items.BONE_MEAL
            ),
            ImmutableSet.of(Blocks.FARMLAND),
            SoundEvents.ENTITY_WANDERING_TRADER_DRINK_POTION
    );

    VillagerProfession DRUG_ADDICT_PROFESSION = register("drug_addict",
            PointOfInterestType.NONE,
            VillagerProfession.IS_ACQUIRABLE_JOB_SITE,
            ImmutableSet.of(),
            ImmutableSet.of(),
            null
    );

    static void bootstrap() {
        TradeOfferHelper.registerVillagerOffers(DRUG_DEALER_PROFESSION, 1, factories -> {
            factories.add(sell(1, PSItems.CANNABIS_LEAF, 1, 9, 1, 0.7f));
            factories.add(sell(1, PSItems.CANNABIS_SEEDS, 5, 12, 2, 0.5f));
            factories.add(sell(1, PSItems.HASH_MUFFIN, 2, 3, 1, 0.7f));
            factories.add(sell(1, PSItems.COCA_LEAVES, 4, 4, 1, 0.5f));
            factories.add(sell(2, PSItems.COCA_SEEDS, 4, 4, 1, 0.5f));
            factories.add(sell(1, PSItems.PEYOTE, 5, 4, 4, 0.5f));

            factories.add(sell(1, PSItems.CIGARETTE, 4, 2, 3, 0.8f));
        });
        TradeOfferHelper.registerVillagerOffers(DRUG_DEALER_PROFESSION, 2, factories -> {
            factories.add(sell(2, PSItems.DRIED_CANNABIS_BUDS, 2, 8, 2, 0.9f));
            factories.add(sell(2, PSItems.DRIED_CANNABIS_LEAF, 2, 5, 3, 0.8f));
            factories.add(sell(3, PSItems.DRIED_PEYOTE, 10, 2, 2, 0.5f));
            factories.add(sell(3, PSItems.DRIED_COCA_LEAVES, 20, 3, 2, 0.5f));

            factories.add(sell(1, PSItems.CIGAR, 5, 2, 3, 0.5f));
            factories.add(sell(1, PSItems.SMOKING_PIPE, 5, 2, 3, 0.5f));
            factories.add(sell(6, PSItems.DRYING_TABLE, 1, 2, 3, 0.5f));
        });
        TradeOfferHelper.registerVillagerOffers(DRUG_DEALER_PROFESSION, 3, factories -> {
            factories.add(sell(5, PSItems.BROWN_MAGIC_MUSHROOMS, 8, 3, 3, 0.5f));
            factories.add(sell(2, PSItems.RED_MAGIC_MUSHROOMS, 8, 3, 3, 0.5f));

            factories.add(sell(3, PSItems.SYRINGE, 4, 3, 1, 0.5f));
            factories.add(sell(3, PSItems.BONG, 4, 3, 1, 0.5f));
            factories.add(sell(1, PSItems.PEYOTE_JOINT, 3, 2, 3, 0.5f));
            factories.add(sell(2, PSItems.LSD_PILL, 3, 2, 3, 0.5f));
            factories.add(trade(3, Items.PAPER, 2, PSItems.LSA_SQUARE, 3, 2, 3));

            factories.add(sell(1, PSItems.JOINT, 2, 2, 3, 0.5f));

            if (Psychedelicraft.getConfig().enableHarmonium.get()) {
                factories.add(new TradeOffers.SellDyedArmorFactory(PSItems.HARMONIUM, 3, 7, 2));
            }
        });
        TradeOfferHelper.registerVillagerOffers(DRUG_DEALER_PROFESSION, 4, factories -> {
            factories.add(sell(10, PSItems.CRACK_COCAINE, 7, 3, 3, 0.5f));
            factories.add(sell(13, PSItems.CRYSTAL_METH, 10, 3, 3, 0.5f));

            factories.add(sell(3, PSItems.EXTACY, 4, 3, 1, 0.5f));
            factories.add(sell(9, PSItems.HEROINE_POWDER, 4, 3, 1, 0.5f));
            factories.add(sell(2, PSItems.PEYOTE_JOINT, 3, 2, 3, 0.5f));
            factories.add(sell(3, PSItems.LSD_PILL, 6, 2, 3, 0.5f));
        });
        TradeOfferHelper.registerVillagerOffers(DRUG_DEALER_PROFESSION, 5, factories -> {
            factories.add(trade(8, PSItems.RIFT_JAR, 10, Items.DIAMOND_AXE, 1, 1, 3));
            factories.add(trade(3, PSItems.OBSIDIAN_DUST, 3, PSItems.OBSIDIAN_BOTTLE, 1, 5, 3));
        });


        TradeOfferHelper.registerVillagerOffers(DRUG_ADDICT_PROFESSION, 1, factories -> {
            factories.add(buy(5, PSItems.BROWN_MAGIC_MUSHROOMS, 8, 3, 3));
            factories.add(buy(2, PSItems.RED_MAGIC_MUSHROOMS, 8, 3, 3));
            factories.add(buy(2, PSItems.LSD_PILL, 3, 2, 3));
            factories.add(buy(1, PSItems.JOINT, 2, 2, 3));
            factories.add(buy(1, PSItems.CIGARETTE, 4, 2, 3));
            factories.add(buy(1, PSItems.CIGAR, 5, 2, 3));
            factories.add(buy(1, PSItems.HASH_MUFFIN, 2, 3, 1));

            factories.add(buy(4, PSItems.DRIED_CANNABIS_BUDS, 2, 8, 2));
            factories.add(buy(4, PSItems.DRIED_CANNABIS_LEAF, 2, 5, 3));
            factories.add(buy(5, PSItems.DRIED_PEYOTE, 10, 2, 2));
            factories.add(buy(5, PSItems.DRIED_COCA_LEAVES, 20, 3, 2));
        });
        TradeOfferHelper.registerVillagerOffers(DRUG_ADDICT_PROFESSION, 2, factories -> {
            factories.add(buy(6, PSItems.CRACK_COCAINE, 7, 4, 3));
        });
        TradeOfferHelper.registerVillagerOffers(DRUG_ADDICT_PROFESSION, 3, factories -> {
            factories.add(buy(5, PSItems.HEROINE_POWDER, 5, 5, 3));
            factories.add(buy(3, PSItems.CRYSTAL_METH, 3, 5, 6));
        });
        TradeOfferHelper.registerVillagerOffers(DRUG_ADDICT_PROFESSION, 4, factories -> {
            factories.add(buy(15, PSItems.HEROINE_POWDER, 10, 6, 3));
            factories.add(buy(12, PSItems.CRYSTAL_METH, 11, 6, 3));
        });
        TradeOfferHelper.registerVillagerOffers(DRUG_ADDICT_PROFESSION, 5, factories -> {
            factories.add(buy(5, PSItems.EXTACY, 8, 7, 3));
            factories.add(trade(3, PSItems.CRYSTAL_METH, 3, PSItems.JOLLY_RANCHER, 1, 7, 5));
        });

        PointOfInterestTypes.register(Registries.POINT_OF_INTEREST_TYPE, DRUG_DEALER_POI, Stream.concat(
                        PSBlocks.DRYING_TABLE.getStateManager().getStates().stream(),
                        PSBlocks.IRON_DRYING_TABLE.getStateManager().getStates().stream()
        ).collect(Collectors.toUnmodifiableSet()), 1, 1);

        if (Psychedelicraft.getConfig().worldGeneration.get().farmerDrugDeals()) {
            TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 1, factories -> {
                factories.add(sell(2, PSItems.WINE_GRAPES, 3, 8, 1, 0.5F));
                factories.add(sell(1, PSItems.HOP_CONES, 1, 4, 1, 0.6F));
                factories.add(sell(1, PSItems.HOP_SEEDS, 1, 4, 1, 0.4F));
                factories.add(sell(1, PSItems.WOODEN_MUG, 1, 4, 1, 0.5F));
                factories.add(sell(4, PSItems.DRIED_TOBACCO, 1, 4, 1, 0.3F));
                factories.add(sell(2, PSItems.CIGARETTE, 1, 4, 1, 0.8F));
                factories.add(sell(2, PSItems.CIGAR, 1, 4, 1, 0.8F));
                factories.add(sell(2, PSItems.TOBACCO_SEEDS, 1, 4, 1, 0.3F));
                factories.add(sell(1, PSItems.COFFEE_BEANS, 1, 4, 1, 0.8F));
                factories.add(sell(1, PSItems.COFFEA_CHERRIES, 1, 4, 1, 0.6F));
            });
        }

        TradeOfferHelper.registerVillagerOffers(VillagerProfession.LIBRARIAN, 3, factories -> {
            factories.add(sell(6, PSItems.BOTTLE_RACK, 1, 12, 1, 0.5F));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.LIBRARIAN, 4, factories -> {
            var barrels = PSItems.ALL_BARRELS.stream().map(barrel -> sell(6, barrel, 1, 12, 1, 0.5F)).toList();
            factories.add((e, r) -> barrels.get(r.nextInt(barrels.size()) % barrels.size()).create(e, r));
        });

        TradeOfferHelper.registerVillagerOffers(VillagerProfession.CLERIC, 4, factories -> {
            factories.add(new PrepareFluidFactory(40, PSItems.BOTTLE, PSFluids.RED_GRAPES, 9, 1));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.BUTCHER, 4, factories -> {
            factories.add(new PrepareFluidFactory(25, PSItems.BOTTLE, PSFluids.MILK, 9, 1));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 4, factories -> {
            factories.add(new PrepareFluidFactory(30, PSItems.BOTTLE, PSFluids.APPLE, 9, 1));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 4, factories -> {
            factories.add(new TradeFluidFactory(30, PSItems.BOTTLE, PSFluids.RED_GRAPES, PSFluids.HONEY, 9, 1));
            factories.add(new TradeFluidFactory(25, PSItems.BOTTLE, PSFluids.MILK, PSFluids.CORN, 9, 1));
            factories.add(new TradeFluidFactory(35, PSItems.BOTTLE, PSFluids.TOMATO, PSFluids.PINEAPPLE, 9, 1));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 5, factories -> {
            factories.add(new TradeFluidFactory(45, PSItems.BOTTLE, PSFluids.WHEAT, PSFluids.BANANA, 9, 1));
            factories.add(new TradeFluidFactory(25, PSItems.BOTTLE, PSFluids.POTATO, PSFluids.PINEAPPLE, 9, 1));
            factories.add(new TradeFluidFactory(45, PSItems.BOTTLE, PSFluids.HONEY, PSFluids.RICE, 9, 1));
        });
    }

    private static TradeOffers.Factory buy(int price, Item item, int count, int maxUses, int experience) {
        return (entity, random) -> new TradeOffer(new ItemStack(item, count), new ItemStack(Items.EMERALD, price), maxUses, experience, 0.05F);
    }

    private static TradeOffers.Factory sell(int price, Item item, int count, int maxUses, int experience, float priceChange) {
        return new TradeOffers.SellItemFactory(item.getDefaultStack(), price, count, maxUses, experience, priceChange);
    }

    private static TradeOffers.Factory trade(int price, ItemConvertible item, int count, Item returnItem, int returnCount, int maxUses, int experience) {
        return new TradeOffers.ProcessItemFactory(item, count, price, returnItem, returnCount, maxUses, experience);
    }

    private static RegistryKey<PointOfInterestType> poi(String id) {
        return RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, Psychedelicraft.id(id));
    }

    private static VillagerProfession register(String id, Predicate<RegistryEntry<PointOfInterestType>> heldWorkstation, Predicate<RegistryEntry<PointOfInterestType>> acquirableWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound) {
        return Registry.register(Registries.VILLAGER_PROFESSION, Psychedelicraft.id(id), new VillagerProfession("psychedelicraft:" + id, heldWorkstation, acquirableWorkstation, gatherableItems, secondaryJobSites, workSound));
    }

    static class PrepareFluidFactory implements TradeOffers.Factory {
        private final int price;
        private final Item item;
        private final AlcoholicFluid fluid;
        private final List<DrinkTypes.Variant> variants;
        private final int maxUses;
        private final int experience;

        public PrepareFluidFactory(int price, Item item, AlcoholicFluid fluid, int maxUses, int experience) {
            this.price = price;
            this.fluid = fluid;
            this.variants = fluid.getVariants();
            this.item = item;
            this.maxUses = maxUses;
            this.experience = experience;
        }

        @Override
        public TradeOffer create(Entity entity, Random random) {
            DrinkTypes.Variant variant = variants.get(random.nextInt(variants.size()) % variants.size());
            ItemStack tradedItem = ItemFluids.set(new ItemStack(item, 1), fluid.getDefaultStack(FluidCapacity.get(item.getDefaultStack())));
            ItemStack soldItem = ItemFluids.set(item.getDefaultStack(), variant.predicate().state().apply(fluid.getDefaultStack(FluidCapacity.get(item.getDefaultStack()))));
            return new TradeOffer(new ItemStack(Items.EMERALD, price), tradedItem, soldItem, maxUses, experience, 0.3F);
        }
    }

    static class TradeFluidFactory implements TradeOffers.Factory {
        private final int price;
        private final Item item;
        private final AlcoholicFluid buy;
        private final AlcoholicFluid sell;

        private final int maxUses;
        private final int experience;

        public TradeFluidFactory(int price, Item item, AlcoholicFluid buy, AlcoholicFluid sell, int maxUses, int experience) {
            this.price = price;
            this.buy = buy;
            this.sell = sell;
            this.item = item;
            this.maxUses = maxUses;
            this.experience = experience;
        }

        @Override
        public TradeOffer create(Entity entity, Random random) {
            ItemStack tradedItem = ItemFluids.set(item.getDefaultStack(), buy.getDefaultStack(FluidCapacity.get(item.getDefaultStack())));
            ItemStack soldItem = ItemFluids.set(item.getDefaultStack(), sell.getDefaultStack(FluidCapacity.get(item.getDefaultStack())));
            return new TradeOffer(new ItemStack(Items.EMERALD, price), tradedItem, soldItem, maxUses, experience, 0.3F);
        }
    }
}

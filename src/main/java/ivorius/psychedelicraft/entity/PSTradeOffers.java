/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.item.PSItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.registry.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
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
            factories.add(trade(3, Items.PAPER, 2, PSItems.LSA_SQUARE, 3, 2, 3, 0.5f));

            factories.add(sell(1, PSItems.JOINT, 2, 2, 3, 0.5f));

            if (Psychedelicraft.getConfig().balancing.enableHarmonium) {
                factories.add(new TradeOffers.SellDyedArmorFactory(PSItems.HARMONIUM, 3, 7, 2));
            }
        });

        TradeOfferHelper.registerVillagerOffers(DRUG_ADDICT_PROFESSION, 1, factories -> {
            factories.add(buy(5, PSItems.BROWN_MAGIC_MUSHROOMS, 8, 3, 3, 0.5f));
            factories.add(buy(2, PSItems.RED_MAGIC_MUSHROOMS, 8, 3, 3, 0.5f));
            factories.add(buy(2, PSItems.LSD_PILL, 3, 2, 3, 0.5f));
            factories.add(buy(1, PSItems.JOINT, 2, 2, 3, 0.5f));
            factories.add(buy(1, PSItems.CIGARETTE, 4, 2, 3, 0.8f));
            factories.add(buy(1, PSItems.CIGAR, 5, 2, 3, 0.5f));
            factories.add(buy(1, PSItems.HASH_MUFFIN, 2, 3, 1, 0.7f));

            factories.add(buy(4, PSItems.DRIED_CANNABIS_BUDS, 2, 8, 2, 0.9f));
            factories.add(buy(4, PSItems.DRIED_CANNABIS_LEAF, 2, 5, 3, 0.8f));
            factories.add(buy(5, PSItems.DRIED_PEYOTE, 10, 2, 2, 0.5f));
            factories.add(buy(5, PSItems.DRIED_COCA_LEAVES, 20, 3, 2, 0.5f));
        });

        PointOfInterestTypes.register(Registry.POINT_OF_INTEREST_TYPE, DRUG_DEALER_POI, Stream.concat(
                        PSBlocks.DRYING_TABLE.getStateManager().getStates().stream(),
                        PSBlocks.IRON_DRYING_TABLE.getStateManager().getStates().stream()
        ).collect(Collectors.toUnmodifiableSet()), 1, 1);

        if (Psychedelicraft.getConfig().balancing.worldGeneration.farmerDrugDeals) {
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
    }

    private static TradeOffers.Factory buy(int cost, Item returnItem, int returnCount, int maxUses, int experience, float priceChange) {
        return (e, rng) -> new TradeOffer(new ItemStack(returnItem, returnCount), new ItemStack(Items.EMERALD, cost), maxUses, experience, priceChange);
    }

    private static TradeOffers.Factory sell(int cost, Item returnItem, int returnCount, int maxUses, int experience, float priceChange) {
        return (e, rng) -> new TradeOffer(new ItemStack(Items.EMERALD, cost), new ItemStack(returnItem, returnCount), maxUses, experience, priceChange);
    }

    private static TradeOffers.Factory trade(int cost, Item item, int count, Item returnItem, int returnCount, int maxUses, int experience, float priceChange) {
        return (e, rng) -> new TradeOffer(new ItemStack(Items.EMERALD, cost), new ItemStack(item, count), new ItemStack(returnItem, returnCount), maxUses, experience, priceChange);
    }

    private static RegistryKey<PointOfInterestType> poi(String id) {
        return RegistryKey.of(Registry.POINT_OF_INTEREST_TYPE_KEY, Psychedelicraft.id(id));
    }

    private static VillagerProfession register(String id, Predicate<RegistryEntry<PointOfInterestType>> heldWorkstation, Predicate<RegistryEntry<PointOfInterestType>> acquirableWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound) {
        return Registry.register(Registry.VILLAGER_PROFESSION, Psychedelicraft.id(id), new VillagerProfession("psychedelicraft:" + id, heldWorkstation, acquirableWorkstation, gatherableItems, secondaryJobSites, workSound));
    }
}

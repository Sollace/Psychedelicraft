/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entities;

import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.items.PSItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.village.*;
import net.minecraft.world.poi.PointOfInterestType;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSTradeOffers {
    VillagerProfession DRUG_DEALER_PROFESSION = register("drug_dealer", PointOfInterestType.NONE, VillagerProfession.IS_ACQUIRABLE_JOB_SITE, null);

    static void bootstrap() {
        TradeOfferHelper.registerVillagerOffers(DRUG_DEALER_PROFESSION, 0, factories -> {
            factories.add(buy(Items.EMERALD, 1, PSItems.cannabisLeaf, 1, 9, 1, 0.7f));
            factories.add(buy(Items.EMERALD, 1, PSItems.cannabisSeeds, 5, 12, 2, 0.5f));
            factories.add(buy(Items.EMERALD, 1, PSItems.driedCannabisBuds, 2, 8, 2, 0.9f));
            factories.add(buy(Items.EMERALD, 1, PSItems.driedCannabisLeaves, 2, 5, 3, 0.8f));
            factories.add(buy(Items.EMERALD, 1, PSItems.hashMuffin, 2, 3, 1, 0.7f));

            factories.add(buy(Items.EMERALD, 2, PSItems.magicMushroomsBrown, 8, 3, 3, 0.5f));
            factories.add(buy(Items.EMERALD, 2, PSItems.magicMushroomsRed, 8, 3, 3, 0.5f));

            factories.add(buy(Items.EMERALD, 1, PSItems.cocaLeaf, 4, 4, 1, 0.5f));
            factories.add(buy(Items.EMERALD, 2, PSItems.cocaSeeds, 4, 4, 1, 0.5f));
            factories.add(buy(Items.EMERALD, 2, PSItems.driedCocaLeaves, 20, 3, 2, 0.5f));

            factories.add(buy(Items.EMERALD, 2, PSItems.syringe, 4, 3, 1, 0.5f));

            factories.add(buy(Items.EMERALD, 3, PSItems.driedPeyote, 10, 2, 2, 0.5f));
            factories.add(buy(Items.EMERALD, 1, PSItems.peyoteJoint, 3, 2, 3, 0.5f));
            factories.add(buy(Items.EMERALD, 1, PSItems.peyote, 5, 4, 4, 0.5f));
        });

        if (PSConfig.getInstance().balancing.worldGeneration.farmerDrugDeals) {
            TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 0, factories -> {
                factories.add(buy(Items.EMERALD, 2, PSItems.wineGrapes, 3, 8, 1, 0.5F));
                factories.add(buy(Items.EMERALD, 1, PSItems.hopCones, 1, 4, 1, 0.6F));
                factories.add(buy(Items.EMERALD, 1, PSItems.hopSeeds, 1, 4, 1, 0.4F));
                factories.add(buy(Items.EMERALD, 1, PSItems.woodenMug, 1, 4, 1, 0.5F));
                factories.add(buy(Items.EMERALD, 4, PSItems.driedTobacco, 1, 4, 1, 0.3F));
                factories.add(buy(Items.EMERALD, 2, PSItems.cigarette, 1, 4, 1, 0.8F));
                factories.add(buy(Items.EMERALD, 2, PSItems.cigar, 1, 4, 1, 0.8F));
                factories.add(buy(Items.EMERALD, 2, PSItems.tobaccoSeeds, 1, 4, 1, 0.3F));
                factories.add(buy(Items.EMERALD, 1, PSItems.coffeeBeans, 1, 4, 1, 0.8F));
                factories.add(buy(Items.EMERALD, 1, PSItems.coffeaCherries, 1, 4, 1, 0.6F));
            });
        }
    }

    private static TradeOffers.Factory buy(Item item, int count, Item returnItem, int returnCount, int maxUses, int experience, float priceChange) {
        return (e, rng) -> new TradeOffer(new ItemStack(item, count), new ItemStack(returnItem, returnCount), maxUses, experience, priceChange);
    }

    private static VillagerProfession register(String id, Predicate<RegistryEntry<PointOfInterestType>> heldWorkstation, Predicate<RegistryEntry<PointOfInterestType>> acquirableWorkstation, @Nullable SoundEvent workSound) {
        return register(id, heldWorkstation, acquirableWorkstation, ImmutableSet.of(), ImmutableSet.of(), workSound);
    }

    private static VillagerProfession register(String id, Predicate<RegistryEntry<PointOfInterestType>> heldWorkstation, Predicate<RegistryEntry<PointOfInterestType>> acquirableWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound) {
        return Registry.register(Registries.VILLAGER_PROFESSION, Psychedelicraft.id(id), new VillagerProfession("psychedelicraft:" + id, heldWorkstation, acquirableWorkstation, gatherableItems, secondaryJobSites, workSound));
    }
}

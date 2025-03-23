package ivorius.psychedelicraft.datagen.providers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.advancement.CustomEventCriterion;
import ivorius.psychedelicraft.advancement.DrugEffectsChangedCriterion;
import ivorius.psychedelicraft.advancement.MashingTubEventCriterion;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.fluid.AlcoholicFluid;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.alcohol.DrinkType;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.PSComponents;
import ivorius.psychedelicraft.item.component.PSSubPredicates;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRequirements.CriterionMerger;
import net.minecraft.advancement.criterion.ConsumeItemCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.predicate.component.ComponentsPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.TagKey;

import static net.minecraft.advancement.criterion.InventoryChangedCriterion.Conditions.items;

public class PSAdvancementsProvider extends FabricAdvancementProvider {
    public PSAdvancementsProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> lookup) {
        super(output, lookup);
    }

    @Override
    public void generateAdvancement(WrapperLookup registries, Consumer<AdvancementEntry> exporter) {
        var items = registries.getOrThrow(RegistryKeys.ITEM);

        PSAdvancementBuilder.create(Psychedelicraft.id("root"), PSItems.CANNABIS_LEAF)
            .doNotAnnounce()
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR)
            .criterion("crafting_table", items(Items.CRAFTING_TABLE))
            .build(exporter)
            .children(root -> {
                root.child(Psychedelicraft.id("all_drugs"), PSItems.JOLLY_RANCHER)
                    .frame(AdvancementFrame.CHALLENGE)
                    .criteriaMerger(CriterionMerger.OR)
                    .criterion("have_all_effects", DrugEffectsChangedCriterion.Conditions.create(
                            DrugType.REGISTRY.stream().filter(i -> i != DrugType.SLEEP_DEPRIVATION).toList()
                    ))
                    .build(exporter);
                root.child(Psychedelicraft.id("make_drying_table"), PSItems.DRYING_TABLE)
                    .criteriaMerger(CriterionMerger.OR)
                    .criterion("has_the_thing", tag(items, PSTags.Items.DRYING_TABLES))
                    .build(exporter).children(makeDryingTable -> {
                        makeDryingTable.child(Psychedelicraft.id("dry_brown_mushrooms"), PSItems.BROWN_MAGIC_MUSHROOMS)
                            .criteriaMerger(CriterionMerger.OR)
                            .criterion("has_the_thing", items(PSItems.BROWN_MAGIC_MUSHROOMS))
                            .build(exporter);
                        makeDryingTable.child(Psychedelicraft.id("dry_red_mushrooms"), PSItems.RED_MAGIC_MUSHROOMS)
                            .criteriaMerger(CriterionMerger.OR)
                            .criterion("has_the_thing", items(PSItems.RED_MAGIC_MUSHROOMS))
                            .build(exporter);
                        makeDryingTable.child(Psychedelicraft.id("cannabis_buds"), PSItems.CANNABIS_BUDS)
                            .criteriaMerger(CriterionMerger.OR)
                            .criterion("has_the_thing", items(PSItems.CANNABIS_BUDS))
                            .build(exporter)
                            .child(Psychedelicraft.id("dry_cannabis_buds"), PSItems.DRIED_CANNABIS_BUDS)
                                .criteriaMerger(CriterionMerger.OR)
                                .criterion("has_the_thing", items(PSItems.DRIED_CANNABIS_BUDS))
                                .build(exporter)
                                .child(Psychedelicraft.id("roll_joint"), PSItems.JOINT)
                                    .criteriaMerger(CriterionMerger.OR)
                                    .criterion("has_joint", items(PSItems.JOINT))
                                    .criterion("has_peyote_joint", items(PSItems.PEYOTE_JOINT))
                                    .build(exporter);
                    });
                root.child(Psychedelicraft.id("time_to_cook"), PSItems.TRAY)
                    .criteriaMerger(CriterionMerger.OR)
                    .criterion("has_the_thing", items(PSItems.TRAY))
                    .build(exporter).children(makeTray -> {
                        makeTray.child(Psychedelicraft.id("first_batch"), PSItems.CRYSTAL_METH)
                            .criteriaMerger(CriterionMerger.OR)
                            .criterion("done_the_thing", CustomEventCriterion.Conditions.create("tray_harden"))
                            .build(exporter).children(firstBatch -> {
                                firstBatch.child(Psychedelicraft.id("the_good_stuff"), PSItems.CRYSTAL_METH)
                                    .criteriaMerger(CriterionMerger.OR)
                                    .criterion("has_crystal_meth", items(PSItems.CRYSTAL_METH))
                                    .build(exporter)
                                    .child(Psychedelicraft.id("thats_no_good"), PSItems.VOMIT)
                                        .criteriaMerger(CriterionMerger.OR)
                                        .criterion("has_side_effect", CustomEventCriterion.Conditions.create("side_effect"))
                                        .build(exporter)
                                        .child(Psychedelicraft.id("goo_goo"), PSItems.PACIFIER)
                                            .criteriaMerger(CriterionMerger.OR)
                                            .criterion("has_blocked_side_effect", CustomEventCriterion.Conditions.create("suck_pacifier"))
                                            .build(exporter);
                                firstBatch.child(Psychedelicraft.id("hows_it_cracking"), PSItems.CRACK_COCAINE)
                                    .criteriaMerger(CriterionMerger.OR)
                                    .criterion("has_heroine_powder", items(PSItems.HEROINE_POWDER))
                                    .criterion("has_crack_cocain", items(PSItems.CRACK_COCAINE))
                                    .build(exporter);
                            });

                        // eye_protection - wear goggles
                        // breaking_bad - wear entire suit
                    });
                root.child(Psychedelicraft.id("make_mash_tub"), PSItems.MASH_TUB)
                    .criteriaMerger(CriterionMerger.OR)
                    .criterion("has_the_thing", items(PSItems.MASH_TUB))
                    .build(exporter).children(makeMashTub -> {
                        makeMashTub.child(Psychedelicraft.id("hop_cones"), PSItems.HOP_CONES)
                            .criteriaMerger(CriterionMerger.OR)
                            .criterion("has_the_thing", items(PSItems.HOP_CONES))
                            .build(exporter);
                        makeMashTub.child(Psychedelicraft.id("grapes"), PSItems.WINE_GRAPES)
                            .criteriaMerger(CriterionMerger.OR)
                            .criterion("has_the_thing", items(PSItems.WINE_GRAPES))
                            .build(exporter)
                            .child(Psychedelicraft.id("wash_grapes"), PSItems.WINE_GRAPES)
                                .criteriaMerger(CriterionMerger.OR)
                                .criterion("mashed_item", MashingTubEventCriterion.Conditions.create(PSFluids.RED_GRAPES, IntRange.exactly(0), IntRange.ANY, IntRange.ANY))
                                .build(exporter)
                                .children(washGrapes -> {
                                    washGrapes.child(Psychedelicraft.id("drink_brandy"), PSItems.WOODEN_MUG)
                                        .icon(icon -> icon.set(PSComponents.FLUIDS, AlcoholicFluid.DISTILLATION.set(AlcoholicFluid.FERMENTATION.set(PSFluids.RED_GRAPES.getDefaultStack(), 1), 7)))
                                        .criteriaMerger(CriterionMerger.OR)
                                        .criterion("drink_brandy", ConsumeItemCriterion.Conditions.predicate(ItemPredicate.Builder.create()
                                                .components(ComponentsPredicate.Builder.create().partial(PSSubPredicates.DRINK_TYPE, DrinkType.Predicate.create(DrinkType.BRANDY)).build())))
                                        .build(exporter);
                                    washGrapes.child(Psychedelicraft.id("drink_brandy"), PSItems.WOODEN_MUG)
                                        .icon(icon -> icon.set(PSComponents.FLUIDS, AlcoholicFluid.DISTILLATION.set(AlcoholicFluid.FERMENTATION.set(PSFluids.RED_GRAPES.getDefaultStack(), 1), 7)))
                                        .criteriaMerger(CriterionMerger.OR)
                                        .criterion("drink_basi", ConsumeItemCriterion.Conditions.predicate(ItemPredicate.Builder.create()
                                                .components(ComponentsPredicate.Builder.create().partial(PSSubPredicates.DRINK_TYPE, DrinkType.Predicate.create(DrinkType.WINE, PSFluids.RED_GRAPES)).build())))
                                        .build(exporter);
                                    washGrapes.child(Psychedelicraft.id("splitting_headache"), PSItems.BOTTLE)
                                        .criteriaMerger(CriterionMerger.OR)
                                        .criterion("done_the_thing", CustomEventCriterion.Conditions.create("get_hangover"))
                                        .build(exporter);
                                });
                        makeMashTub.child(Psychedelicraft.id("wash_beer"), Items.WHEAT)
                            .criteriaMerger(CriterionMerger.OR)
                            .criterion("mashed_item", MashingTubEventCriterion.Conditions.create(PSFluids.WHEAT, IntRange.exactly(2), IntRange.ANY, IntRange.ANY))
                            .build(exporter)
                            .child(Psychedelicraft.id("drink_beer"), PSItems.WOODEN_MUG)
                                .icon(icon -> icon.set(PSComponents.FLUIDS, AlcoholicFluid.FERMENTATION.set(PSFluids.WHEAT_HOP.getDefaultStack(), 2)))
                                .criteriaMerger(CriterionMerger.OR)
                                .criterion("drink_beer", ConsumeItemCriterion.Conditions.predicate(ItemPredicate.Builder.create()
                                        .components(ComponentsPredicate.Builder.create().partial(PSSubPredicates.DRINK_TYPE, new DrinkType.Predicate(Optional.of(ItemFluids.Predicate.builder()
                                                .fluid(PSFluids.WHEAT, PSFluids.WHEAT_HOP)
                                                .attribute("maturation", IntRange.atMost(6))
                                                .build()), DrinkType.BEER)).build())))
                                .build(exporter)
                                .child(Psychedelicraft.id("drink_mature_beer"), PSItems.WOODEN_MUG)
                                    .icon(icon -> icon.set(PSComponents.FLUIDS, AlcoholicFluid.FERMENTATION.set(PSFluids.WHEAT_HOP.getDefaultStack(), 2)))
                                    .criteriaMerger(CriterionMerger.OR)
                                    .criterion("drink_beer", ConsumeItemCriterion.Conditions.predicate(ItemPredicate.Builder.create()
                                            .components(ComponentsPredicate.Builder.create().partial(PSSubPredicates.DRINK_TYPE, new DrinkType.Predicate(Optional.of(ItemFluids.Predicate.builder()
                                                    .fluid(PSFluids.WHEAT, PSFluids.WHEAT_HOP)
                                                    .attribute("maturation", IntRange.atLeast(7))
                                                    .build()), DrinkType.BEER)).build())))
                                    .build(exporter);
                        makeMashTub.child(Psychedelicraft.id("wash_sugar_cane"), Items.SUGAR_CANE)
                            .criteriaMerger(CriterionMerger.OR)
                            .criterion("mashed_item", MashingTubEventCriterion.Conditions.create(PSFluids.SUGAR_CANE, IntRange.exactly(2), IntRange.ANY, IntRange.ANY))
                            .build(exporter).children(washSugarCane -> {
                                washSugarCane.child(Psychedelicraft.id("drink_basi"), PSItems.WOODEN_MUG)
                                    .icon(icon -> icon.set(PSComponents.FLUIDS, AlcoholicFluid.FERMENTATION.set(PSFluids.SUGAR_CANE.getDefaultStack(), 2)))
                                    .criteriaMerger(CriterionMerger.OR)
                                    .criterion("drink_basi", ConsumeItemCriterion.Conditions.predicate(ItemPredicate.Builder.create()
                                            .components(ComponentsPredicate.Builder.create().partial(PSSubPredicates.DRINK_TYPE, DrinkType.Predicate.create(DrinkType.BASI, PSFluids.SUGAR_CANE)).build())))
                                    .build(exporter);
                                washSugarCane.child(Psychedelicraft.id("drink_rum"), PSItems.WOODEN_MUG)
                                    .icon(icon -> icon.set(PSComponents.FLUIDS, AlcoholicFluid.FERMENTATION.set(PSFluids.WHEAT_HOP.getDefaultStack(), 2)))
                                    .criteriaMerger(CriterionMerger.OR)
                                    .criterion("drink_rum", ConsumeItemCriterion.Conditions.predicate(ItemPredicate.Builder.create()
                                            .components(ComponentsPredicate.Builder.create().partial(PSSubPredicates.DRINK_TYPE, DrinkType.Predicate.create(DrinkType.RUM, PSFluids.SUGAR_CANE)).build())))
                                    .build(exporter);
                            });
                        makeMashTub.child(Psychedelicraft.id("make_distillery"), PSItems.DISTILLERY)
                            .criteriaMerger(CriterionMerger.OR)
                            .criterion("has_the_thing", items(PSItems.DISTILLERY))
                            .build(exporter);
                    });
                root.child(Psychedelicraft.id("hash_a_muffin"), PSItems.HASH_MUFFIN)
                    .criteriaMerger(CriterionMerger.OR)
                    .criterion("has_the_thing", items(PSItems.HASH_MUFFIN))
                    .build(exporter)
                    .child(Psychedelicraft.id("feed_a_villager"), PSItems.HASH_MUFFIN)
                        .criteriaMerger(CriterionMerger.OR)
                        .criterion("done_the_thing", CustomEventCriterion.Conditions.create("feed_villager"))
                        .build(exporter);
        });
    }

    public static AdvancementCriterion<InventoryChangedCriterion.Conditions> tag(RegistryEntryLookup<Item> lookup, TagKey<Item> tag) {
        return items(ItemPredicate.Builder.create().tag(lookup, tag));
    }
}

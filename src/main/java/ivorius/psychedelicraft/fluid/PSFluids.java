/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.SimpleFluid.Settings;
import ivorius.psychedelicraft.fluid.alcohol.FluidAppearance;
import ivorius.psychedelicraft.fluid.alcohol.DrinkType;
import ivorius.psychedelicraft.fluid.alcohol.DrinkTypes;
import ivorius.psychedelicraft.fluid.alcohol.DrinkType.Variation;
import ivorius.psychedelicraft.fluid.alcohol.StatePredicate;

/**
 * Created by lukas on 22.10.14.
 */
public interface PSFluids {
    SimpleFluid EMPTY = new SimpleFluid(SimpleFluid.EMPTY_KEY, new Settings().color(0xFFFFFFFF), true);
    AlcoholicFluid WHEAT_HOP = new AlcoholicFluid(Psychedelicraft.id("wheat_hop"), new AlcoholicFluid.Settings()
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoWheatHop())
            .variants(DrinkTypes.builder(Variation.BITTER)
                    .vinegar(DrinkType.VINEGAR.withName("beer_vinegar").withVariation(Variation.BITTER))
                    .firstFerment(DrinkType.HALF_WASH.withVariation(Variation.BITTER))
                    .secondFerment(DrinkType.BEER.withVariation("green").withAppearance(FluidAppearance.RUM_MATURE))
                    .matured(DrinkType.BEER)
                    .distilled(DrinkType.BEER.withAppearance(FluidAppearance.CLEAR))
            )
            .color(0xaafeaa08)
    );
    AlcoholicFluid WHEAT = new AlcoholicFluid(Psychedelicraft.id("wheat"), new AlcoholicFluid.Settings()
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoWheat())
            .variants(DrinkTypes.builder(DrinkType.WORT)
                    .vinegar(DrinkType.VINEGAR.withName("beer_vinegar"))
                    .firstFerment(DrinkType.HALF_WASH)
                    .secondFerment(DrinkType.WASH)
                    .matured(DrinkType.WASH)
                    .distilled(DrinkType.VODKA)
                    .add(DrinkType.WHISKEY.withName("wheat_whiskey").withVariation(Variation.WELL_AGED), StatePredicate.builder().minFerments(1).minMaturity(8).minDistills(1))
                    .matureDistilled(DrinkType.WHISKEY.withName("wheat_whiskey"))
            )
            .color(0xaafeaa08)
    );
    AlcoholicFluid POTATO = new AlcoholicFluid(Psychedelicraft.id("potato"), new AlcoholicFluid.Settings()
            .alcohol(0.45, 1.9, 0.15)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoPotato())
            .variants(DrinkTypes.builder(DrinkType.WORT)
                    .vinegar(DrinkType.VINEGAR)
                    .firstFerment(DrinkType.HALF_WASH)
                    .secondFerment(DrinkType.BEER)
                    .matured(DrinkType.BEER.withAppearance(FluidAppearance.RUM_MATURE))
                    .distilled(DrinkType.VODKA)
                    .matureDistilled(DrinkType.POTEEN.withAppearance(FluidAppearance.RUM_SEMI_MATURE))
            )
            .color(0xaafeaa08)
            .viscocity(2)
    );
    AlcoholicFluid TOMATO = new AlcoholicFluid(Psychedelicraft.id("tomato"), new AlcoholicFluid.Settings()
            .alcohol(0.15, 0.9, 0.05)
            .distilledColor(0xaaff0000)
            .matureColor(0xaaff3300)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoTomato())
            .variants(DrinkTypes.builder(DrinkType.JUICE.withAppearance(FluidAppearance.TOMATO_JUICE))
                    .vinegar(DrinkType.KETCHUP.withExtraDrug(new DrugInfluence(DrugType.SUGAR, 20, 0.003, 0.002, 0.3)))
                    .firstFerment(DrinkType.HALF_WASH)
                    .secondFerment(DrinkType.BEER)
                    .add(DrinkType.WHISKEY.withAppearance(FluidAppearance.RUM_SEMI_MATURE), StatePredicate.builder().minFerments(1).minMaturity(6).minDistills(2))
                    .add(DrinkType.MEAD, StatePredicate.builder().minFerments(1).minMaturity(5).distills(1))
                    .matured(DrinkType.BEER)
                    .distilled(DrinkType.VODKA)
            )
            .color(0xaaffaa08)
            .viscocity(1)
    );
    AlcoholicFluid RED_GRAPES = new AlcoholicFluid(Psychedelicraft.id("red_grapes"), new AlcoholicFluid.Settings()
            .alcohol(0.55, 1.7, 0.2)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoRedGrapes())
            .distilledColor(0x993f0822)
            .matureColor(0xee3f0822)
            .variants(DrinkTypes.builder(DrinkType.JUICE.withAppearance(FluidAppearance.WINE))
                    .vinegar(DrinkType.VINEGAR)
                    .add(DrinkType.VINEGAR, StatePredicate.builder().minMaturity(16))
                    .firstFerment(DrinkType.HALF_WASH.withVariation(Variation.WINE).withAppearance(FluidAppearance.WINE))
                    .secondFerment(DrinkType.WASH.withVariation(Variation.WINE).withAppearance(FluidAppearance.WINE))
                    .add(DrinkType.WINE.withVariation(Variation.WELL_AGED), StatePredicate.builder().ferments(1).minMaturity(14))
                    .add(DrinkType.WINE.withVariation(Variation.AGED), StatePredicate.builder().ferments(1).minMaturity(8))
                    .add(DrinkType.WINE.withVariation(Variation.SLIGHTLY_AGED), StatePredicate.builder().ferments(1).minMaturity(4))
                    .add(DrinkType.WINE.withVariation(Variation.YOUNG), StatePredicate.FERMENTED_MATURED)
                    .distilled(DrinkType.BRANDY)
            )
            .color(0xaafeaa08)
    );
    AlcoholicFluid RICE = new AlcoholicFluid(Psychedelicraft.id("rice"), new AlcoholicFluid.Settings()
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoRice())
            .matureColor(0x88D6BC90)
            .variants(DrinkTypes.builder(FluidAppearance.RICE_WINE)
                    .vinegar(DrinkType.VINEGAR)
                    .firstFerment(DrinkType.HALF_WASH.withAppearance(FluidAppearance.CLEAR))
                    .secondFerment(DrinkType.WINE.withVariation(Variation.YOUNG).withAppearance(FluidAppearance.CLEAR))
                    .matured(DrinkType.WINE.withAppearance(FluidAppearance.CLEAR))
                    .distilled(DrinkType.BRANDY.withAppearance(FluidAppearance.CLEAR))
            )
            .color(0xeecac4b2)
    );
    AlcoholicFluid JUNIPER = new AlcoholicFluid(Psychedelicraft.id("juniper"), new AlcoholicFluid.Settings()
            .alcohol(0.4, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoJuniper())
            .variants(DrinkTypes.builder(FluidAppearance.SLURRY)
                    .vinegar(DrinkType.VINEGAR)
                    .firstFerment(DrinkType.HALF_WASH)
                    .secondFerment(DrinkType.WASH.withVariation(Variation.YOUNG))
                    .matured(DrinkType.WASH)
                    .distilled(DrinkType.GIN)
            )
            .color(0xcc704E21)
    );
    AlcoholicFluid HONEY = new AlcoholicFluid(Psychedelicraft.id("honey"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x99e9ae3b)
            .matureColor(0xaaD1984D)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoHoney())
            .variants(DrinkTypes.builder(FluidAppearance.MEAD)
                    .vinegar(DrinkType.VINEGAR.withAppearance(FluidAppearance.MEAD))
                    .firstFerment(DrinkType.HALF_WASH.withAppearance(FluidAppearance.MEAD))
                    .secondFerment(DrinkType.MEAD.withVariation(Variation.YOUNG))
                    .matured(DrinkType.MEAD)
                    .distilled(DrinkType.BRANDY.withAppearance(FluidAppearance.MEAD))
            )
            .color(0xbbe9ae3b)
            .viscocity(5)
    );
    AlcoholicFluid SUGAR_CANE = new AlcoholicFluid(Psychedelicraft.id("sugar_cane"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .variants(DrinkTypes.builder(FluidAppearance.CLEAR)
                    .vinegar(DrinkType.VINEGAR)
                    .firstFerment(DrinkType.HALF_WASH)
                    .secondFerment(DrinkType.BASI.withVariation(Variation.YOUNG))
                    .matured(DrinkType.BASI)
                    .distilled(DrinkType.RUM.withVariation(Variation.YOUNG))
                    .matureDistilled(DrinkType.RUM)
                )
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoSugarCane())
            .color(0xaafeaa08)
    );
    AlcoholicFluid CORN = new AlcoholicFluid(Psychedelicraft.id("corn"), new AlcoholicFluid.Settings()
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoCorn())
            .variants(DrinkTypes.builder(FluidAppearance.BEER)
                    .vinegar(DrinkType.VINEGAR)
                    .firstFerment(DrinkType.HALF_WASH)
                    .secondFerment(DrinkType.BEER.withVariation(Variation.GREEN))
                    .matured(DrinkType.BEER)
                    .distilled(DrinkType.VODKA)
                    .matureDistilled(DrinkType.WHISKEY)
            )
            .color(0xaafeaa08)
    );
    AlcoholicFluid APPLE = new AlcoholicFluid(Psychedelicraft.id("apple"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x66EDC13B)
            .matureColor(0x88EDC13B)
            .variants(DrinkTypes.builder(DrinkType.CIDER.withVariation(Variation.SWEET))
                    .vinegar(DrinkType.VINEGAR)
                    .firstFerment(DrinkType.CIDER.withVariation(Variation.HALF_SWEET))
                    .secondFerment(DrinkType.CIDER.withVariation(Variation.HARD))
                    .matured(DrinkType.CIDER)
                    .distilled(DrinkType.BRANDY.withAppearance(FluidAppearance.CIDER))
            )
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoApple())
            .color(0x99EDC13B)
    );
    AlcoholicFluid PINEAPPLE = new AlcoholicFluid(Psychedelicraft.id("pineapple"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x66EDC13B)
            .matureColor(0x88EDC13B)
            .variants(DrinkTypes.builder(DrinkType.JUICE.withAppearance(FluidAppearance.CIDER))
                    .vinegar(DrinkType.VINEGAR)
                    .firstFerment(DrinkType.HALF_WASH.withAppearance(FluidAppearance.CIDER))
                    .secondFerment(DrinkType.WINE.withVariation("young").withAppearance(FluidAppearance.CIDER))
                    .matured(DrinkType.WINE.withAppearance(FluidAppearance.CIDER))
                    .distilled(DrinkType.BRANDY.withAppearance(FluidAppearance.CIDER))
            )
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoPineapple())
            .color(0x99EDC13B)
            .viscocity(2)
    );
    AlcoholicFluid BANANA = new AlcoholicFluid(Psychedelicraft.id("banana"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x99e9ae3b)
            .matureColor(0xaaD1984D)
            .variants(DrinkTypes.builder(DrinkType.JUICE.withAppearance(FluidAppearance.MEAD))
                    .vinegar(DrinkType.VINEGAR)
                    .firstFerment(DrinkType.HALF_WASH.withAppearance(FluidAppearance.MEAD))
                    .secondFerment(DrinkType.BEER.withAppearance(FluidAppearance.MEAD))
                    .matured(DrinkType.BEER.withAppearance(FluidAppearance.MEAD))
                    .distilled(DrinkType.BRANDY.withAppearance(FluidAppearance.MEAD))
            )
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoBanana())
            .color(0xbbe9ae3b)
            .viscocity(3)
    );
    AlcoholicFluid MILK = new AlcoholicFluid(Psychedelicraft.id("milk"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x77cac4b2)
            .matureColor(0x88D6BC90)
            .variants(DrinkTypes.builder(DrinkType.WORT.withAppearance(FluidAppearance.RICE_WINE))
                    .vinegar(DrinkType.VINEGAR.withAppearance(FluidAppearance.RICE_WINE))
                    .firstFerment(DrinkType.HALF_WASH.withAppearance(FluidAppearance.RICE_WINE))
                    .secondFerment(DrinkType.BLAAND.withAppearance(FluidAppearance.RICE_WINE))
                    .matured(DrinkType.BLAAND.withAppearance(FluidAppearance.RICE_WINE))
                    .distilled(DrinkType.ARKHI.withAppearance(FluidAppearance.RICE_WINE))
            )
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoMilk())
            .color(0x88ffffff)
            .viscocity(2)
    );
    AlcoholicFluid AGAVE = new AgaveFluid(Psychedelicraft.id("agave"), new AlcoholicFluid.Settings()
            .alcohol(0.15, 1.5, 0.05)
            .distilledColor(0x779beb62)
            .matureColor(0x779beb62)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoAgave())
            .variants(DrinkTypes.builder(DrinkType.JUICE)
                    .vinegar(DrinkType.VINEGAR)
                    .firstFerment(DrinkType.HALF_WASH)
                    .secondFerment(DrinkType.WASH)
                    .add(DrinkType.MEZCAL, StatePredicate.builder().minFerments(1).maturation(0).distills(1))
                    .add(DrinkType.TEQUILA.withVariation(Variation.BLANCO), StatePredicate.builder().minFerments(1).maturation(0).minDistills(2))
                    .add(DrinkType.TEQUILA.withVariation(Variation.REPOSADO), StatePredicate.builder().minFerments(1).minMaturity(1).minDistills(2))
            )
            .color(0x779bab62)
            .viscocity(1)
    );

    DrugFluid COFFEE = new CoffeeFluid(Psychedelicraft.id("coffee"), new DrugFluid.Settings()
            .drinkable()
            .appearance(FluidAppearance.COFFEE)
            .color(0xffa77d55)
    );
    DrugFluid COCA_TEA = new CocaTeaFluid(Psychedelicraft.id("coca_tea"), new DrugFluid.Settings()
            .drinkable()
            .appearance(FluidAppearance.TEA)
            .influence(new DrugInfluence(DrugType.COCAINE, DrugInfluence.DelayType.METABOLISED, 0.005, 0.002, 0.2))
            .color(0x44787a36)
    );
    DrugFluid CANNABIS_TEA = new DrugFluid(Psychedelicraft.id("cannabis_tea"), new DrugFluid.Settings()
            .drinkable()
            .appearance(FluidAppearance.TEA)
            .influence(new DrugInfluence(DrugType.CANNABIS, DrugInfluence.DelayType.METABOLISED, 0.005, 0.002, 0.25))
            .color(0x446d6f3c)
    );
    DrugFluid PEYOTE_JUICE = new DrugFluid(Psychedelicraft.id("peyote_juice"), new DrugFluid.Settings()
            .drinkable()
            .influence(new DrugInfluence(DrugType.PEYOTE, DrugInfluence.DelayType.INGESTED, 0.005, 0.003, 2))
            .appearance(FluidAppearance.TEA)
            .color(0x779bab62)
            .viscocity(2)
    );
    DrugFluid KAVA = new DrugFluid(Psychedelicraft.id("kava"), new DrugFluid.Settings()
            .drinkable()
            .influence(new DrugInfluence(DrugType.KAVA, DrugInfluence.DelayType.INHALED, 0.005, 0.003, 2))
            .color(0x779bab62)
    );

    DrugFluid ETHANOL = new EthanolFluid(Psychedelicraft.id("ethanol"), chemicalSolution(DrugType.ALCOHOL));
    DrugFluid ACID = new DrugFluid(Psychedelicraft.id("acid"), chemicalSolution(DrugType.LSD));
    DrugFluid ATROPINE = new DrugFluid(Psychedelicraft.id("atropine"), chemicalSolution(DrugType.ATROPINE));
    DrugFluid COCAINE = new DrugFluid(Psychedelicraft.id("cocaine"), chemicalSolution(DrugType.COCAINE));
    DrugFluid CAFFEINE = new DrugFluid(Psychedelicraft.id("caffeine"), new DrugFluid.Settings()
            .injectable()
            .appearance(FluidAppearance.CLEAR)
            .influence(new DrugInfluence(DrugType.CAFFEINE, DrugInfluence.DelayType.IMMEDIATE, 0.005, 0.01, 85))
            .color(0x66eee2d3)
    );
    DrugFluid BATH_SALTS = new DrugFluid(Psychedelicraft.id("bath_salts"), new DrugFluid.Settings()
            .injectable()
            .appearance(FluidAppearance.CLEAR)
            .influence(new DrugInfluence(DrugType.BATH_SALTS, DrugInfluence.DelayType.IMMEDIATE, 0.005, 0.01, 50))
            .color(0x2233f4f8)
    );

    ChemicalExtractFluid MORNING_GLORY_EXTRACT = new ChemicalExtractFluid(Psychedelicraft.id("morning_glory_extract"), new DrugFluid.Settings().color(0x66eee2d3), DrugType.LSD, ACID);
    ChemicalExtractFluid BELLADONA_EXTRACT = new ChemicalExtractFluid(Psychedelicraft.id("belladonna_extract"), new DrugFluid.Settings().color(0x66eee2d3), DrugType.ATROPINE, ATROPINE);
    ChemicalExtractFluid JIMSONWEED_EXTRACT = new ChemicalExtractFluid(Psychedelicraft.id("jimsonweed_extract"), new DrugFluid.Settings().color(0x66eee2d3), DrugType.ATROPINE, ATROPINE);

    SimpleFluid SLURRY = new SlurryFluid(Psychedelicraft.id("slurry"), new SimpleFluid.Settings().color(0xcc704E21).viscocity(4));

    static DrugFluid.Settings chemicalSolution(DrugType<?> type) {
        return new DrugFluid.Settings()
                .injectable()
                .appearance(FluidAppearance.CLEAR)
                .influence(new DrugInfluence(type, DrugInfluence.DelayType.IMMEDIATE, 0.05, 0.01, 50))
                .color(0x44e8f4f8);
    }

    static void bootstrap() { }
}

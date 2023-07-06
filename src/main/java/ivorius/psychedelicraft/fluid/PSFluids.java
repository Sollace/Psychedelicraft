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
import net.minecraft.predicate.NumberRange.IntRange;

/**
 * Created by lukas on 22.10.14.
 */
public interface PSFluids {
    SimpleFluid EMPTY = new SimpleFluid(SimpleFluid.EMPTY_KEY, new Settings().color(0xFFFFFFFF), true);
    AlcoholicFluid WHEAT_HOP = new AlcoholicFluid(Psychedelicraft.id("wheat_hop"), new AlcoholicFluid.Settings()
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoWheatHop)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.WORT.withVariation(Variation.BITTER), StatePredicate.Standard.BASE)
                    .add(DrinkType.VINEGAR.withName("beer_vinegar").withVariation(Variation.BITTER), StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.BEER.withAppearance(FluidAppearance.CLEAR), StatePredicate.Standard.DISTILLED)
                    .add(DrinkType.BEER, StatePredicate.Standard.MATURED)
                    .add(DrinkType.HALF_WASH.withVariation(Variation.BITTER), StatePredicate.Standard.FERMENTED_1)
                    .add(DrinkType.BEER.withVariation("green").withAppearance(FluidAppearance.RUM_MATURE), StatePredicate.Standard.FERMENTED_2))
            .color(0xaafeaa08)
    );
    AlcoholicFluid WHEAT = new AlcoholicFluid(Psychedelicraft.id("wheat"), new AlcoholicFluid.Settings()
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoWheat)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.WORT, StatePredicate.Standard.BASE)
                    .add(DrinkType.VINEGAR.withName("beer_vinegar"), StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.WHISKEY.withName("wheat_whiskey"), StatePredicate.builder().matured().distilled())
                    .add(DrinkType.VODKA, StatePredicate.Standard.DISTILLED)
                    .add(DrinkType.WASH, StatePredicate.Standard.MATURED)
                    .add(DrinkType.HALF_WASH, StatePredicate.Standard.FERMENTED_1)
                    .add(DrinkType.WASH, StatePredicate.Standard.FERMENTED_2))
            .color(0xaafeaa08)
    );
    AlcoholicFluid POTATO = new AlcoholicFluid(Psychedelicraft.id("potato"), new AlcoholicFluid.Settings()
            .alcohol(0.45, 1.9, 0.15)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoPotato)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.WORT, StatePredicate.Standard.BASE)
                    .add(DrinkType.VINEGAR, StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.POTEEN.withAppearance(FluidAppearance.RUM_SEMI_MATURE), StatePredicate.builder().matured().distilled())
                    .add(DrinkType.VODKA, StatePredicate.Standard.DISTILLED)
                    .add(DrinkType.BEER, StatePredicate.Standard.MATURED)
                    .add(DrinkType.HALF_WASH, StatePredicate.Standard.FERMENTED_1)
                    .add(DrinkType.BEER.withAppearance(FluidAppearance.RUM_MATURE), StatePredicate.Standard.FERMENTED_2))
            .color(0xaafeaa08)
            .viscocity(2)
    );
    AlcoholicFluid TOMATO = new AlcoholicFluid(Psychedelicraft.id("tomato"), new AlcoholicFluid.Settings()
            .alcohol(0.15, 0.9, 0.05)
            .distilledColor(0xaaff0000)
            .matureColor(0xaaff3300)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoTomato)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.KETCHUP.withExtraDrug(new DrugInfluence(DrugType.SUGAR, 20, 0.003, 0.002, 0.3)), StatePredicate.builder().unfermented().maturation(IntRange.between(1, 4)).vinegar())
                    .add(DrinkType.WHISKEY.withAppearance(FluidAppearance.RUM_SEMI_MATURE), StatePredicate.builder().fermented().maturation(IntRange.atLeast(6)).distillation(IntRange.atLeast(3)))
                    .add(DrinkType.MEAD, StatePredicate.builder().fermented().maturation(IntRange.atLeast(5)).undistilled())
                    .add(DrinkType.BEER, StatePredicate.builder().fermented().distillation(IntRange.exactly(1)))
                    .add(DrinkType.VINEGAR, StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.VODKA, StatePredicate.Standard.DISTILLED)
                    .add(DrinkType.JUICE.withAppearance(FluidAppearance.TOMATO_JUICE), StatePredicate.Standard.ANY))
            .color(0xaaffaa08)
            .viscocity(1)
    );
    AlcoholicFluid RED_GRAPES = new AlcoholicFluid(Psychedelicraft.id("red_grapes"), new AlcoholicFluid.Settings()
            .alcohol(0.55, 1.7, 0.2)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoRedGrapes)
            .distilledColor(0x993f0822)
            .matureColor(0xee3f0822)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.JUICE.withAppearance(FluidAppearance.WINE), StatePredicate.Standard.BASE)
                    .add(DrinkType.VINEGAR, StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.BRANDY, StatePredicate.Standard.DISTILLED)
                    .add(DrinkType.VINEGAR, StatePredicate.builder().maturation(IntRange.atLeast(16)))
                    .add(DrinkType.WINE.withVariation(Variation.WELL_AGED), StatePredicate.builder().maturation(IntRange.atLeast(14)))
                    .add(DrinkType.WINE.withVariation(Variation.AGED), StatePredicate.builder().maturation(IntRange.atLeast(8)))
                    .add(DrinkType.WINE.withVariation(Variation.SLIGHTLY_AGED), StatePredicate.builder().maturation(IntRange.atLeast(4)))
                    .add(DrinkType.WINE.withVariation(Variation.YOUNG), StatePredicate.Standard.MATURED)
                    .add(DrinkType.HALF_WASH.withVariation(Variation.WINE).withAppearance(FluidAppearance.WINE), StatePredicate.Standard.FERMENTED_1)
                    .add(DrinkType.WASH.withVariation(Variation.WINE).withAppearance(FluidAppearance.WINE), StatePredicate.Standard.FERMENTED_2))
            .color(0xaafeaa08)
    );
    AlcoholicFluid RICE = new AlcoholicFluid(Psychedelicraft.id("rice"), new AlcoholicFluid.Settings()
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoRice)
            .matureColor(0x88D6BC90)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.WORT.withAppearance(FluidAppearance.RICE_WINE), StatePredicate.Standard.BASE)
                    .add(DrinkType.VINEGAR, StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.BRANDY.withAppearance(FluidAppearance.CLEAR), StatePredicate.Standard.DISTILLED)
                    .add(DrinkType.WINE.withAppearance(FluidAppearance.CLEAR), StatePredicate.Standard.MATURED)
                    .add(DrinkType.HALF_WASH.withAppearance(FluidAppearance.CLEAR), StatePredicate.Standard.FERMENTED_1)
                    .add(DrinkType.WINE.withVariation(Variation.YOUNG).withAppearance(FluidAppearance.CLEAR), StatePredicate.Standard.FERMENTED_2))
            .color(0xeecac4b2)
    );
    AlcoholicFluid JUNIPER = new AlcoholicFluid(Psychedelicraft.id("juniper"), new AlcoholicFluid.Settings()
            .alcohol(0.4, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoJuniper)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.WORT.withAppearance(FluidAppearance.SLURRY), StatePredicate.Standard.BASE)
                    .add(DrinkType.VINEGAR, StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.GIN, StatePredicate.Standard.DISTILLED)
                    .add(DrinkType.WASH, StatePredicate.Standard.MATURED)
                    .add(DrinkType.HALF_WASH, StatePredicate.Standard.FERMENTED_1)
                    .add(DrinkType.WASH.withVariation(Variation.YOUNG), StatePredicate.Standard.FERMENTED_2))
            .color(0xcc704E21)
    );
    AlcoholicFluid HONEY = new AlcoholicFluid(Psychedelicraft.id("honey"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x99e9ae3b)
            .matureColor(0xaaD1984D)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoHoney)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.WORT.withAppearance(FluidAppearance.MEAD), StatePredicate.Standard.BASE)
                    .add(DrinkType.VINEGAR.withAppearance(FluidAppearance.MEAD), StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.BRANDY.withAppearance(FluidAppearance.MEAD), StatePredicate.Standard.DISTILLED)
                    .add(DrinkType.MEAD, StatePredicate.Standard.MATURED)
                    .add(DrinkType.HALF_WASH.withAppearance(FluidAppearance.MEAD), StatePredicate.Standard.FERMENTED_1)
                    .add(DrinkType.MEAD.withVariation(Variation.YOUNG), StatePredicate.Standard.FERMENTED_2))
            .color(0xbbe9ae3b)
            .viscocity(5)
    );
    AlcoholicFluid SUGAR_CANE = new AlcoholicFluid(Psychedelicraft.id("sugar_cane"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.WORT.withAppearance(FluidAppearance.CLEAR), StatePredicate.Standard.BASE)
                    .add(DrinkType.VINEGAR, StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.RUM.withVariation(Variation.YOUNG), StatePredicate.builder().distilled().unmatured())
                    .add(DrinkType.RUM, StatePredicate.Standard.DISTILLED)
                    .add(DrinkType.BASI, StatePredicate.Standard.MATURED)
                    .add(DrinkType.HALF_WASH, StatePredicate.Standard.FERMENTED_1)
                    .add(DrinkType.BASI.withVariation(Variation.YOUNG), StatePredicate.Standard.FERMENTED_2))
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoSugarCane)
            .color(0xaafeaa08)
    );
    AlcoholicFluid CORN = new AlcoholicFluid(Psychedelicraft.id("corn"), new AlcoholicFluid.Settings()
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoCorn)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.WORT.withAppearance(FluidAppearance.BEER), StatePredicate.Standard.BASE)
                    .add(DrinkType.VINEGAR, StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.VODKA, StatePredicate.builder().unmatured().distilled())
                    .add(DrinkType.WHISKEY, StatePredicate.builder().matured().distilled())
                    .add(DrinkType.BEER, StatePredicate.Standard.MATURED)
                    .add(DrinkType.HALF_WASH, StatePredicate.Standard.FERMENTED_1)
                    .add(DrinkType.BEER.withVariation(Variation.GREEN), StatePredicate.Standard.FERMENTED_2))
            .color(0xaafeaa08)
    );
    AlcoholicFluid APPLE = new AlcoholicFluid(Psychedelicraft.id("apple"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x66EDC13B)
            .matureColor(0x88EDC13B)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.CIDER.withVariation(Variation.SWEET), StatePredicate.Standard.BASE)
                    .add(DrinkType.VINEGAR, StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.BRANDY.withAppearance(FluidAppearance.CIDER), StatePredicate.Standard.DISTILLED)
                    .add(DrinkType.CIDER, StatePredicate.Standard.MATURED)
                    .add(DrinkType.CIDER.withVariation(Variation.HALF_SWEET), StatePredicate.Standard.FERMENTED_1)
                    .add(DrinkType.CIDER.withVariation(Variation.HARD), StatePredicate.Standard.FERMENTED_2))
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoApple)
            .color(0x99EDC13B)
    );
    AlcoholicFluid PINEAPPLE = new AlcoholicFluid(Psychedelicraft.id("pineapple"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x66EDC13B)
            .matureColor(0x88EDC13B)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.JUICE.withAppearance(FluidAppearance.CIDER), StatePredicate.Standard.BASE)
                    .add(DrinkType.VINEGAR, StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.BRANDY.withAppearance(FluidAppearance.CIDER), StatePredicate.Standard.DISTILLED)
                    .add(DrinkType.WINE.withAppearance(FluidAppearance.CIDER), StatePredicate.Standard.MATURED)
                    .add(DrinkType.HALF_WASH.withAppearance(FluidAppearance.CIDER), StatePredicate.Standard.FERMENTED_1)
                    .add(DrinkType.WINE.withVariation("young").withAppearance(FluidAppearance.CIDER), StatePredicate.Standard.FERMENTED_2))
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoPineapple)
            .color(0x99EDC13B)
            .viscocity(2)
    );
    AlcoholicFluid BANANA = new AlcoholicFluid(Psychedelicraft.id("banana"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x99e9ae3b)
            .matureColor(0xaaD1984D)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.JUICE.withAppearance(FluidAppearance.MEAD), StatePredicate.Standard.BASE)
                    .add(DrinkType.VINEGAR, StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.BRANDY.withAppearance(FluidAppearance.MEAD), StatePredicate.Standard.DISTILLED)
                    .add(DrinkType.BEER.withAppearance(FluidAppearance.MEAD), StatePredicate.Standard.MATURED)
                    .add(DrinkType.HALF_WASH.withAppearance(FluidAppearance.MEAD), StatePredicate.Standard.FERMENTED_1)
                    .add(DrinkType.BEER.withAppearance(FluidAppearance.MEAD), StatePredicate.Standard.FERMENTED_2))
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoBanana)
            .color(0xbbe9ae3b)
            .viscocity(3)
    );
    AlcoholicFluid MILK = new AlcoholicFluid(Psychedelicraft.id("milk"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x77cac4b2)
            .matureColor(0x88D6BC90)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.WORT.withAppearance(FluidAppearance.RICE_WINE), StatePredicate.Standard.BASE)
                    .add(DrinkType.VINEGAR.withAppearance(FluidAppearance.RICE_WINE), StatePredicate.Standard.VINEGAR)
                    .add(DrinkType.ARKHI.withAppearance(FluidAppearance.RICE_WINE), StatePredicate.Standard.DISTILLED)
                    .add(DrinkType.BLAAND.withAppearance(FluidAppearance.RICE_WINE), StatePredicate.Standard.MATURED)
                    .add(DrinkType.HALF_WASH.withAppearance(FluidAppearance.RICE_WINE), StatePredicate.Standard.FERMENTED_1)
                    .add(DrinkType.BLAAND.withAppearance(FluidAppearance.RICE_WINE), StatePredicate.Standard.FERMENTED_2))
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoMilk)
            .color(0x88ffffff)
            .viscocity(2)
    );
    AlcoholicFluid AGAVE = new AgaveFluid(Psychedelicraft.id("agave"), new AlcoholicFluid.Settings()
            .alcohol(0.15, 1.5, 0.05)
            .distilledColor(0x779beb62)
            .matureColor(0x779beb62)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoAgave)
            .variants(DrinkTypes.builder()
                    .add(DrinkType.MEZCAL, StatePredicate.builder().fermented().unmatured().distillation(IntRange.exactly(1)))
                    .add(DrinkType.TEQUILA.withVariation(Variation.BLANCO), StatePredicate.builder().fermented().unmatured().distillation(IntRange.atLeast(2)))
                    .add(DrinkType.TEQUILA.withVariation(Variation.REPOSADO), StatePredicate.builder().fermented().matured().distillation(IntRange.atLeast(2)))
                    .add(DrinkType.JUICE, StatePredicate.Standard.ANY))
            .color(0x779bab62)
            .viscocity(1)
    );

    DrugFluid COFFEE = new CoffeeFluid(Psychedelicraft.id("coffee"), new DrugFluid.Settings()
            .drinkable()
            .appearance(FluidAppearance.COFFEE)
            .color(0xffa77d55)
    );
    DrugFluid COCA_TEA = new DrugFluid(Psychedelicraft.id("coca_tea"), new DrugFluid.Settings()
            .drinkable()
            .appearance(FluidAppearance.TEA)
            .influence(new DrugInfluence(DrugType.COCAINE, 60, 0.005, 0.002, 0.2f))
            .color(0x44787a36)
    );
    DrugFluid CANNABIS_TEA = new DrugFluid(Psychedelicraft.id("cannabis_tea"), new DrugFluid.Settings()
            .drinkable()
            .appearance(FluidAppearance.TEA)
            .influence(new DrugInfluence(DrugType.CANNABIS, 60, 0.005, 0.002, 0.25f))
            .color(0x446d6f3c)
    );
    DrugFluid PEYOTE_JUICE = new DrugFluid(Psychedelicraft.id("peyote_juice"), new DrugFluid.Settings()
            .drinkable()
            .influence(new DrugInfluence(DrugType.PEYOTE, 15, 0.005, 0.003, 2.0f))
            .appearance(FluidAppearance.TEA)
            .color(0x779bab62)
            .viscocity(2)
    );
    DrugFluid KAVA = new DrugFluid(Psychedelicraft.id("kava"), new DrugFluid.Settings()
            .drinkable()
            .influence(new DrugInfluence(DrugType.KAVA, 20, 0.005, 0.003, 2.0f))
            .color(0x779bab62)
    );

    DrugFluid COCAINE = new DrugFluid(Psychedelicraft.id("cocaine"), new DrugFluid.Settings()
            .injectable()
            .appearance(FluidAppearance.CLEAR)
            .influence(new DrugInfluence(DrugType.COCAINE, 0, 0.005, 0.01, 50.0f))
            .color(0x44e8f4f8)
    );
    DrugFluid CAFFEINE = new DrugFluid(Psychedelicraft.id("caffeine"), new DrugFluid.Settings()
            .injectable()
            .appearance(FluidAppearance.CLEAR)
            .influence(new DrugInfluence(DrugType.CAFFEINE, 0, 0.005, 0.01, 85.0f))
            .color(0x66eee2d3)
    );
    DrugFluid BATH_SALTS = new DrugFluid(Psychedelicraft.id("bath_salts"), new DrugFluid.Settings()
            .injectable()
            .appearance(FluidAppearance.CLEAR)
            .influence(new DrugInfluence(DrugType.BATH_SALTS, 0, 0.005, 0.01, 50.0f))
            .color(0x2233f4f8)
    );
    ChemicalExtractFluid MORNING_GLORY_EXTRACT = new ChemicalExtractFluid(Psychedelicraft.id("morning_glory_extract"), new DrugFluid.Settings()
            .injectable()
            .color(0x66eee2d3), DrugType.LSD
    );
    ChemicalExtractFluid BELLADONA_EXTRACT = new ChemicalExtractFluid(Psychedelicraft.id("belladonna_extract"), new DrugFluid.Settings()
            .color(0x66eee2d3), DrugType.ATROPINE
    );
    ChemicalExtractFluid JIMSONWEED_EXTRACT = new ChemicalExtractFluid(Psychedelicraft.id("jimsonweed_extract"), new DrugFluid.Settings()
            .color(0x66eee2d3), DrugType.ATROPINE
    );

    SimpleFluid SLURRY = new SlurryFluid(Psychedelicraft.id("slurry"), new SimpleFluid.Settings().color(0xcc704E21).viscocity(4));

    static void bootstrap() {}
}

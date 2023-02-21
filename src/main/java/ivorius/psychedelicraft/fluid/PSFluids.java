/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.fluid.SimpleFluid.Settings;

/**
 * Created by lukas on 22.10.14.
 */
public interface PSFluids {
    SimpleFluid EMPTY = new SimpleFluid(SimpleFluid.EMPTY_KEY, new Settings().color(0xFFFFFFFF), true);
    AlcoholicFluid WHEAT_HOP = new AlcoholicFluid(Psychedelicraft.id("wheat_hop"), new AlcoholicFluid.Settings()
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoWheatHop)
            .variants(DrinkTypes.BEER, DrinkTypes.BEER_ICONS)
            .color(0xaafeaa08)
    );
    AlcoholicFluid WHEAT = new AlcoholicFluid(Psychedelicraft.id("wheat"), new AlcoholicFluid.Settings()
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoWheat)
            .variants(DrinkTypes.VODKA_WHISKEY, DrinkTypes.BEER_ICONS)
            .color(0xaafeaa08)
    );
    AlcoholicFluid POTATO = new AlcoholicFluid(Psychedelicraft.id("potato"), new AlcoholicFluid.Settings()
            .alcohol(0.45, 1.9, 0.15)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoPotato)
            .variants(DrinkTypes.BEER_VODKA_WHISKEY, DrinkTypes.BEER_ICONS)
            .color(0xaafeaa08)
            .viscocity(2)
    );
    AlcoholicFluid RED_GRAPES = new AlcoholicFluid(Psychedelicraft.id("red_grapes"), new AlcoholicFluid.Settings()
            .alcohol(0.55, 1.7, 0.2)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoRedGrapes)
            .distilledColor(0x993f0822)
            .matureColor(0xee3f0822)
            .variants(DrinkTypes.RED_WINE, DrinkTypes.only("wine"))
            .color(0xaafeaa08)
    );
    AlcoholicFluid RICE = new AlcoholicFluid(Psychedelicraft.id("rice"), new AlcoholicFluid.Settings()
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoRice)
            .matureColor(0x88D6BC90)
            .variants(DrinkTypes.RICE_WINE, DrinkTypes.clear("rice_wine"))
            .color(0xeecac4b2)
    );
    AlcoholicFluid JUNIPER = new AlcoholicFluid(Psychedelicraft.id("juniper"), new AlcoholicFluid.Settings()
            .alcohol(0.4, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoJuniper)
            .variants(DrinkTypes.JENEVER, DrinkTypes.maturable("slurry"))
            .color(0xcc704E21)
    );
    AlcoholicFluid HONEY = new AlcoholicFluid(Psychedelicraft.id("honey"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x99e9ae3b)
            .matureColor(0xaaD1984D)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoHoney)
            .variants(DrinkTypes.MEAD, DrinkTypes.only("mead"))
            .color(0xbbe9ae3b)
            .viscocity(5)
    );
    AlcoholicFluid SUGAR_CANE = new AlcoholicFluid(Psychedelicraft.id("sugar_cane"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .variants(DrinkTypes.RUM, DrinkTypes.maturable("clear"))
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoSugarCane)
            .color(0xaafeaa08)
    );
    AlcoholicFluid CORN = new AlcoholicFluid(Psychedelicraft.id("corn"), new AlcoholicFluid.Settings()
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoCorn)
            .variants(DrinkTypes.BEER_VODKA_WHISKEY, DrinkTypes.BEER_ICONS)
            .color(0xaafeaa08)
    );
    AlcoholicFluid APPLE = new AlcoholicFluid(Psychedelicraft.id("apple"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x66EDC13B)
            .matureColor(0x88EDC13B)
            .variants(DrinkTypes.NONE, DrinkTypes.only("cider"))
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoApple)
            .color(0x99EDC13B)
    );
    AlcoholicFluid PINEAPPLE = new AlcoholicFluid(Psychedelicraft.id("pineapple"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x66EDC13B)
            .matureColor(0x88EDC13B)
            .variants(DrinkTypes.NONE, DrinkTypes.only("cider"))
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoPineapple)
            .color(0x99EDC13B)
            .viscocity(2)
    );
    AlcoholicFluid BANANA = new AlcoholicFluid(Psychedelicraft.id("banana"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x99e9ae3b)
            .matureColor(0xaaD1984D)
            .variants(DrinkTypes.NONE, DrinkTypes.only("mead"))
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoBanana)
            .color(0xbbe9ae3b)
            .viscocity(3)
    );
    AlcoholicFluid MILK = new AlcoholicFluid(Psychedelicraft.id("milk"), new AlcoholicFluid.Settings()
            .alcohol(0.35, 1.7, 0.1)
            //.distilledColor(0x77cac4b2)
            .matureColor(0x88D6BC90)
            .variants(DrinkTypes.NONE, DrinkTypes.only("rice_wine"))
            .tickRate(() -> Psychedelicraft.getConfig().balancing.fluidAttributes.alcInfoMilk)
            .color(0xeecac4b2)
            .viscocity(2)
            // TODO: (Sollace) Color for milk was set twice. Second time set to 0x77cac4b2 Should it be the distilled colour?
    );

    DrugFluid COFFEE = new CoffeeFluid(Psychedelicraft.id("coffee"), new DrugFluid.Settings()
            .drinkable()
            .appearance(DrinkTypes.Icons.of("coffee"))
            .color(0xffa77d55)
    );
    DrugFluid COCA_TEA = new DrugFluid(Psychedelicraft.id("coca_tea"), new DrugFluid.Settings()
            .drinkable()
            .appearance(DrinkTypes.Icons.of("tea"))
            .influence(new DrugInfluence(DrugType.COCAINE, 60, 0.005, 0.002, 0.2f))
            .color(0x44787a36)
    );
    DrugFluid CANNABIS_TEA = new DrugFluid(Psychedelicraft.id("cannabis_tea"), new DrugFluid.Settings()
            .drinkable()
            .appearance(DrinkTypes.Icons.of("tea"))
            .influence(new DrugInfluence(DrugType.CANNABIS, 60, 0.005, 0.002, 0.25f))
            .color(0x446d6f3c)
    );
    DrugFluid PEYOTE_JUICE = new DrugFluid(Psychedelicraft.id("peyote_juice"), new DrugFluid.Settings()
            .drinkable()
            .influence(new DrugInfluence(DrugType.PEYOTE, 15, 0.005, 0.003, 2.0f))
            .appearance(DrinkTypes.Icons.of("tea"))
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
            .appearance(DrinkTypes.Icons.of("clear"))
            .influence(new DrugInfluence(DrugType.COCAINE, 0, 0.005, 0.01, 50.0f))
            .color(0x44e8f4f8)
    );
    DrugFluid CAFFEINE = new DrugFluid(Psychedelicraft.id("caffeine"), new DrugFluid.Settings()
            .injectable()
            .appearance(DrinkTypes.Icons.of("clear"))
            .influence(new DrugInfluence(DrugType.CAFFEINE, 0, 0.005, 0.01, 85.0f))
            .color(0x66eee2d3)
    );
    DrugFluid BATH_SALTS = new DrugFluid(Psychedelicraft.id("bath_salts"), new DrugFluid.Settings()
            .injectable()
            .appearance(DrinkTypes.Icons.of("clear"))
            .influence(new DrugInfluence(DrugType.BATH_SALTS, 0, 0.005, 0.01, 50.0f))
            .color(0x2233f4f8)
    );
    ChemicalExtractFluid MORNING_GLORY_EXTRACT = new ChemicalExtractFluid(Psychedelicraft.id("morning_glory_extract"), new DrugFluid.Settings()
            .injectable()
            .color(0x66eee2d3)
    );
    ChemicalExtractFluid BELLADONA_EXTRACT = new ChemicalExtractFluid(Psychedelicraft.id("belladonna_extract"), new DrugFluid.Settings()
            .color(0x66eee2d3)
    );
    ChemicalExtractFluid JIMSONWEED_EXTRACT = new ChemicalExtractFluid(Psychedelicraft.id("jimsonweed_extract"), new DrugFluid.Settings()
            .color(0x66eee2d3)
    );

    SimpleFluid SLURRY = new SlurryFluid(Psychedelicraft.id("slurry"), new SimpleFluid.Settings().color(0xcc704E21).viscocity(4));

    static void bootstrap() {}
}

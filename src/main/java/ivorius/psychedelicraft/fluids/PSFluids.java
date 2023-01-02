/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluids;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entities.drugs.DrugInfluence;
import net.minecraft.predicate.NumberRange.IntRange;

/**
 * Created by lukas on 22.10.14.
 */
public interface PSFluids {
    AlcoholicFluid alcWheatHop = new AlcoholicFluid(Psychedelicraft.id("wheat_hop"), (AlcoholicFluid.Settings)new AlcoholicFluid.Settings()
            .fermentation(2)
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> PSConfig.alcInfoWheatHop)
            .types(AlcoholicDrinkTypes.BEER)
            .color(0xaafeaa08)
    );
    AlcoholicFluid alcWheat = new AlcoholicFluid(Psychedelicraft.id("wheat"), (AlcoholicFluid.Settings)new AlcoholicFluid.Settings()
            .fermentation(2)
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> PSConfig.alcInfoWheat)
            .types(AlcoholicDrinkTypes.VODKA_WHISKEY)
            .color(0xaafeaa08)
    );
    AlcoholicFluid alcPotato = new AlcoholicFluid(Psychedelicraft.id("potato"), (AlcoholicFluid.Settings)new AlcoholicFluid.Settings()
            .fermentation(2)
            .alcohol(0.45, 1.9, 0.15)
            .tickRate(() -> PSConfig.alcInfoPotato)
            .types(AlcoholicDrinkTypes.BEER_VODKA_WHISKEY)
            .color(0xaafeaa08)
    );
    AlcoholicFluid alcRedGrapes = new AlcoholicFluid(Psychedelicraft.id("red_grapes"), (AlcoholicFluid.Settings)new AlcoholicFluid.Settings()
            .fermentation(2)
            .alcohol(0.55, 1.7, 0.2)
            .tickRate(() -> PSConfig.alcInfoRedGrapes)
            .distilledColor(0x993f0822)
            .matureColor(0xee3f0822)
            .types(new AlcoholicDrinkTypes.Builder()
                    .addName("red_wine", IntRange.ANY, IntRange.ANY)
                    .build())
            .color(0xaafeaa08)
    );
    AlcoholicFluid alcRice = new AlcoholicFluid(Psychedelicraft.id("rice"), (AlcoholicFluid.Settings)new AlcoholicFluid.Settings()
            .fermentation(2)
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> PSConfig.alcInfoRice)
            .matureColor(0x88D6BC90)
            .types(new AlcoholicDrinkTypes.Builder()
                    .addName("rice_wine", IntRange.ANY, IntRange.ANY)
                    .addIcon(IntRange.ANY, IntRange.ANY, IntRange.atLeast(2), "clear_still", "clear_flow")
                    .build())
            .color(0xeecac4b2)
    );
    AlcoholicFluid alcJuniper = new AlcoholicFluid(Psychedelicraft.id("juniper"), (AlcoholicFluid.Settings)new AlcoholicFluid.Settings()
            .fermentation(2)
            .alcohol(0.4, 1.7, 0.1)
            .tickRate(() -> PSConfig.alcInfoJuniper)
            .types(AlcoholicDrinkTypes.JENEVER)
            .color(0xcc704E21)
    );
    AlcoholicFluid alcHoney = new AlcoholicFluid(Psychedelicraft.id("honey"), (AlcoholicFluid.Settings)new AlcoholicFluid.Settings()
            .fermentation(2)
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x99e9ae3b)
            .matureColor(0xaaD1984D)
            .tickRate(() -> PSConfig.alcInfoHoney)
            .types(new AlcoholicDrinkTypes.Builder()
                    .addName("mead", IntRange.ANY, IntRange.ANY)
                    .build())
            .color(0xbbe9ae3b)
    );
    AlcoholicFluid alcSugarCane = new AlcoholicFluid(Psychedelicraft.id("sugar_cane"), (AlcoholicFluid.Settings)new AlcoholicFluid.Settings()
            .fermentation(2)
            .alcohol(0.35, 1.7, 0.1)
            .tickRate(() -> PSConfig.alcInfoSugarCane)
            .types(AlcoholicDrinkTypes.RUM)
            .color(0xaafeaa08)
    );
    AlcoholicFluid alcCorn = new AlcoholicFluid(Psychedelicraft.id("corn"), (AlcoholicFluid.Settings)new AlcoholicFluid.Settings()
            .fermentation(2)
            .alcohol(0.25, 1.7, 0.1)
            .tickRate(() -> PSConfig.alcInfoCorn)
            .types(AlcoholicDrinkTypes.BEER_VODKA_WHISKEY)
            .color(0xaafeaa08)
    );
    AlcoholicFluid alcApple = new AlcoholicFluid(Psychedelicraft.id("apple"), (AlcoholicFluid.Settings)new AlcoholicFluid.Settings()
            .fermentation(2)
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x66EDC13B)
            .matureColor(0x88EDC13B)
            .tickRate(() -> PSConfig.alcInfoApple)
            .types(new AlcoholicDrinkTypes.Builder()
                    .addName("apple", IntRange.ANY, IntRange.ANY)
                    .build())
            .color(0x99EDC13B)
    );
    AlcoholicFluid alcPineapple = new AlcoholicFluid(Psychedelicraft.id("pineapple"), (AlcoholicFluid.Settings)new AlcoholicFluid.Settings()
            .fermentation(2)
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x66EDC13B)
            .matureColor(0x88EDC13B)
            .tickRate(() -> PSConfig.alcInfoPineapple)
            .types(new AlcoholicDrinkTypes.Builder()
                    .addName("pineapple", IntRange.ANY, IntRange.ANY)
                    .build())
            .color(0x99EDC13B)
    );
    AlcoholicFluid alcBanana = new AlcoholicFluid(Psychedelicraft.id("banana"), (AlcoholicFluid.Settings)new AlcoholicFluid.Settings()
            .fermentation(2)
            .alcohol(0.35, 1.7, 0.1)
            .distilledColor(0x99e9ae3b)
            .matureColor(0xaaD1984D)
            .tickRate(() -> PSConfig.alcInfoBanana)
            .types(new AlcoholicDrinkTypes.Builder()
                    .addName("banana", IntRange.ANY, IntRange.ANY)
                    .build())
            .color(0xbbe9ae3b)
    );
    AlcoholicFluid alcMilk = new AlcoholicFluid(Psychedelicraft.id("milk"), (AlcoholicFluid.Settings)new AlcoholicFluid.Settings()
            .fermentation(2)
            .alcohol(0.35, 1.7, 0.1)
            //.distilledColor(0x77cac4b2)
            .matureColor(0x88D6BC90)
            .tickRate(() -> PSConfig.alcInfoMilk)
            .types(new AlcoholicDrinkTypes.Builder()
                    .addName("milk", IntRange.ANY, IntRange.ANY)
                    .build())
            .color(0xeecac4b2)
            // TODO: Color for milk was set twice. Second time set to 0x77cac4b2 Should it be the distilled colour?
    );

    FluidDrug coffee = new FluidCoffee(Psychedelicraft.id("coffee"), (FluidDrug.Settings)new FluidDrug.Settings().drinkable().color(0xffa77d55));
    FluidDrug cocaTea = new FluidDrug(Psychedelicraft.id("coca_tea"), (FluidDrug.Settings)new FluidDrug.Settings()
            .drinkable()
            .influence(new DrugInfluence("Cocaine", 60, 0.005, 0.002, 0.2f))
            .color(0x44787a36)
    );
    FluidDrug cannabisTea = new FluidDrug(Psychedelicraft.id("cannabis_tea"), (FluidDrug.Settings)new FluidDrug.Settings()
            .drinkable()
            .influence(new DrugInfluence("Cannabis", 60, 0.005, 0.002, 0.25f))
            .color(0x446d6f3c)
    );
    FluidDrug peyoteJuice = new FluidDrug(Psychedelicraft.id("peyote_juice"), (FluidDrug.Settings)new FluidDrug.Settings()
            .drinkable()
            .influence(new DrugInfluence("Peyote", 15, 0.005, 0.003, 2.0f))
            .color(0x779bab62)
    );

    FluidDrug cocaineFluid = new FluidDrug(Psychedelicraft.id("cocaine"), (FluidDrug.Settings)new FluidDrug.Settings()
            .injectable()
            .influence(new DrugInfluence("Cocaine", 0, 0.005, 0.01, 50.0f))
            .color(0x44e8f4f8)
    );
    FluidDrug caffeineFluid = new FluidDrug(Psychedelicraft.id("caffeine"), (FluidDrug.Settings)new FluidDrug.Settings()
            .injectable()
            .influence(new DrugInfluence("Caffeine", 0, 0.005, 0.01, 85.0f))
            .color(0x66eee2d3)
    );

    SimpleFluid slurry = new SlurryFluid(Psychedelicraft.id("slurry"), new SimpleFluid.Settings().color(0xcc704E21));

    static void bootstrap() { }
}

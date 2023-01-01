/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluids;

import static ivorius.psychedelicraft.Psychedelicraft.modBase;
import static ivorius.psychedelicraft.fluids.PSFluids.*;

import ivorius.psychedelicraft.IntegerRange;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.entities.drugs.DrugInfluence;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

/**
 * Created by lukas on 22.10.14.
 */
public class PSFluids
{
    public static FluidAlcohol alcWheatHop;
    public static FluidAlcohol alcWheat;
    public static FluidAlcohol alcPotato;
    public static FluidAlcohol alcRedGrapes;
    public static FluidAlcohol alcRice;
    public static FluidAlcohol alcJuniper;
    public static FluidAlcohol alcHoney;
    public static FluidAlcohol alcSugarCane;
    public static FluidAlcohol alcCorn;
    public static FluidAlcohol alcApple;
    public static FluidAlcohol alcPineapple;
    public static FluidAlcohol alcBanana;
    public static FluidAlcohol alcMilk;

    public static FluidDrug coffee;
    public static FluidDrug cocaTea;
    public static FluidDrug cannabisTea;
    public static FluidDrug peyoteJuice;

    public static FluidDrug cocaineFluid;
    public static FluidDrug caffeineFluid;

    public static FluidSimple slurry;

    public static ItemStack filledStack(IFluidContainerItem container, FluidStack fluidStack)
    {
        ItemStack stack = new ItemStack((Item) container);
        container.fill(stack, fluidStack, true);
        return stack;
    }

    public static boolean containsFluid(ItemStack stack, Fluid fluid)
    {
        FluidStack contained = getFluid(stack);
        return contained != null && contained.getFluid() == fluid;
    }

    public static FluidStack getFluid(ItemStack stack)
    {
        return stack.getItem() instanceof IFluidContainerItem
                ? ((IFluidContainerItem) stack.getItem()).drain(stack, ((IFluidContainerItem) stack.getItem()).getCapacity(stack), false)
                : null;
    }

    public static void bootstrap() {
        slurry = new FluidSlurry("psc_slurry");
        slurry.setColor(0xcc704E21);
        slurry.setStillIconName(modBase + "slurry_still");
        slurry.setFlowingIconName(modBase + "slurry_flow");

        alcWheatHop = new FluidAlcohol("psc_wheat_hop", 2, 0.25, 1.7, 0.1, PSConfig.alcInfoWheatHop);
        alcWheatHop.addName(modBase + "drinkBeer", new IntegerRange(0, -1), new IntegerRange(0, -1));
        alcWheatHop.setColor(0xaafeaa08);
        alcWheatHop.setStillIconName(modBase + "beer_still");
        alcWheatHop.setFlowingIconName(modBase + "beer_flow");
        alcWheatHop.addIcon(new IntegerRange(-1, -1), new IntegerRange(0, 3), new IntegerRange(2, -1), modBase + "clear_still", modBase + "clear_flow");
        alcWheatHop.addIcon(new IntegerRange(-1, -1), new IntegerRange(4, 13), new IntegerRange(0, -1), modBase + "rum_semi_mature_still", modBase + "rum_semi_mature_flow");
        alcWheatHop.addIcon(new IntegerRange(-1, -1), new IntegerRange(14, -1), new IntegerRange(0, -1), modBase + "rum_mature_still", modBase + "rum_mature_flow");

        alcWheat = new FluidAlcohol("psc_wheat", 2, 0.25, 1.7, 0.1, PSConfig.alcInfoWheat);
        alcWheat.addName(modBase + "drinkVodka", new IntegerRange(0, 0), new IntegerRange(1, -1));
        alcWheat.addName(modBase + "drinkWhisky", new IntegerRange(0, -1), new IntegerRange(1, -1));
        alcWheat.setColor(0xaafeaa08);
        alcWheat.setStillIconName(modBase + "beer_still");
        alcWheat.setFlowingIconName(modBase + "beer_flow");
        alcWheat.addIcon(new IntegerRange(-1, -1), new IntegerRange(0, 3), new IntegerRange(2, -1), modBase + "clear_still", modBase + "clear_flow");
        alcWheat.addIcon(new IntegerRange(-1, -1), new IntegerRange(4, 13), new IntegerRange(0, -1), modBase + "rum_semi_mature_still", modBase + "rum_semi_mature_flow");
        alcWheat.addIcon(new IntegerRange(-1, -1), new IntegerRange(14, -1), new IntegerRange(0, -1), modBase + "rum_mature_still", modBase + "rum_mature_flow");

        alcCorn = new FluidAlcohol("psc_corn", 2, 0.25, 1.7, 0.1, PSConfig.alcInfoCorn);
        alcCorn.addName(modBase + "drinkBeer", new IntegerRange(0, -1), new IntegerRange(0, 0));
        alcCorn.addName(modBase + "drinkVodka", new IntegerRange(0, 0), new IntegerRange(1, -1));
        alcCorn.addName(modBase + "drinkWhisky", new IntegerRange(1, -1), new IntegerRange(1, -1));
        alcCorn.setColor(0xaafeaa08);
        alcCorn.setStillIconName(modBase + "beer_still");
        alcCorn.setFlowingIconName(modBase + "beer_flow");
        alcCorn.addIcon(new IntegerRange(-1, -1), new IntegerRange(0, 3), new IntegerRange(2, -1), modBase + "clear_still", modBase + "clear_flow");
        alcCorn.addIcon(new IntegerRange(-1, -1), new IntegerRange(4, 13), new IntegerRange(0, -1), modBase + "rum_semi_mature_still", modBase + "rum_semi_mature_flow");
        alcCorn.addIcon(new IntegerRange(-1, -1), new IntegerRange(14, -1), new IntegerRange(0, -1), modBase + "rum_mature_still", modBase + "rum_mature_flow");

        alcPotato = new FluidAlcohol("psc_potato", 2, 0.45, 1.9, 0.15, PSConfig.alcInfoPotato);
        alcPotato.addName(modBase + "drinkBeer", new IntegerRange(0, -1), new IntegerRange(0, 0));
        alcPotato.addName(modBase + "drinkVodka", new IntegerRange(0, 0), new IntegerRange(1, -1));
        alcPotato.addName(modBase + "drinkWhisky", new IntegerRange(0, -1), new IntegerRange(1, -1));
        alcPotato.setColor(0xaafeaa08);
        alcPotato.setStillIconName(modBase + "beer_still");
        alcPotato.setFlowingIconName(modBase + "beer_flow");
        alcPotato.addIcon(new IntegerRange(-1, -1), new IntegerRange(0, 3), new IntegerRange(2, -1), modBase + "clear_still", modBase + "clear_flow");
        alcPotato.addIcon(new IntegerRange(-1, -1), new IntegerRange(4, 13), new IntegerRange(0, -1), modBase + "rum_semi_mature_still", modBase + "rum_semi_mature_flow");
        alcPotato.addIcon(new IntegerRange(-1, -1), new IntegerRange(14, -1), new IntegerRange(0, -1), modBase + "rum_mature_still", modBase + "rum_mature_flow");

        alcRedGrapes = new FluidAlcohol("psc_red_grapes", 2, 0.55, 1.7, 0.2, PSConfig.alcInfoRedGrapes);
        alcRedGrapes.addName(modBase + "drinkWine", new IntegerRange(0, -1), new IntegerRange(0, -1));
        alcRedGrapes.setColor(0xee3f0822);
        alcRedGrapes.setDistilledColor(0x993f0822);
        alcRedGrapes.setMatureColor(0xee3f0822);
        alcRedGrapes.setStillIconName(modBase + "wine_still");
        alcRedGrapes.setFlowingIconName(modBase + "wine_flow");

        alcRice = new FluidAlcohol("psc_rice", 2, 0.25, 1.7, 0.1, PSConfig.alcInfoRice);
        alcRice.addName(modBase + "drinkRiceWine", new IntegerRange(0, -1), new IntegerRange(0, -1));
        alcRice.setColor(0xeecac4b2);
        alcRice.setMatureColor(0x88D6BC90);
        alcRice.setStillIconName(modBase + "rice_wine_still");
        alcRice.setFlowingIconName(modBase + "rice_wine_flow");
        alcRice.addIcon(new IntegerRange(-1, -1), new IntegerRange(0, -1), new IntegerRange(2, -1), modBase + "clear_still", modBase + "clear_flow");

        alcHoney = new FluidAlcohol("psc_honey", 2, 0.35, 1.7, 0.1, PSConfig.alcInfoHoney);
        alcHoney.addName(modBase + "drinkMead", new IntegerRange(0, -1), new IntegerRange(0, -1));
        alcHoney.setColor(0xbbe9ae3b);
        alcHoney.setDistilledColor(0x99e9ae3b);
        alcHoney.setMatureColor(0xaaD1984D);
        alcHoney.setStillIconName(modBase + "mead_still");
        alcHoney.setFlowingIconName(modBase + "mead_flow");

        alcJuniper = new FluidAlcohol("psc_juniper", 2, 0.4, 1.7, 0.1, PSConfig.alcInfoJuniper);
        alcJuniper.addName(modBase + "drinkJenever", new IntegerRange(0, -1), new IntegerRange(0, -1));
        alcJuniper.setColor(0xcc704E21);
        alcJuniper.addIcon(new IntegerRange(-1, -1), new IntegerRange(0, 3), new IntegerRange(2, -1), modBase + "clear_still", modBase + "clear_flow");
        alcJuniper.addIcon(new IntegerRange(-1, -1), new IntegerRange(4, 13), new IntegerRange(0, -1), modBase + "rum_semi_mature_still", modBase + "rum_semi_mature_flow");
        alcJuniper.addIcon(new IntegerRange(-1, -1), new IntegerRange(14, -1), new IntegerRange(0, -1), modBase + "rum_mature_still", modBase + "rum_mature_flow");

        alcSugarCane = new FluidAlcohol("psc_sugar_cane", 2, 0.35, 1.7, 0.1, PSConfig.alcInfoSugarCane);
        alcSugarCane.addName(modBase + "drinkRum", new IntegerRange(0, -1), new IntegerRange(0, -1));
        alcSugarCane.setColor(0xcc704E21);
        alcSugarCane.setStillIconName(modBase + "clear_still");
        alcSugarCane.setFlowingIconName(modBase + "clear_flow");
        alcSugarCane.addIcon(new IntegerRange(-1, -1), new IntegerRange(0, 3), new IntegerRange(2, -1), modBase + "clear_still", modBase + "clear_flow");
        alcSugarCane.addIcon(new IntegerRange(-1, -1), new IntegerRange(4, 13), new IntegerRange(0, -1), modBase + "rum_semi_mature_still", modBase + "rum_semi_mature_flow");
        alcSugarCane.addIcon(new IntegerRange(-1, -1), new IntegerRange(14, -1), new IntegerRange(0, -1), modBase + "rum_mature_still", modBase + "rum_mature_flow");

        alcApple = new FluidAlcohol("psc_apple", 2, 0.35, 1.7, 0.1, PSConfig.alcInfoApple);
        alcApple.addName(modBase + "drinkApple", new IntegerRange(0, -1), new IntegerRange(0, -1));
        alcApple.setColor(0x99EDC13B);
        alcApple.setDistilledColor(0x66EDC13B);
        alcApple.setMatureColor(0x88EDC13B);
        alcApple.setStillIconName(modBase + "cider_still");
        alcApple.setFlowingIconName(modBase + "cider_flow");

        alcPineapple = new FluidAlcohol("psc_pineapple", 2, 0.35, 1.7, 0.1, PSConfig.alcInfoPineapple);
        alcPineapple.addName(modBase + "drinkPineapple", new IntegerRange(0, -1), new IntegerRange(0, -1));
        alcPineapple.setColor(0x99EDC13B);
        alcPineapple.setDistilledColor(0x66EDC13B);
        alcPineapple.setMatureColor(0x88EDC13B);
        alcPineapple.setStillIconName(modBase + "cider_still");
        alcPineapple.setFlowingIconName(modBase + "cider_flow");

        alcBanana = new FluidAlcohol("psc_banana", 2, 0.35, 1.7, 0.1, PSConfig.alcInfoBanana);
        alcBanana.addName(modBase + "drinkBanana", new IntegerRange(0, -1), new IntegerRange(0, -1));
        alcBanana.setColor(0xbbe9ae3b);
        alcBanana.setDistilledColor(0x99e9ae3b);
        alcBanana.setMatureColor(0xaaD1984D);
        alcBanana.setStillIconName(modBase + "mead_still");
        alcBanana.setFlowingIconName(modBase + "mead_flow");

        alcMilk = new FluidAlcohol("psc_milk", 2, 0.35, 1.7, 0.1, PSConfig.alcInfoMilk);
        alcMilk.addName(modBase + "drinkMilk", new IntegerRange(0, -1), new IntegerRange(0, -1));
        alcMilk.setColor(0xeecac4b2);
        alcMilk.setColor(0x77cac4b2);
        alcMilk.setMatureColor(0x88D6BC90);
        alcMilk.setStillIconName(modBase + "rice_wine_still");
        alcMilk.setFlowingIconName(modBase + "rice_wine_flow");

        coffee = new FluidCoffee("psc_coffee");
        coffee.setDrinkable(true);
        coffee.setColor(0xffa77d55);
        coffee.setSymbolIconName(modBase + "drinkCoffee");
        coffee.setStillIconName(modBase + "coffee_still");
        coffee.setFlowingIconName(modBase + "coffee_flow");

        peyoteJuice = new FluidDrug("psc_peyote_juice", new DrugInfluence("Peyote", 15, 0.005, 0.003, 2.0f));
        peyoteJuice.setDrinkable(true);
        peyoteJuice.setColor(0x779bab62);
        peyoteJuice.setSymbolIconName(modBase + "drinkPeyote");
        peyoteJuice.setStillIconName(modBase + "tea_still");
        peyoteJuice.setFlowingIconName(modBase + "tea_flow");

        cocaTea = new FluidDrug("psc_coca_tea", new DrugInfluence("Cocaine", 60, 0.005, 0.002, 0.2f));
        cocaTea.setDrinkable(true);
        cocaTea.setColor(0x44787a36);
        cocaTea.setSymbolIconName(modBase + "drinkCocaTea");
        cocaTea.setStillIconName(modBase + "tea_still");
        cocaTea.setFlowingIconName(modBase + "tea_flow");

        cannabisTea = new FluidDrug("psc_cannabis_tea", new DrugInfluence("Cannabis", 60, 0.005, 0.002, 0.25f));
        cannabisTea.setDrinkable(true);
        cannabisTea.setColor(0x446d6f3c);
        cannabisTea.setSymbolIconName(modBase + "drinkCannabisTea");
        cannabisTea.setStillIconName(modBase + "tea_still");
        cannabisTea.setFlowingIconName(modBase + "tea_flow");

        cocaineFluid = new FluidDrug("psc_cocaine_fluid", new DrugInfluence("Cocaine", 0, 0.005, 0.01, 50.0f));
        cocaineFluid.setInjectable(true);
        cocaineFluid.setColor(0x44e8f4f8);
        cocaineFluid.setStillIconName(modBase + "clear_still");
        cocaineFluid.setFlowingIconName(modBase + "clear_flow");

        caffeineFluid = new FluidDrug("psc_caffeine_fluid", new DrugInfluence("Caffeine", 0, 0.005, 0.01, 85.0f));
        caffeineFluid.setInjectable(true);
        caffeineFluid.setColor(0x66eee2d3);
        caffeineFluid.setStillIconName(modBase + "clear_still");
        caffeineFluid.setFlowingIconName(modBase + "clear_flow");
    }
}

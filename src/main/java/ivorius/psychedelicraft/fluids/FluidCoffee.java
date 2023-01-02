/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluids;

import ivorius.psychedelicraft.entities.drugs.DrugInfluence;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.*;

import java.util.List;

/**
 * Created by lukas on 22.10.14.
 */
public class FluidCoffee extends FluidDrug {
    public static final int WARMTH_STEPS = 2;

    public FluidCoffee(Identifier id, Settings settings) {
        super(id, settings);
    }

    @Override
    public void getDrugInfluencesPerLiter(ItemStack fluidStack, List<DrugInfluence> list) {
        super.getDrugInfluencesPerLiter(fluidStack, list);
        float warmth = (float)getCoffeeTemperature(fluidStack) / WARMTH_STEPS;
        list.add(new DrugInfluence("Caffeine", 20, 0.002, 0.001, 0.25f + warmth * 0.05f));
        list.add(new DrugInfluence("Warmth", 0, 0.00, 0.1, 0.8f * warmth));
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(getTranslationKey() + ".temperature" + getCoffeeTemperature(stack));
    }

    public void setCoffeeTemperature(ItemStack stack, int temperature) {
        getFluidTag(stack, false).putInt("temperature", temperature);
    }

    public int getCoffeeTemperature(ItemStack stack) {
        return getFluidTag(stack, true).getInt("temperature");
    }
}

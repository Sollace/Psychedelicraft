/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Consumer;

import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;
import ivorius.psychedelicraft.item.PSItems;

/**
 * Created by lukas on 22.10.14.
 */
public class CoffeeFluid extends DrugFluid {
    public static final int WARMTH_STEPS = 2;

    public CoffeeFluid(Identifier id, Settings settings) {
        super(id, settings);
    }

    @Override
    public void getDrugInfluencesPerLiter(ItemStack fluidStack, List<DrugInfluence> list) {
        super.getDrugInfluencesPerLiter(fluidStack, list);
        float warmth = (float)getTemperature(fluidStack) / WARMTH_STEPS;
        list.add(new DrugInfluence(DrugType.CAFFEINE, 20, 0.002, 0.001, 0.25F + warmth * 0.05F));
        list.add(new DrugInfluence(DrugType.WARMTH, 0, 0, 0.1, 0.8F * warmth));
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(getTranslationKey() + ".temperature." + getTemperature(stack));
    }

    public ItemStack setTemperature(ItemStack stack, int temperature) {
        getFluidTag(stack, false).putInt("temperature", temperature);
        return stack;
    }

    public int getTemperature(ItemStack stack) {
        return MathHelper.clamp(getFluidTag(stack, true).getInt("temperature"), 0, WARMTH_STEPS);
    }

    @Override
    public void getDefaultStacks(FluidContainerItem container, Consumer<ItemStack> consumer) {
        super.getDefaultStacks(container, consumer);
        consumer.accept(setTemperature(getDefaultStack(container), 1));
        consumer.accept(setTemperature(getDefaultStack(container), 2));
    }

    @Override
    public boolean isSuitableContainer(FluidContainerItem container) {
        return container == PSItems.STONE_CUP;
    }
}

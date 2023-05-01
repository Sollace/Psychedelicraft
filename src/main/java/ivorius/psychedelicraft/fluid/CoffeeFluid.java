/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.*;

import java.util.function.Consumer;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.influence.DrugInfluence;

/**
 * Created by lukas on 22.10.14.
 */
public class CoffeeFluid extends DrugFluid {
    public static final Attribute<Integer> WARMTH = Attribute.ofInt("warmth", 0, 2);

    public CoffeeFluid(Identifier id, Settings settings) {
        super(id, settings);
    }

    @Override
    public void getDrugInfluencesPerLiter(ItemStack stack, Consumer<DrugInfluence> consumer) {
        super.getDrugInfluencesPerLiter(stack, consumer);
        float warmth = (float)WARMTH.get(stack) / 2F;
        consumer.accept(new DrugInfluence(DrugType.CAFFEINE, 20, 0.002, 0.001, 0.25F + warmth * 0.05F));
        consumer.accept(new DrugInfluence(DrugType.WARMTH, 0, 0, 0.1, 0.8F * warmth));
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(getTranslationKey() + ".temperature." + WARMTH.get(stack));
    }

    @Override
    public void getDefaultStacks(FluidContainer container, Consumer<ItemStack> consumer) {
        super.getDefaultStacks(container, consumer);
        consumer.accept(WARMTH.set(getDefaultStack(container), 1));
        consumer.accept(WARMTH.set(getDefaultStack(container), 2));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSuitableContainer(FluidContainer container) {
        return container.asItem().getRegistryEntry().isIn(PSTags.Items.SUITABLE_HOT_DRINK_RECEPTICALS);
    }
}

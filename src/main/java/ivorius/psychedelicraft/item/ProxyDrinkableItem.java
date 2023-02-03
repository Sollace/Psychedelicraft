/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.fluid.*;
import net.minecraft.item.*;
import net.minecraft.text.Text;

/**
 * Created by Sollace on Jan 1 2023
 */
public class ProxyDrinkableItem extends DrinkableItem {

    private final Item basis;

    public ProxyDrinkableItem(Item basis, Settings settings, int capacity, ConsumableFluid.ConsumptionType consumptionType) {
        super(settings.recipeRemainder(basis), capacity, capacity, consumptionType);
        this.basis = basis;
    }

    @Override
    public Item asEmpty() {
        return basis;
    }

    @Override
    public Text getName(ItemStack stack) {
        SimpleFluid fluid = getFluid(stack);

        if (!fluid.isEmpty()) {
            return Text.translatable("%s %s", fluid.getName(stack), basis.getName(stack));
        }

        return basis.getName(stack);
    }
}

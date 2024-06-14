/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.item.*;
import net.minecraft.text.Text;

/**
 * Created by Sollace on Jan 1 2023
 */
public class ProxyDrinkableItem extends DrinkableItem {

    private final Item basis;

    public ProxyDrinkableItem(Item basis, Settings settings, int capacity, ConsumableFluid.ConsumptionType consumptionType) {
        super(settings.recipeRemainder(basis), capacity, DEFAULT_MAX_USE_TIME, consumptionType);
        this.basis = basis;
    }

    @Override
    public Text getName(ItemStack stack) {
        ItemFluids fluids = ItemFluids.of(stack);

        if (!fluids.isEmpty()) {
            return Text.translatable("%s %s", fluids.fluid().getName(fluids), basis.getName(stack));
        }

        return basis.getName(stack);
    }
}

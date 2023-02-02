/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.fluid.FluidContainer;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.text.Text;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 1 Jan 2023
 */
public class FlaskItem extends BlockItem implements FluidContainer {

    private final int capacity;

    public FlaskItem(Block block, Settings settings, int capacity) {
        super(block, settings);
        this.capacity = capacity;
    }

    @Override
    public Text getName(ItemStack stack) {
        SimpleFluid fluid = getFluid(stack);

        if (!fluid.isEmpty()) {
            return Text.translatable(getTranslationKey() + ".filled", fluid.getName(stack));
        }

        return super.getName(stack);
    }

    @Override
    public int getMaxCapacity() {
        return capacity;
    }
}

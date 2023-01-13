/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import net.minecraft.block.*;
import net.minecraft.item.*;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 1 Jan 2023
 */
public class FlaskItem extends BlockItem implements FluidContainerItem {

    private final int capacity;

    public FlaskItem(Block block, Settings settings, int capacity) {
        super(block, settings);
        this.capacity = capacity;
    }

    @Override
    public int getMaxCapacity() {
        return capacity;
    }
}

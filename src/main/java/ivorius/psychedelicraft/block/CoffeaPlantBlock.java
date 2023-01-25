/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import net.minecraft.block.*;

public class CoffeaPlantBlock extends TobaccoPlantBlock {

    public CoffeaPlantBlock(Settings settings) {
        super(settings);
    }

    @Override
    public int getMaxAge(BlockState state) {
        return state.get(TOP) ? 3 : super.getMaxAge(state);
    }
}

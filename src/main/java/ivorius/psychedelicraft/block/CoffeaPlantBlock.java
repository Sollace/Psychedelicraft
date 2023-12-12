/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.*;

public class CoffeaPlantBlock extends TobaccoPlantBlock {
    public static final MapCodec<CoffeaPlantBlock> CODEC = createCodec(CoffeaPlantBlock::new);

    public CoffeaPlantBlock(Settings settings) {
        super(settings);
    }

    @Override
    public MapCodec<? extends CoffeaPlantBlock> getCodec() {
        return CODEC;
    }

    @Override
    public int getMaxAge(BlockState state) {
        return state.get(TOP) ? 3 : super.getMaxAge(state);
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

public class PeyoteBlock extends SucculentPlantBlock implements BlockEntityProvider {
    public static final IntProperty AGE = Properties.AGE_3;
    public PeyoteBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected IntProperty getAgeProperty() {
        return AGE;
    }

    @Override
    protected int getMaxAge() {
        return Properties.AGE_3_MAX;
    }

    @Override
    protected int getGrowthRate(BlockState state) {
        return state.get(getAgeProperty()) < getMaxAge() ? 20 : 120;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return PSBlockEntities.PEYOTE.instantiate(pos, state);
    }
}

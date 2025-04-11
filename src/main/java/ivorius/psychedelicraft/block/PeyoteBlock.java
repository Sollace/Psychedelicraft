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
import net.minecraft.util.shape.VoxelShape;

public class PeyoteBlock extends SucculentPlantBlock implements BlockEntityProvider {
    public static final IntProperty AGE = Properties.AGE_3;
    public static final int MAX_AGE = Properties.AGE_3_MAX;
    private static final VoxelShape[] SHAPES = {
            Block.createCuboidShape(6, 0, 6, 10, 4, 10),
            Block.createCuboidShape(5, 0, 5, 11, 4, 11),
            Block.createCuboidShape(4, 0, 4, 12, 4, 12),
            Block.createCuboidShape(3, 0, 3, 13, 4, 13)
    };

    public PeyoteBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape[] getShapes() {
        return SHAPES;
    }

    @Override
    protected IntProperty getAgeProperty() {
        return AGE;
    }

    @Override
    protected int getMaxAge() {
        return MAX_AGE;
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

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class LatticeBlock extends HorizontalFacingBlock {
    static final VoxelShape NS_SHAPE = Block.createCuboidShape(0, 0, 6.4, 16, 16, 9.6);
    static final VoxelShape EW_SHAPE = Block.createCuboidShape(6.4, 0, 0, 9.6, 16, 16);

    public LatticeBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(FACING).getAxis() == Axis.X ? EW_SHAPE : NS_SHAPE;
    }
}

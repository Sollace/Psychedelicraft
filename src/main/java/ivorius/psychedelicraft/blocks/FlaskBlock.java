/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.block.entity.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/**
 * Created by lukas on 25.10.14.
 */
public class FlaskBlock extends BlockWithFluid<FlaskBlockEntity> {
    private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 11.5, 12);

    public FlaskBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected BlockEntityType<FlaskBlockEntity> getBlockEntityType() {
        return PSBlockEntities.FLASK;
    }
}

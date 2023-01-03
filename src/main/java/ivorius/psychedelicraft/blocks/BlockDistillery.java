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
public class BlockDistillery extends BlockWithFluid<DistilleryBlockEntity> {
    private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 14.4, 12);

    public BlockDistillery(Settings settings) {
        super(settings.nonOpaque());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    /*
    @Override
    public IIcon getIcon(int side, int meta)
    {
        return Blocks.glass.getIcon(side, 0);
    }
*/
    @Override
    protected BlockEntityType<DistilleryBlockEntity> getBlockEntityType() {
        return PSBlockEntities.DISTILLERY;
    }
}

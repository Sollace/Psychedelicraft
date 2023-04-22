/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class TrayBlock extends Block {

    private static final VoxelShape X_SHAPE = ShapeUtil.createCenteredShape(9, 2, 6);
    private static final VoxelShape Z_SHAPE = ShapeUtil.createCenteredShape(6, 2, 9);

    private static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;

    public TrayBlock(Settings settings) {
        super(settings.nonOpaque());
        this.setDefaultState(getDefaultState().with(AXIS, Direction.Axis.X));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(AXIS) == Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(AXIS, ctx.getPlayerFacing().rotateYClockwise().getAxis());
    }
}

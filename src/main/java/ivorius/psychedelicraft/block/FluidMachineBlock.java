package ivorius.psychedelicraft.block;

import ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.BlockView;

abstract class FluidMachineBlock<T extends FluidProcessingBlockEntity> extends BlockWithFluid<T> {

    protected FluidMachineBlock(Settings settings) {
        super(settings);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        // signal 0-15 to indicate progress
        return direction.getAxis() == Axis.Y ? 0 : toRedstoneSignal(getProgress(world, pos), 15);
    }

    protected float getProgress(BlockView world, BlockPos pos) {
        return world.getBlockEntity(pos, getBlockEntityType())
                .map(e -> e.getProgress(1))
                .orElse(0F);
    }
}

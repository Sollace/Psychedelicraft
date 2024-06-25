package ivorius.psychedelicraft.block;

import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public interface PipeInsertable {
    int SPILL_STATUS = -1;

    default boolean acceptsConnectionFrom(WorldAccess world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return false;
    }

    default int tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, ItemFluids fluids) {
        return SPILL_STATUS;
    }

    static boolean canConnectWith(WorldAccess world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return neighborState.getBlock() instanceof PipeInsertable pipe
                && pipe.acceptsConnectionFrom(world, neighborState, neighborPos, state, pos, direction.getOpposite(), input);
    }

    static int tryInsert(ServerWorld world, BlockPos pos, Direction direction, Resovoir tank) {
        int amount = tryInsert(world, pos, direction, tank.getContents());
        if (amount > 0) {
            tank.drain(amount);
        }
        return amount;
    }

    @SuppressWarnings("deprecation")
    static int tryInsert(ServerWorld world, BlockPos pos, Direction direction, ItemFluids fluids) {
        if (!world.isChunkLoaded(pos)) {
            return 0;
        }
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof PipeInsertable insertable) {
            return insertable.tryInsert(world, state, pos, direction, fluids);
        }
        return SPILL_STATUS;
    }
}

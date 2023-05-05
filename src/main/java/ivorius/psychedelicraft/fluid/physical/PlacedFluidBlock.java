package ivorius.psychedelicraft.fluid.physical;

import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.Material;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

abstract class PlacedFluidBlock extends FluidBlock {
    protected abstract PhysicalFluid getPysicalFluid();

    public PlacedFluidBlock(FlowableFluid fluid) {
        super(fluid, AbstractBlock.Settings.of(Material.WATER).noCollision().strength(100).dropsNothing());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        getPysicalFluid().type.getStateManager().appendProperties(builder);
    }

    @Override
    public ItemStack tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
        if (state.get(LEVEL) == 0) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            return getPysicalFluid().type.getStack(state.getFluidState(), PSItems.FILLED_BUCKET);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getPysicalFluid().type.getStateManager().copyStateValues(state, super.getFluidState(state));
    }
}
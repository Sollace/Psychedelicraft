package ivorius.psychedelicraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface FluidFilled extends FluidFillable {
    int getFluidHeight(World world, BlockState state, BlockPos pos);

    FluidState getContainedFluid(World world, BlockState state, BlockPos pos);
}

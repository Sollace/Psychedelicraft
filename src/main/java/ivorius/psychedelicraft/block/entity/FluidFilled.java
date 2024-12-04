package ivorius.psychedelicraft.block.entity;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public interface FluidFilled extends FluidFillable {
    double getFluidHeight(World world, BlockState state, BlockPos pos);

    Optional<FluidState> getContainedFluid(World world, BlockState state, BlockPos pos);

    default Box getFluidCollisionBox(World world, BlockState state, BlockPos pos) {
        double height = getFluidHeight(world, state, pos);
        return new Box(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + height, pos.getZ() + 1
        ).expand(0.5, 0, 0.5);
    }
}

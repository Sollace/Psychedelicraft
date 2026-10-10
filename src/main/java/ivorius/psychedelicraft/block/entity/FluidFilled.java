package ivorius.psychedelicraft.block.entity;

import java.util.Optional;

import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public interface FluidFilled extends FluidFillable {
    double getFluidHeight(World world, BlockState state, BlockPos pos);

    Optional<FluidState> getContainedFluid(World world, BlockState state, BlockPos pos);

    default Box getFluidCollisionBox(World world, BlockState state, BlockPos pos) {
        double height = getFluidHeight(world, state, pos);
        Box box = state.getOutlineShape(world, pos).getBoundingBox();

        return box.offset(pos).withMaxY(pos.getY() + height)
                .expand(-0.01, 0, -0.01);
    }

    /**
     * Fluid level aware variant of {ItemUsage.exchangeStack} that correctly gives back either an empty or fill (no fractional) stack amounts when in creative.
     */
    static ItemStack exchangeStack(ItemStack inputStack, PlayerEntity player, ItemStack outputStack) {
        if (player.isInCreativeMode()) {
            outputStack = ItemFluids.of(inputStack).amount() < ItemFluids.of(outputStack).amount() ? ItemFluids.set(outputStack.copy(), ItemFluids.EMPTY) : ItemFluids.of(outputStack).ofFilling(outputStack);
        }
        return ItemUsage.exchangeStack(inputStack, player, outputStack);
    }
}

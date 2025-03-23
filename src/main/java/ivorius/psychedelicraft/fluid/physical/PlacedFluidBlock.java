package ivorius.psychedelicraft.fluid.physical;

import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public abstract class PlacedFluidBlock extends FluidBlock {
    protected abstract PhysicalFluid getPysicalFluid();

    static PlacedFluidBlock create(Identifier id, PhysicalFluid physical) {
        var key = RegistryKey.of(RegistryKeys.BLOCK, id);
        return Registry.register(Registries.BLOCK, key, new PlacedFluidBlock(key, (FlowableFluid)physical.getFlowingFluid()) {
            @Override
            protected PhysicalFluid getPysicalFluid() {
                return physical;
            }
        });
    }

    PlacedFluidBlock(RegistryKey<Block> key, FlowableFluid fluid) {
        super(fluid, Settings.copy(Blocks.WATER).ticksRandomly().registryKey(key));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        getPysicalFluid().getType().getStateManager().appendProperties(builder);
    }

    @Override
    public ItemStack tryDrainFluid(LivingEntity player, WorldAccess world, BlockPos pos, BlockState state) {
        if (state.get(LEVEL) == 0) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            ItemStack stack = PSItems.FILLED_BUCKET.getDefaultStack();
            return ItemFluids.set(stack, getPysicalFluid().getType().getStack(state.getFluidState(), FluidCapacity.get(stack)));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getPysicalFluid().getType().getStateManager().copyStateValues(state, super.getFluidState(state));
    }
}
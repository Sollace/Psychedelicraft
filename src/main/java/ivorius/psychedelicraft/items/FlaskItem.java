/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by lukas on 25.10.14.
 */
public class FlaskItem extends BlockItem implements FluidContainerItem {

    private final int capacity;

    public FlaskItem(Block block, Settings settings, int capacity) {
        super(block, settings);
        this.capacity = capacity;
    }

    @Override
    public int getMaxCapacity() {
        return capacity;
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        FluidState fluid = this.getFluidState(stack);
        if (state.getBlock() instanceof FluidContainer container) {
            container.fill(pos, world, state, fluid);
        }
        return super.postPlacement(pos, world, player, stack, state);
    }

    public interface FluidContainer {
        void fill(BlockPos pos, World world, BlockState state, FluidState fluid);
    }
}

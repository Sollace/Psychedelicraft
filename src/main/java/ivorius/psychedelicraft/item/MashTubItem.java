/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

/**
 * Updated by Sollace on 8 Feb 2023
 */
public class MashTubItem extends FlaskItem {
    public MashTubItem(Block block, Settings settings, int capacity) {
        super(block, settings, capacity);
    }

    @Override
    @Nullable
    public ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
        return findPlacementPosition(context.getWorld(), context.getBlockPos()).map(position -> {
            return new ItemPlacementContext(
                    context.getPlayer(), context.getHand(), context.getStack(),
                    new BlockHitResult(position.toCenterPos(), Direction.UP, position, false)
            );
        }).orElse(context);
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        if (super.place(context, state)) {
            BlockPos center = context.getBlockPos();
            BlockPos.iterateOutwards(center, 1, 0, 1).forEach(p -> {
                if (!p.equals(center)) {
                    context.getWorld().setBlockState(p, PSBlocks.MASH_TUB_EDGE.getDefaultState(), Block.NOTIFY_ALL);
                    context.getWorld().getBlockEntity(p, PSBlockEntities.MASH_TUB_EDGE).ifPresent(be -> be.setMasterPos(center));
                }
            });
            return true;
        }
        return false;
    }

    public static Optional<BlockPos> findPlacementPosition(WorldView world, BlockPos pos) {
        return BlockPos.streamOutwards(pos, 1, 0, 1)
                .filter(center -> BlockPos.streamOutwards(center, 1, 0, 1).allMatch(p -> {
                    BlockState s = world.getBlockState(p);
                    return world.isAir(p) || s.isReplaceable();
                }))
                .findFirst()
                .map(p -> p.toImmutable());
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.TileEntityRiftJar;
import ivorius.psychedelicraft.items.ItemRiftJar;
import ivorius.psychedelicraft.items.PSItems;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

class BlockRiftJar extends BlockWithEntity {
    private static final VoxelShape SHAPE = Block.createCuboidShape(1.6, 1.6, 1.6, 14.4, 12.8, 14.4);

    public BlockRiftJar(Settings settings) {
        super(settings);
    }

    @Override
    @Deprecated
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return world.getBlockEntity(pos, PSBlockEntities.RIFT_JAR).map(be -> {
            if (player.isSneaking()) {
                be.toggleSuckingRifts();
            } else {
                be.toggleRiftJarOpen();
            }
            return ActionResult.SUCCESS;
        }).orElse(ActionResult.FAIL);
    }

    @Deprecated
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && !world.isClient) {
            world.getBlockEntity(pos, PSBlockEntities.RIFT_JAR).ifPresent(be -> {

                if (!be.jarBroken) {
                    Block.dropStack(world, pos, ItemRiftJar.createFilledRiftJar(be.currentRiftFraction, PSItems.RIFT_JAR));
                }
            });
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityRiftJar(pos, state);
    }
}

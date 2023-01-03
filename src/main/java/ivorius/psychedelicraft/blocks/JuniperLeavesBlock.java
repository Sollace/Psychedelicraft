/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.items.PSItems;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class JuniperLeavesBlock extends LeavesBlock {
    public JuniperLeavesBlock(Settings settings) {
        super(settings);
    }

    /*
    @SideOnly(Side.CLIENT)
    public int getRenderColor(int p_149741_1_)
    {
        if ((p_149741_1_ & 3) == 0 || (p_149741_1_ & 3) == 1)
            return 0xffffff;

        return (p_149741_1_ & 3) == 1 ? ColorizerFoliage.getFoliageColorPine() : ((p_149741_1_ & 3) == 2 ? ColorizerFoliage.getFoliageColorBirch() : super.getRenderColor(p_149741_1_));
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess p_149720_1_, int p_149720_2_, int p_149720_3_, int p_149720_4_)
    {
        int l = p_149720_1_.getBlockMetadata(p_149720_2_, p_149720_3_, p_149720_4_);

        if ((l & 3) == 0 || (l & 3) == 1)
            return 0xffffff;

        return super.colorMultiplier(p_149720_1_, p_149720_2_, p_149720_3_, p_149720_4_);
    }*/

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        if (this == PSBlocks.juniper_leaves && random.nextFloat() < 0.01F && !state.get(WATERLOGGED)) {
            world.setBlockState(pos, PSBlocks.juniper_leaves.getDefaultState()
                    .with(DISTANCE, state.get(DISTANCE))
                    .with(PERSISTENT, state.get(PERSISTENT))
                    .with(WATERLOGGED, state.get(WATERLOGGED)));
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (this == PSBlocks.juniper_berries) {
            Block.dropStack(world, pos, PSItems.juniperBerries.getDefaultStack());
            world.setBlockState(pos, PSBlocks.juniper_leaves.getDefaultState());
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

}

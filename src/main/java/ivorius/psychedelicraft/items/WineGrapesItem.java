/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.blocks.LatticeBlock;
import ivorius.psychedelicraft.blocks.PSBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class WineGrapesItem extends SpecialFoodItem {
    public WineGrapesItem(Settings settings, int eatSpeed) {
        super(settings, eatSpeed);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.shouldCancelInteraction() || !context.getPlayer().canModifyAt(context.getWorld(), context.getBlockPos())) {
            return ActionResult.PASS;
        }

        BlockPos pos = context.getBlockPos();
        BlockState state = context.getWorld().getBlockState(pos);

        if (state.isOf(PSBlocks.LATTICE)) {
            context.getWorld().setBlockState(pos, PSBlocks.WINE_GRAPE_LATTICE.getDefaultState().with(LatticeBlock.FACING, state.get(LatticeBlock.FACING)));
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.items;

import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import net.minecraft.block.Block;

public class MashTubItem extends FlaskItem {
    public MashTubItem(Block block, Settings settings) {
        super(block, settings, MashTubBlockEntity.MASH_TUB_CAPACITY);
    }

    // TODO:
    /**
    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        int width = 2;
        int height = 1;

        int rotation = 0;
        List<int[]> positions = IvMultiBlockHelper.getRotatedPositions(rotation, width, height, width);

        IvMultiBlockHelper multiBlockHelper = new IvMultiBlockHelper();
        if (multiBlockHelper.beginPlacing(positions, world, x, y, z, side, itemStack, entityPlayer, this.field_150939_a, 0, rotation))
        {
            for (int[] position : multiBlockHelper)
            {
                IvTileEntityMultiBlock tileEntity = multiBlockHelper.placeBlock(position);

                if (tileEntity instanceof TileEntityMashTub)
                {
                    TileEntityMashTub mashTub = (TileEntityMashTub) tileEntity;
                    if (tileEntity.isParent())
                    {
                        FluidStack fluidStack = itemStack.getItem() instanceof IFluidContainerItem ? ((IFluidContainerItem) itemStack.getItem()).getFluid(itemStack) : null;
                        if (fluidStack != null)
                            mashTub.fill(ForgeDirection.UP, fluidStack, true);
                    }
                }
            }

            itemStack.stackSize--;
        }

        return true;
    }**/
}

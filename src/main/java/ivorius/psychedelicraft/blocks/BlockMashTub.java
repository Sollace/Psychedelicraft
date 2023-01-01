/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.ivtoolkit.blocks.IvBlockMultiblock;
import ivorius.ivtoolkit.blocks.IvTileEntityMultiBlock;
import ivorius.psychedelicraft.PSMultiBlockHelper;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.screen.PSGuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import java.util.List;

/**
 * Created by lukas on 27.10.14.
 */
public class BlockMashTub extends IvBlockMultiblock
{
    public BlockMashTub()
    {
        super(Material.wood);

        setStepSound(soundTypeWood);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public IIcon getIcon(int par1, int par2)
    {
        return Blocks.planks.getIcon(0, 0);
    }

    @Override
    public void parentBlockHarvestItem(World world, IvTileEntityMultiBlock tileEntity, int x, int y, int z, Block block, int metadata)
    {
        if (tileEntity instanceof TileEntityMashTub)
        {
            TileEntityMashTub tileEntityMashTub = (TileEntityMashTub) tileEntity;
            FluidStack fluidStack = tileEntityMashTub.drain(ForgeDirection.DOWN, TileEntityMashTub.MASH_TUB_CAPACITY, true);
            ItemStack stack = new ItemStack(this);

            if (fluidStack != null && fluidStack.amount > 0)
                ((IFluidContainerItem) stack.getItem()).fill(stack, fluidStack, true);

            dropBlockAsItem(world, x, y, z, stack);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9)
    {
        TileEntity tileEntity = getValidatedTotalParent(this, world, x, y, z);
        if (tileEntity instanceof TileEntityMashTub)
        {
            if (!world.isRemote)
            {
                TileEntityMashTub tileEntityMashTub = (TileEntityMashTub) tileEntity;
                if (tileEntityMashTub.solidContents != null)
                {
                    dropBlockAsItem(world, x, y, z, tileEntityMashTub.solidContents);
                    tileEntityMashTub.solidContents = null;

                    world.markBlockForUpdate(tileEntityMashTub.xCoord, tileEntityMashTub.yCoord, tileEntityMashTub.zCoord);
                    tileEntity.markDirty();
                }
                else
                    player.openGui(Psychedelicraft.instance, PSGuiHandler.woodenVatContainerID, world, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
            }

            return true;
        }

        return false;
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB entityBB, List list, Entity entity)
    {
        IvTileEntityMultiBlock tileEntity = getValidatedTotalParent(this, world, x, y, z);
        if (tileEntity instanceof TileEntityMashTub)
        {
            float size = 15.0f / 16.0f;
            float borderSize = 1.0f / 16.0f;
            float height = 16.0f / 16.0f;

            addIntersectingCollisionBox(-size - borderSize, -.5f, -size - borderSize, size * 2 + borderSize * 2, height, borderSize, tileEntity, entityBB, list, x, y, z);
            addIntersectingCollisionBox(-size - borderSize, -.5f, size, size * 2 + borderSize * 2, height, borderSize, tileEntity, entityBB, list, x, y, z);

            addIntersectingCollisionBox(-size - borderSize, -.5f, -size - borderSize, borderSize, height, size * 2 + borderSize * 2, tileEntity, entityBB, list, x, y, z);
            addIntersectingCollisionBox(size, -.5f, -size - borderSize, borderSize, height, size * 2 + borderSize * 2, tileEntity, entityBB, list, x, y, z);

            addIntersectingCollisionBox(-size - borderSize, -.5f, -size - borderSize, size * 2 + borderSize * 2, borderSize, size * 2 + borderSize * 2, tileEntity, entityBB, list, x, y, z);
        }
    }

    private void addIntersectingCollisionBox(double bbX, double bbY, double bbZ, double width, double height, double depth, IvTileEntityMultiBlock te, AxisAlignedBB entityBB, List list, int x, int y, int z)
    {
        AxisAlignedBB bb = PSMultiBlockHelper.intersection(te.getRotatedBB(bbX, bbY, bbZ, width, height, depth), x, y, z);
        if (entityBB.intersectsWith(bb))
            list.add(bb);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {

    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityMashTub();
    }
}

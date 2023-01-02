package ivorius.psychedelicraft.blocks;

import ivorius.ivtoolkit.blocks.IvMultiBlockHelper;
import ivorius.psychedelicraft.block.entity.TileEntityBottleRack;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Created by lukas on 16.11.14.
 */
public class BlockBottleRack extends Block
{
    public BlockBottleRack(Settings settings) {
        super(settings);
    }

    @Override
    public int getRenderType()
    {
        return -1;
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
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
    }

    @Override
    public IIcon getIcon(int side, int meta)
    {
        return Blocks.planks.getIcon(side, 0);
    }

    @Override
    public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
        TileEntity tileEntity = par1World.getTileEntity(par2, par3, par4);

        if (tileEntity instanceof TileEntityBottleRack)
        {
            TileEntityBottleRack tileEntityBottleRack = (TileEntityBottleRack) tileEntity;

            ItemStack heldItem = par5EntityPlayer.getHeldItem();
            if (heldItem != null && tileEntityBottleRack.tryStoringItem(heldItem, par5EntityPlayer))
                return true;
            else
                return tileEntityBottleRack.pickUpItem(par5EntityPlayer);
        }

        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
    {
        if (!world.isRemote)
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);

            if (tileEntity instanceof TileEntityBottleRack)
            {
                TileEntityBottleRack tileEntityBottleRack = (TileEntityBottleRack) tileEntity;
                for (int slot = 0; slot < tileEntityBottleRack.getSizeInventory(); slot++)
                {
                    ItemStack stackInSlotOnClosing = tileEntityBottleRack.getStackInSlotOnClosing(slot);
                    if (stackInSlotOnClosing != null)
                        dropBlockAsItem(world, x, y, z, stackInSlotOnClosing);
                }
            }
        }

        super.breakBlock(world, x, y, z, par5, par6);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
    {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z)
    {
        TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);

        if (tileEntity instanceof TileEntityBottleRack)
            setBlockBounds(IvMultiBlockHelper.getRotatedBB(-0.5f, -0.5f, -0.1f, 1.0f, 1.0f, 0.6f, ((TileEntityBottleRack) tileEntity).getDirection(), new double[]{0.5, 0.5, 0.5}));
        else
            setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    public void setBlockBounds(AxisAlignedBB bb)
    {
        setBlockBounds((float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ);
    }

    @Override
    public boolean hasTileEntity(int metadata)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World var1, int i)
    {
        return new TileEntityBottleRack();
    }
}

/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class HopPlantBlock extends CannabisPlantBlock {
    public HopPlantBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.FARMLAND) || floor.isOf(this) || floor.isOf(Blocks.DIRT) || floor.isOf(Blocks.GRASS_BLOCK);
    }
/*
    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune)
    {
        ArrayList<ItemStack> drops = new ArrayList<>();

        int countB = world.rand.nextInt(meta / 6 + 1);
        for (int i = 0; i < countB; i++)
            drops.add(new ItemStack(PSItems.hopCones, 1, 0));

        int countS = meta / 8;
        for (int i = 0; i < countS; i++)
            drops.add(new ItemStack(PSItems.hopSeeds, 1, 0));

        return drops;
    }
*/
}

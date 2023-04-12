/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import net.minecraft.block.*;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class CocaPlantBlock extends CannabisPlantBlock {
    private static final IntProperty AGE_12 = IntProperty.of("age", 0, 12);

    public CocaPlantBlock(Settings settings) {
        super(settings);
    }

    @Override
    public IntProperty getAgeProperty() {
        return AGE_12;
    }

    @Override
    public int getMaxAge(BlockState state) {
        return 12;
    }

    @Override
    protected float getRandomGrowthChance() {
        return 0.1F;
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

        int countL = world.rand.nextInt(meta / 3 + 1) + meta / 5;
        for (int i = 0; i < countL; i++)
            drops.add(new ItemStack(PSItems.cocaLeaf, 1, 0));

        int countS = meta / 8;
        for (int i = 0; i < countS; i++)
            drops.add(new ItemStack(PSItems.cocaSeeds, 1, 0));

        return drops;
    }
*/
}

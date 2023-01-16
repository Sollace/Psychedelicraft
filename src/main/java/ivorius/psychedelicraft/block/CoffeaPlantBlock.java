/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import net.minecraft.block.*;

public class CoffeaPlantBlock extends TobaccoPlantBlock {

    public CoffeaPlantBlock(Settings settings) {
        super(settings);
    }

    @Override
    public int getMaxAge(BlockState state) {
        return state.get(TOP) ? 3 : super.getMaxAge(state);
    }

/*
    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune)
    {
        ArrayList<ItemStack> drops = new ArrayList<>();

        int stage = (meta >> 1);
        boolean above = (meta & 1) == 1;

        if (above)
            stage += 4;

        if (stage == 6 || stage == 7)
        {
            int countL = (world.rand.nextInt(3) + 1) * (stage - 5);
            for (int i = 0; i < countL; i++)
                drops.add(new ItemStack(PSItems.coffeaCherries, 1, 0));
        }

        return drops;
    }*/
}

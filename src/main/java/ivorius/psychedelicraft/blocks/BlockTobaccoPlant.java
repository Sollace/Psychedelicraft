/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */
package ivorius.psychedelicraft.blocks;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;

public class BlockTobaccoPlant extends BlockCannabisPlant {
    public BlockTobaccoPlant(Settings settings) {
        super(settings);
    }

    @Override
    protected int getMaxHeight() {
        return 2;
    }

    @Override
    protected int getMaxAge(BlockState state) {
        return 7;
    }

    @Override
    protected float getRandomGrothChance() {
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
            drops.add(new ItemStack(PSItems.tobaccoLeaf, 1, 0));

        int countS = meta / 8;
        for (int i = 0; i < countS; i++)
            drops.add(new ItemStack(PSItems.tobaccoSeeds, 1, 0));

        return drops;
    }*/

    @Override
    public void applyGrowth(World world, Random random, BlockPos pos, BlockState state, boolean bonemeal) {
        int number = bonemeal ? random.nextInt(2) + 1 : 1;

        for (int i = 0; i < number; i++) {
            final int age = state.get(AGE);
            final boolean freeOver = world.isAir(pos.up()) && getPlantSize(world, pos) < getMaxHeight();

            if (age < getMaxAge(state)) {
                world.setBlockState(pos, state.cycle(AGE), Block.NOTIFY_ALL);
            }
            if (freeOver && age >= getMaxAge(state)) {
                world.setBlockState(pos.up(), getDefaultState(), Block.NOTIFY_ALL);
            }
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean client) {
        final boolean freeOver = world.isAir(pos.up()) && getPlantSize(world, pos) < getMaxHeight();
        final int age = state.get(AGE);
        return (age < getMaxAge(state))
            || (freeOver && age >= getMaxAge(state));
    }
}

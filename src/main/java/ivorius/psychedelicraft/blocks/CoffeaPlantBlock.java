/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import net.minecraft.block.*;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;

public class CoffeaPlantBlock extends TobaccoPlantBlock {
    public static final BooleanProperty TOP = BooleanProperty.of("top");

    public CoffeaPlantBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(TOP, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TOP);
    }

    @Override
    protected int getMaxAge(BlockState state) {
        return state.get(TOP) ? 3 : 7;
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

    @Override
    public void applyGrowth(World world, Random random, BlockPos pos, BlockState state, boolean bonemeal) {
        int number = bonemeal ? random.nextInt(2) + 1 : 1;

        for (int i = 0; i < number; i++) {
            final int age = state.get(AGE);
            final boolean freeOver = world.isAir(pos.up()) && getPlantSize(world, pos) < getMaxHeight();

            if (age < getMaxAge(state)) {
                world.setBlockState(pos, state.cycle(AGE), Block.NOTIFY_ALL);
            }
            if (freeOver && !state.get(TOP) && age >= 3) {
                world.setBlockState(pos.up(), getDefaultState().with(TOP, true), Block.NOTIFY_ALL);
            }
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean client) {
        final boolean freeOver = world.isAir(pos.up()) && getPlantSize(world, pos) < getMaxHeight();
        final int age = state.get(AGE);
        return (age < getMaxAge(state))
            || (freeOver && !state.get(TOP) && age >= 3);
    }
}

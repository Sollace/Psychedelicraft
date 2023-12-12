/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */
package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

public class TobaccoPlantBlock extends CannabisPlantBlock {
    public static final MapCodec<TobaccoPlantBlock> CODEC = createCodec(TobaccoPlantBlock::new);
    public static final BooleanProperty TOP = BooleanProperty.of("top");

    public TobaccoPlantBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(TOP, false));
    }

    @Override
    public MapCodec<? extends TobaccoPlantBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForHeight(int y) {
        return getDefaultState().with(TOP, y > 0);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TOP);
    }

    @Override
    public IntProperty getAgeProperty() {
        return Properties.AGE_7;
    }

    @Override
    public int getMaxHeight() {
        return 2;
    }

    @Override
    public int getMaxAge(BlockState state) {
        return 7;
    }

    @Override
    protected float getRandomGrowthChance() {
        return 0.1F;
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.FARMLAND) || floor.isOf(this) || floor.isIn(BlockTags.DIRT) || floor.isOf(Blocks.GRASS_BLOCK);
    }

    @Override
    public void applyGrowth(World world, BlockPos pos, BlockState state, boolean bonemeal) {
        int number = bonemeal ? world.random.nextInt(2) + 1 : 1;

        for (int i = 0; i < number; i++) {
            final boolean freeOver = world.isAir(pos.up()) && getPlantSize(world, pos) < getMaxHeight();

            if (state.get(getAgeProperty()) < getMaxAge(state)) {
                state = state.cycle(getAgeProperty());
                world.setBlockState(pos, state, Block.NOTIFY_ALL);
            }
            if (freeOver && state.get(getAgeProperty()) >= getMaxAge(state)) {
                pos = pos.up();
                state = getDefaultState().with(TOP, true);
                world.setBlockState(pos, state, Block.NOTIFY_ALL);
            }
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return (world.isAir(pos.up()) && getPlantSize(world, pos) < getMaxHeight())
                || state.get(getAgeProperty()) < getMaxAge(state);
    }
}

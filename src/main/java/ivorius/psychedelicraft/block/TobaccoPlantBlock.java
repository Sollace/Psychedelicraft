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
        return Properties.AGE_7_MAX;
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
    protected int getMaxBonemealGrowth() {
        return 2;
    }
}

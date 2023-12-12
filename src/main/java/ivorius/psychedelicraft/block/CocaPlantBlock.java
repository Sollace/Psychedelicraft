/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class CocaPlantBlock extends CannabisPlantBlock {
    public static final MapCodec<CocaPlantBlock> CODEC = createCodec(CocaPlantBlock::new);
    private static final IntProperty AGE_12 = IntProperty.of("age", 0, 12);

    public CocaPlantBlock(Settings settings) {
        super(settings);
    }

    @Override
    public MapCodec<? extends CocaPlantBlock> getCodec() {
        return CODEC;
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
        return floor.isOf(Blocks.FARMLAND) || floor.isOf(this) || floor.isIn(BlockTags.DIRT) || floor.isOf(Blocks.GRASS_BLOCK);
    }
}

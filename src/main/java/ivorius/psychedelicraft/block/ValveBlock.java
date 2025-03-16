/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ValveBlock extends GlassTubeBlock {
    public static final MapCodec<GlassTubeBlock> CODEC = createCodec(ValveBlock::new);

    public static final BooleanProperty OPEN = Properties.OPEN;

    protected ValveBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(OPEN, true));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(OPEN);
    }

    @Override
    protected boolean isBlocked(BlockState state) {
        return !state.get(OPEN);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        world.setBlockState(pos, state.cycle(OPEN));
        world.playSoundAtBlockCenter(pos, SoundEvents.ENTITY_PARROT_AMBIENT, SoundCategory.BLOCKS, 1.5F, state.get(OPEN) ? 1 : 5, true);
        return ActionResult.SUCCESS;
    }
}

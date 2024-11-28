package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TorchBlock;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class FlammableGasBlock extends AirBlock {
    public static final MapCodec<FlammableGasBlock> CODEC = createCodec(FlammableGasBlock::new);

    public FlammableGasBlock(Settings settings) {
        super(settings.ticksRandomly());
        FlammableBlockRegistry.getDefaultInstance().add(this, new FlammableBlockRegistry.Entry(150, 2000));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public MapCodec<AirBlock> getCodec() {
        return (MapCodec)CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        for (Direction direction : Direction.values()) {
            BlockState neighbor = world.getBlockState(pos.offset(direction));
            if (neighbor.getBlock() instanceof TorchBlock || neighbor.getFluidState().isIn(FluidTags.LAVA)) {
                world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                return;
            }
        }
        BlockState below = world.getBlockState(pos.down());
        if (below.isAir() && !below.isOf(this)) {
            world.setBlockState(pos.down(), state);
        }
        super.randomTick(state, world, pos, random);
    }
}

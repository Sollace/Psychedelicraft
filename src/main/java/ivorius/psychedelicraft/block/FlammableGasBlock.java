package ivorius.psychedelicraft.block;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TorchBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class FlammableGasBlock extends AirBlock {
    public FlammableGasBlock(Settings settings) {
        super(settings.ticksRandomly());
        FlammableBlockRegistry.getDefaultInstance().add(this, new FlammableBlockRegistry.Entry(150, 2000));
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        Vec3d center = pos.toCenterPos();
        for (int i = 0; i < 10; i++) {
            world.addParticle(ParticleTypes.ENTITY_EFFECT,
                    random.nextTriangular(center.x, 0.5F),
                    random.nextTriangular(center.y, 0.5F),
                    random.nextTriangular(center.z, 0.5F),
                    0, 1F, 0
            );
        }
    }

    @Deprecated
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
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

package ivorius.psychedelicraft.fluid.physical;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public abstract class PlacedFluid extends WaterFluid {
    private static final Direction[] ALL_DIRECTIONS = Direction.values();

    protected abstract PhysicalFluid getPysicalFluid();

    @Override
    protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
        super.appendProperties(builder);
        getType().getStateManager().appendProperties(builder);
    }

    public SimpleFluid getType() {
        return getPysicalFluid().getType();
    }

    @Override
    public Fluid getFlowing() {
        return getPysicalFluid().getFlowingFluid();
    }

    @Override
    public Fluid getStill() {
        return getPysicalFluid().getStandingFluid();
    }

    @Override
    public Item getBucketItem() {
        return getType().isEmpty() ? Items.BUCKET : PSItems.FILLED_BUCKET;
    }

    @Override
    public BlockState toBlockState(FluidState state) {
        return getType().getStateManager().copyStateValues(state, getPysicalFluid().getBlock().getDefaultState()
                .withIfExists(FluidBlock.LEVEL, getBlockStateLevel(state))
        );
    }

    @Override
    public int getLevelDecreasePerBlock(WorldView world) {
        return getType().getViscocity();
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == getStill() || fluid == getFlowing();
    }

    @Override
    public void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
        super.randomDisplayTick(world, pos, state, random);
        getType().randomDisplayTick(world, pos, state, random);
    }

    @Override
    protected void onRandomTick(World world, BlockPos pos, FluidState state, Random random) {
        super.onRandomTick(world, pos, state, random);
        getType().onRandomTick(world, pos, state, random);
    }

    @Override
    protected FluidState getUpdatedState(World world, BlockPos pos, BlockState state) {
        return getType().getStateManager().computeAverage(Stream.of(ALL_DIRECTIONS)
                .map(direction -> world.getBlockState(pos.offset(direction)).getFluidState())
                .filter(neighbourState -> neighbourState.getFluid().matchesType(this)),
                super.getUpdatedState(world, pos, state)
        );
    }

    @Override
    protected boolean isInfinite(World world) {
        return false;
    }

    @Nullable
    @Override
    public ParticleEffect getParticle() {
        return new FluidParticleEffect(PSParticles.DRIPPING_FLUID, getType());
    }

    static PlacedFluid still(PhysicalFluid physical) {
        return new PlacedFluid() {
            @Override
            public int getLevel(FluidState state) {
                return 8;
            }

            @Override
            public boolean isStill(FluidState state) {
                return true;
            }

            @Override
            protected PhysicalFluid getPysicalFluid() {
                return physical;
            }
        };
    }

    static PlacedFluid flowing(PhysicalFluid physical) {
        return new PlacedFluid() {
            @Override
            protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
                super.appendProperties(builder);
                builder.add(LEVEL);
            }

            @Override
            public int getLevel(FluidState state) {
                return state.get(LEVEL);
            }

            @Override
            public boolean isStill(FluidState state) {
                return false;
            }

            @Override
            protected PhysicalFluid getPysicalFluid() {
                return physical;
            }
        };
    }
}
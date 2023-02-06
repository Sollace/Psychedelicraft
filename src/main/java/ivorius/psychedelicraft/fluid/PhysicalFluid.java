package ivorius.psychedelicraft.fluid;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.Material;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class PhysicalFluid {
    private final Fluid standing;
    private final Fluid flowing;
    private final FluidBlock block;

    @Nullable
    private final SimpleFluid type;

    public PhysicalFluid(Fluid standing, Fluid flowing, FluidBlock block) {
        this.standing = standing;
        this.flowing = flowing;
        this.block = block;
        this.type = null;
    }

    PhysicalFluid(Identifier id, SimpleFluid type) {
        this.type = type;
        standing = Registry.register(Registries.FLUID, id, PlacedFluid.still(this));
        flowing = Registry.register(Registries.FLUID, id.withPath(p -> "flowing_" + p), PlacedFluid.flowing(this));
        block = Registry.register(Registries.BLOCK, id, new PlacedFluidBlock((FlowableFluid)flowing) {
            @Override
            protected PhysicalFluid getPysicalFluid() {
                return PhysicalFluid.this;
            }
        });
    }

    public Fluid getFluid() {
        return standing;
    }

    public Fluid getFlowingFluid() {
        return flowing;
    }

    public FluidBlock getBlock() {
        return block;
    }

    public FluidState getDefaultState() {
        return getFluid().getDefaultState();
    }

    @SuppressWarnings("deprecation")
    public boolean isIn(TagKey<Fluid> tag) {
        return standing.isIn(tag);
    }

    public boolean isOf(Fluid fluid) {
        return getFluid().matchesType(fluid);
    }

    abstract static class PlacedFluidBlock extends FluidBlock {
        protected abstract PhysicalFluid getPysicalFluid();

        public PlacedFluidBlock(FlowableFluid fluid) {
            super(fluid, AbstractBlock.Settings.of(Material.WATER).noCollision().strength(100).dropsNothing());
        }

        @Override
        public ItemStack tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
            if (state.get(LEVEL) == 0) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                return getPysicalFluid().type.getStack(state.getFluidState(), PSItems.FILLED_BUCKET);
            }
            return ItemStack.EMPTY;
        }
    }

    abstract static class PlacedFluid extends WaterFluid {
        protected abstract PhysicalFluid getPysicalFluid();

        public SimpleFluid getType() {
            return getPysicalFluid().type;
        }

        @Override
        public Fluid getFlowing() {
            return getPysicalFluid().flowing;
        }

        @Override
        public Fluid getStill() {
            return getPysicalFluid().standing;
        }

        @Override
        public Item getBucketItem() {
            return PSItems.FILLED_BUCKET;
        }

        @Override
        public BlockState toBlockState(FluidState state) {
            return getPysicalFluid().block.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
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
}

package ivorius.psychedelicraft.fluid;

import java.util.List;

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
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public class PhysicalFluid {
    public final Fluid standing;
    public final Fluid flowing;
    public final FluidBlock block;

    @Nullable
    private final SimpleFluid type;

    private final List<SimpleFluid.Attribute<?>> attributes;

    public PhysicalFluid(Fluid standing, Fluid flowing, FluidBlock block) {
        this.standing = standing;
        this.flowing = flowing;
        this.block = block;
        this.type = null;
        this.attributes = List.of();
    }

    public Fluid getFluid() {
        return standing;
    }

    public Fluid getFlowingFluid() {
        return flowing;
    }

    public FluidState getDefaultState() {
        return getFluid().getDefaultState();
    }

    @SuppressWarnings("deprecation")
    public boolean isIn(TagKey<Fluid> tag) {
        return standing.isIn(tag);
    }

    private void appendProperties(StateManager.Builder<?, ? extends State<?, ?>> builder) {
        attributes.forEach(attribute -> {
            attribute.append(builder);
        });
    }

    PhysicalFluid(Identifier id, SimpleFluid type, List<SimpleFluid.Attribute<?>> attributes) {
        this.type = type;
        this.attributes = attributes;
        standing = Registry.register(Registries.FLUID, id, new PlacedFluid.Still());
        flowing = Registry.register(Registries.FLUID, id.withPath(p -> "flowing_" + p), new PlacedFluid.Flowing());
        block = Registry.register(Registries.BLOCK, id, new PlacedFluidBlock((FlowableFluid)flowing));
    }

    class PlacedFluidBlock extends FluidBlock {
        public PlacedFluidBlock(FlowableFluid fluid) {
            super(fluid, AbstractBlock.Settings.of(Material.WATER).noCollision().strength(100).dropsNothing());
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
            super.appendProperties(builder);
            PhysicalFluid.this.appendProperties(builder);
        }

        @Override
        public ItemStack tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
            if (state.get(LEVEL) == 0) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                return type.getStack(state.getFluidState(), PSItems.FILLED_BUCKET);
            }
            return ItemStack.EMPTY;
        }
    }

    abstract class PlacedFluid extends WaterFluid {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            PhysicalFluid.this.appendProperties(builder);
        }

        public SimpleFluid getType() {
            return PhysicalFluid.this.type;
        }

        @Override
        public Fluid getFlowing() {
            return PhysicalFluid.this.flowing;
        }

        @Override
        public Fluid getStill() {
            return PhysicalFluid.this.standing;
        }

        @Override
        public Item getBucketItem() {
            return PSItems.FILLED_BUCKET;
        }

        @Override
        public boolean matchesType(Fluid fluid) {
            return fluid == getStill() || fluid == getFlowing();
        }
    }

    class Still extends PlacedFluid {
        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }
    }

    class Flowing extends PlacedFluid {
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
    }
}

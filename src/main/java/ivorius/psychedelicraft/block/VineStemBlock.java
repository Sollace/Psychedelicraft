package ivorius.psychedelicraft.block;

import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class VineStemBlock extends FlowerBlock {
    public static final MapCodec<VineStemBlock> CODEC = RecordCodecBuilder.<VineStemBlock>mapCodec(instance -> instance.group(
            Registries.BLOCK.getCodec().<Supplier<Block>>xmap(block -> () -> block, Supplier::get).fieldOf("lattice").forGetter(b -> b.lattice),
            AbstractBlock.createSettingsCodec()
    ).apply(instance, VineStemBlock::new));
    public static final BooleanProperty NORTH = ConnectingBlock.NORTH;
    public static final BooleanProperty EAST = ConnectingBlock.EAST;
    public static final BooleanProperty SOUTH = ConnectingBlock.SOUTH;
    public static final BooleanProperty WEST = ConnectingBlock.WEST;

    public static final IntProperty AGE = Properties.AGE_4;
    public static final int MAX_AGE = Properties.AGE_4_MAX;

    private final Supplier<Block> lattice;

    public VineStemBlock(Supplier<Block> lattice, Settings settings) {
        super(StatusEffects.MINING_FATIGUE, 5, settings);
        this.lattice = lattice;
        setDefaultState(getDefaultState().with(AGE, 0).with(NORTH, false).with(SOUTH, false).with(EAST, false).with(WEST, false));
    }

    @Override
    public MapCodec<? extends VineStemBlock> getCodec() {
        return CODEC;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {

        if (state.get(AGE) < MAX_AGE) {
            world.setBlockState(pos, state.cycle(AGE));
        }

        for (BlockPos mPos : BlockPos.iterateInSquare(pos, 1, Direction.NORTH, Direction.EAST)) {
            BlockState s = world.getBlockState(mPos);
            if (s.isOf(PSBlocks.LATTICE)) {
                world.setBlockState(mPos, LatticeBlock.copyStateProperties(lattice.get().getDefaultState(), s));
                return;
            }
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        return super.getPlacementState(ctx)
                .with(NORTH, canConnect(world.getBlockState(pos.north()), Direction.SOUTH))
                .with(EAST, canConnect(world.getBlockState(pos.east()), Direction.WEST))
                .with(SOUTH, canConnect(world.getBlockState(pos.south()), Direction.NORTH))
                .with(WEST, canConnect(world.getBlockState(pos.west()), Direction.EAST));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis().isHorizontal()) {
            return state.with(ConnectingBlock.FACING_PROPERTIES.get(direction), canConnect(neighborState, direction.getOpposite()));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public boolean canConnect(BlockState state, Direction dir) {
        return state.isOf(PSBlocks.MORNING_GLORY_LATTICE);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, AGE);
    }
}

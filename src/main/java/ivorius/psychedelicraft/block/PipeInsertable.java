package ivorius.psychedelicraft.block;

import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.recipe.FluidMound;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldView;

public interface PipeInsertable {
    Either<Optional<PipeFluids>, Unit> STATUS_VOIDED = Either.right(Unit.INSTANCE);
    Either<Optional<PipeFluids>, Unit> STATUS_ACCEPT_ALL = Either.left(Optional.empty());

    static Either<Optional<PipeFluids>, Unit> reject(PipeFluids fluids) {
        return Either.left(Optional.of(fluids));
    }

    default boolean acceptsConnectionFrom(WorldView world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return false;
    }

    default Either<Optional<PipeFluids>, Unit> tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        return STATUS_VOIDED;
    }

    static boolean canConnectWith(WorldView world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return neighborState.getBlock() instanceof PipeInsertable pipe
                && pipe.acceptsConnectionFrom(world, neighborState, neighborPos, state, pos, direction.getOpposite(), input);
    }

    @SuppressWarnings("deprecation")
    static Either<Optional<PipeFluids>, Unit> tryInsert(ServerWorld world, BlockPos pos, Direction direction, PipeFluids fluids) {
        if (fluids.isEmpty()) {
            return STATUS_ACCEPT_ALL;
        }
        if (!world.isChunkLoaded(pos)) {
            return Either.left(Optional.of(fluids));
        }
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof PipeInsertable insertable) {
            return insertable.tryInsert(world, state, pos, direction, fluids);
        }
        return STATUS_VOIDED;
    }

    public record PipeFluids(FluidMound fluids, int temperature) {
        public static final PipeFluids EMPTY = new PipeFluids(new FluidMound(), 0);
        public static final Codec<PipeFluids> CODEC = RecordCodecBuilder.create(i -> i.group(
                FluidMound.CODEC.fieldOf("fluids").forGetter(PipeFluids::fluids),
                Codec.INT.fieldOf("temperature").forGetter(PipeFluids::temperature)
        ).apply(i, PipeFluids::new));
        public static final Codec<List<PipeFluids>> LIST_CODEC = Codec.xor(CODEC.listOf(), CODEC).flatXmap(
                either -> Either.unwrap(either.mapBoth(DataResult::success, single -> DataResult.success(List.of(single)))),
                list -> DataResult.success(Either.left(list))
        );
        public PipeFluids {
            temperature = MathHelper.clamp(temperature, 0, 15);
        }

        public boolean isEmpty() {
            return fluids.isEmpty();
        }

        public PipeFluids combine(PipeFluids fluids) {
            return new PipeFluids(new FluidMound(fluids()).addAll(fluids.fluids()), MathHelper.lerp(0.5F, temperature(), fluids.temperature()));
        }

        public PipeFluids withTemperature(int temperature) {
            return new PipeFluids(new FluidMound(fluids()), temperature);
        }

        public FluidMound splitCondensate() {
            return fluids.split(i -> i.fluid().getCondensationTemperature() > temperature);
        }
    }
}

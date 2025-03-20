package ivorius.psychedelicraft.block;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

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
        return fluids.isEmpty() ? STATUS_ACCEPT_ALL : Either.left(Optional.of(fluids));
    }

    default boolean acceptsConnectionFrom(WorldView world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return false;
    }

    default Either<Optional<PipeFluids>, Unit> tryInsert(ServerWorld world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        return STATUS_VOIDED;
    }

    default Optional<PipeFluids> tryExtract(ServerWorld world, BlockState state, BlockPos pos, Direction direction) {
        return Optional.empty();
    }

    static boolean canConnectWith(WorldView world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        var pipe = getPipeInterableAt(world, neighborState, neighborPos);
        return pipe != null && pipe.acceptsConnectionFrom(world, neighborState, neighborPos, state, pos, direction.getOpposite(), input);
    }

    @Nullable
    static PipeInsertable getPipeInterableAt(WorldView world, BlockState state, BlockPos pos) {
        return state.getBlock() instanceof PipeInsertable a ? a : world.getBlockEntity(pos) instanceof PipeInsertable b ? b : null;
    }

    @SuppressWarnings("deprecation")
    static Either<Optional<PipeFluids>, Unit> tryInsert(ServerWorld world, BlockPos pos, Direction direction, PipeFluids fluids) {
        if (fluids.isEmpty()) {
            return STATUS_ACCEPT_ALL;
        }
        if (!world.isChunkLoaded(pos)) {
            return reject(fluids);
        }
        BlockState state = world.getBlockState(pos);
        var pipe = getPipeInterableAt(world, state, pos);
        return pipe == null ? STATUS_VOIDED : pipe.tryInsert(world, state, pos, direction, fluids);
    }

    @SuppressWarnings("deprecation")
    static Optional<PipeFluids> tryExtract(ServerWorld world, BlockPos pos, Direction direction) {
        if (!world.isChunkLoaded(pos)) {
            return Optional.empty();
        }
        BlockState state = world.getBlockState(pos);
        var pipe = getPipeInterableAt(world, state, pos);
        return pipe == null ? Optional.empty() : pipe.tryExtract(world, state, pos, direction);
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

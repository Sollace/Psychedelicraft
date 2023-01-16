package ivorius.psychedelicraft.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.block.CannabisPlantBlock;
import ivorius.psychedelicraft.block.PSBlocks;
import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class TilledPatchFeature extends Feature<TilledPatchFeature.Config> {
    public TilledPatchFeature() {
        super(TilledPatchFeature.Config.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<Config> context) {
        final StructureWorldAccess world = context.getWorld();

        final Random random = context.getRandom();

        Config config = context.getConfig();

        BlockPos.Mutable mutablePos = context.getOrigin().mutableCopy();

        findTerrainLevel(world, mutablePos);

        if (world.isOutOfHeightLimit(mutablePos) || (!world.isAir(mutablePos) && !isReplaceable(world, mutablePos))) {
            return false;
        }

        final BlockPos origin = mutablePos.toImmutable();
        mutablePos.move(Direction.DOWN);

        if (isSoil(world, mutablePos) && (!config.needsWater || isWaterNearby(world, mutablePos))) {
            final int patchSize = context.getRandom().nextInt(3) + 1;
            final int patchSizeSq = patchSize * patchSize;

            for (int xOffset = -patchSize; xOffset <= patchSize; xOffset++) {
                for (int zOffset = -patchSize; zOffset <= patchSize; zOffset++) {

                    if (xOffset * xOffset + zOffset * zOffset > patchSizeSq) {
                        continue;
                    }
                    mutablePos.set(origin);
                    mutablePos.move(xOffset, 0, zOffset);

                    findTerrainLevel(world, mutablePos);

                    if (world.isOutOfHeightLimit(mutablePos) || !isReplaceable(world, mutablePos)) {
                        continue;
                    }
                    mutablePos.move(Direction.DOWN);

                    if (isSoil(world, mutablePos) && random.nextInt(3) == 0) {
                        setBlockState(world, mutablePos, Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, config.needsWater ? 7 : 0));
                        placeCrop(world, config, random, mutablePos);
                    }
                }
            }

            return true;
        }

        return false;
    }

    private void placeCrop(StructureWorldAccess world, Config config, Random random, BlockPos.Mutable mutablePos) {
        int plantHeight = Math.min(2 + random.nextInt(random.nextInt(3) + 1), config.block.getMaxHeight());

        for (int i = 0; i < plantHeight; ++i) {
            BlockState state = config.block.getStateForHeight(i);

            int age = config.block.getMaxAge(state);
            if (i == plantHeight - 1) {
                age = random.nextInt(age + 1);
            }

            state = state.with(config.block.getAgeProperty(), age);

            mutablePos.move(Direction.UP);

            if (!state.canPlaceAt(world, mutablePos)) {
                break;
            }

            setBlockState(world, mutablePos, state);
        }
    }

    static boolean isReplaceable(StructureWorldAccess world, BlockPos pos) {
        return world.isAir(pos) || world.getBlockState(pos).isIn(BlockTags.REPLACEABLE_PLANTS);
    }

    static boolean isWaterNearby(StructureWorldAccess world, BlockPos pos) {
        return BlockPos.findClosest(pos, 1, 1, p -> world.getFluidState(p).isIn(FluidTags.WATER) && pos != null)
                .isPresent();
    }

    static void findTerrainLevel(StructureWorldAccess world, BlockPos.Mutable mutablePos) {
        if (isReplaceable(world, mutablePos)) {
            do {
                mutablePos.move(Direction.DOWN);
            } while (isReplaceable(world, mutablePos) && !world.isOutOfHeightLimit(mutablePos));

            mutablePos.move(Direction.UP);
        }

        if (!isReplaceable(world, mutablePos)) {
            do {
                mutablePos.move(Direction.UP);
            } while (!isReplaceable(world, mutablePos) && !world.isOutOfHeightLimit(mutablePos));
        }
    }

    public static record Config (boolean needsWater, CannabisPlantBlock block) implements FeatureConfig {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codec.BOOL.fieldOf("needsWater").orElse(true).forGetter(Config::needsWater),
                    Registries.BLOCK.getCodec().fieldOf("block").forGetter(Config::block)
            ).apply(instance, (needsWater, block) -> new Config(
                    (boolean)needsWater,
                    (CannabisPlantBlock)(block instanceof CannabisPlantBlock ? block : PSBlocks.CANNABIS)
            ));
        });
    }
}

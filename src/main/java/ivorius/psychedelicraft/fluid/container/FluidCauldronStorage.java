package ivorius.psychedelicraft.fluid.container;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.MapMaker;
import com.google.common.primitives.Ints;

import ivorius.psychedelicraft.block.FluidCauldronBlock;
import ivorius.psychedelicraft.block.FluidCauldronBlock.Data;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record FluidCauldronStorage(WorldLocation location) implements SingleSlotStorage<FluidVariant> {
    private static final Map<WorldLocation, FluidCauldronStorage> CAULDRONS = new MapMaker().concurrencyLevel(1).weakValues().makeMap();
    private static final int AMOUNT_PER_LEVEL = FluidVolumes.BOTTLE;

    public static FluidCauldronStorage get(World world, BlockPos pos) {
        return CAULDRONS.computeIfAbsent(new WorldLocation(world, pos), FluidCauldronStorage::new);
    }

    private record WorldLocation(World world, BlockPos pos) {
        public BlockState getBlockState() {
            return world().getBlockState(pos());
        }

        public <T extends BlockEntity> Optional<T> getBlockEntity(BlockEntityType<T> type) {
            return world().getBlockEntity(pos(), type);
        }
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {

        int maxLevelsInserted = Ints.saturatedCast(maxAmount / AMOUNT_PER_LEVEL);

        if (getAmount() == 0) {
            int levelsInserted = Math.min(maxLevelsInserted, FluidCauldronBlock.MAX_LEVEL);
            int amountInserted = levelsInserted * AMOUNT_PER_LEVEL;
            if (amountInserted > 0) {
                FluidCauldronBehavior.setCauldronState(location.world(), location.pos(), ItemFluids.of(resource, levelsInserted * AMOUNT_PER_LEVEL), levelsInserted);
            }

            return amountInserted;
        }

        ItemFluids currentFluids = getFluids();

        if (ItemFluids.of(resource, (int)maxAmount).canCombine(currentFluids)) {
            return 0;
        }

        int currentLevel = location.getBlockState().get(FluidCauldronBlock.LEVEL);
        int levelsInserted = Math.min(maxLevelsInserted, FluidCauldronBlock.MAX_LEVEL - currentLevel);

        if (levelsInserted > 0) {
            FluidCauldronBehavior.setCauldronState(location.world(), location.pos(), ItemFluids.of(resource, (currentLevel + levelsInserted) * AMOUNT_PER_LEVEL), currentLevel + levelsInserted);
        }

        return levelsInserted * AMOUNT_PER_LEVEL;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        ItemFluids currentFluids = getFluids();

        if (!ItemFluids.of(resource, (int)maxAmount).canCombine(currentFluids)) {
            return 0;
        }
        int maxLevelsExtracted = Ints.saturatedCast(maxAmount / AMOUNT_PER_LEVEL);
        int currentLevel = location.getBlockState().get(FluidCauldronBlock.LEVEL);
        int levelsExtracted = Math.min(maxLevelsExtracted, currentLevel);

        if (levelsExtracted > 0) {
            FluidCauldronBehavior.setCauldronState(location.world(), location.pos(),
                    ItemFluids.of(resource, (currentLevel - levelsExtracted) * AMOUNT_PER_LEVEL), currentLevel - levelsExtracted);
        }

        return levelsExtracted * AMOUNT_PER_LEVEL;
    }

    @Override
    public boolean isResourceBlank() {
        return getFluids().isEmpty();
    }

    @Override
    public FluidVariant getResource() {
        return getFluids().toVariant();
    }

    @Override
    public long getAmount() {
        return location.getBlockState().get(FluidCauldronBlock.LEVEL) * AMOUNT_PER_LEVEL;
    }

    private ItemFluids getFluids() {
        return location.getBlockEntity(PSBlockEntities.CAULDRON).map(Data::getFluid).orElse(ItemFluids.EMPTY);
    }

    @Override
    public long getCapacity() {
        return FluidCauldronBlock.MAX_LEVEL * AMOUNT_PER_LEVEL;
    }
}
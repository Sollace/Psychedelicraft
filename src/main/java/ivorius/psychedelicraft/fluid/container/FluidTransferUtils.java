package ivorius.psychedelicraft.fluid.container;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jetbrains.annotations.UnmodifiableView;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

/**
 * Here be dragons
 */
public interface FluidTransferUtils {
    static StandaloneContext standalone(ItemStack stack) {
        return new StandaloneContext(stack);
    }

    static Pair<ItemStack, Optional<Pair<Long, FluidVariant>>> extract(ItemStack stack, long amount) {
        var context = standalone(stack);
        var result = getAsContainer(stack, context)
                .filter(c -> !c.isResourceBlank())
                .map(container -> applyTransaction(t -> {
                    FluidVariant type = container.getResource();
                    long amountExtracted = container.extract(type, amount, t);
                    if (amountExtracted > 0) {
                        return new Pair<>(amountExtracted, type);
                    }
                    return null;
                }));

        return new Pair<>(context.getStack(), result);
    }

    static Pair<ItemStack, Long> deposit(ItemStack stack, FluidVariant variant, long amount) {
        var context = standalone(stack);
        var result = amount - getAsStorage(stack, context)
                .map(storage -> applyTransaction(t -> storage.insert(variant, amount, t)))
                .orElse(0L);

        return new Pair<>(context.getStack(), result);
    }

    static Optional<Storage<FluidVariant>> getAsStorage(ItemStack stack, ContainerItemContext context) {
        return Optional.ofNullable(FluidStorage.ITEM.find(stack, context));
    }

    private static Optional<StorageView<FluidVariant>> getAsContainer(ItemStack stack, ContainerItemContext context) {
        return getAsStorage(stack, context).map(storage -> {
            Iterator<StorageView<FluidVariant>> iter = storage.iterator();
            return iter.hasNext() ? iter.next() : null;
        });
    }

    static Optional<Pair<Long, FluidVariant>> getContents(ItemStack stack) {
        return getAsContainer(stack, ContainerItemContext.withConstant(stack))
                .filter(c -> !c.isResourceBlank())
                .map(container -> new Pair<>(container.getAmount(), container.getResource()));
    }

    static long getCapacity(ItemStack stack) {
        return getAsContainer(stack, ContainerItemContext.withConstant(stack))
                .filter(c -> !c.isResourceBlank() || true)
                .map(container -> container.getCapacity())
                .orElse(0L);
    }

    private static <T> T applyTransaction(Function<Transaction, T> action) {
        try (@SuppressWarnings("deprecation") var transaction = Transaction.isOpen()
                ? Transaction.getCurrentUnsafe().openNested()
                : Transaction.openOuter()) {
            try {
                return action.apply(transaction);
            } finally {
                transaction.commit();
            }
        }
    }

    public static final class StandaloneContext extends SingleStackStorage implements ContainerItemContext {
        private ItemStack stack;

        private StandaloneContext(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public SingleSlotStorage<ItemVariant> getMainSlot() {
            return this;
        }

        @Override
        public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
            return 0;
        }

        @Override
        public @UnmodifiableView List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
            return List.of();
        }

        @Override
        public ItemStack getStack() {
            return stack;
        }

        @Override
        protected void setStack(ItemStack stack) {
            this.stack = stack;
        }
    }
}

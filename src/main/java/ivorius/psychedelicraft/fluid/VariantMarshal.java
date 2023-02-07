package ivorius.psychedelicraft.fluid;

import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

/**
 * Interop layer with Fabric's "experimental" transfer api.
 */
final class VariantMarshal {
    static void bootstrap() {
        FluidStorage.GENERAL_COMBINED_PROVIDER.register(context -> {
            if (context.getItemVariant().getItem() instanceof FluidContainer container) {
                MutableFluidContainer contents = MutableFluidContainer.of(context.getItemVariant().toStack());
                if (!contents.isEmpty()) {
                    return new FullItemFluidStorage(context, v -> ItemVariant.of(container.asEmpty()), packFluid(contents), contents.getLevel());
                }
            }
            return null;
        });
        FluidStorage.combinedItemApiProvider(Items.BUCKET).register(context -> {
            return new InsertionOnlyStorage<>() {
                @Override
                public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                    if (resource.getFluid() instanceof PhysicalFluid.PlacedFluid) {
                        ItemStack stack = VariantMarshal.unpackFluid(context.getItemVariant(), resource, FluidVolumes.BUCKET).asStack();

                        if (context.exchange(ItemVariant.of(stack), 1, transaction) == 1) {
                            return FluidVolumes.BUCKET;
                        }
                    }
                    return 0;
                }
            };
        });
    }

    public static FluidVariant packFluid(MutableFluidContainer contents) {
        return FluidVariant.of(contents.getFluid().getPhysical().getFluid(), contents.getAttributes());
    }

    public static MutableFluidContainer unpackFluid(ItemVariant container, FluidVariant contents, long level) {
        return MutableFluidContainer.of(container.toStack())
            .withFluid(SimpleFluid.forVanilla(contents.getFluid()))
            .withLevel((int)level)
            .withAttributes(contents.copyNbt());
    }

    public static Optional<ViewBasedFluidContainer> probeContents(ItemStack stack) {
        return Optional.of(stack).filter(s -> {
            var storage = FluidStorage.ITEM.find(s, ContainerItemContext.withInitial(s.copy()));
            return storage != null && storage.iterator().hasNext();
        }).map(ViewBasedFluidContainer::new);
    }

    public static class ViewBasedFluidContainer implements FluidContainer {
        private final Item item;
        private final Supplier<MutableFluidContainer> blankView;
        private final Supplier<Item> empty;

        ViewBasedFluidContainer(ItemStack stack) {
            this.item = stack.getItem();
            this.blankView = Suppliers.memoize(() -> toMutable(stack.copy()));
            empty = Suppliers.memoize(() -> toMutable(stack.copy()).drain(getMaxCapacity()).asStack().getItem());
        }

        @Override
        public MutableFluidContainer toMutable(ItemStack stack) {
            var context = ContainerItemContext.withInitial(stack.copy());
            var storage = FluidStorage.ITEM.find(stack, context);
            var view = storage.iterator().next();
            return new Mutable(this, context, storage, view);
        }

        @Override
        public SimpleFluid getFluid(ItemStack stack) {
            return toMutable(stack).getFluid();
        }

        @Override
        public int getLevel(ItemStack stack) {
            return toMutable(stack).getLevel();
        }

        @Override
        public int getMaxCapacity() {
            return blankView.get().getCapacity();
        }

        @Override
        public Item asEmpty() {
            return empty.get();
        }

        @Override
        public Item asItem() {
            return item;
        }

        private static class Mutable extends MutableFluidContainer {
            private ContainerItemContext context;
            private Storage<FluidVariant> storage;
            private StorageView<FluidVariant> view;

            Mutable(ViewBasedFluidContainer container, ContainerItemContext context, Storage<FluidVariant> storage, StorageView<FluidVariant> view) {
                super(container,
                        SimpleFluid.forVanilla(view.getResource().getFluid()),
                        (int)view.getAmount(),
                        FluidContainer.EMPTY_NBT
                );
                this.context = context;
                this.storage = storage;
                this.view = view;
            }

            @Override
            public MutableFluidContainer copy() {
                ItemStack stack = asStack();
                return probeContents(stack).map(i -> i.toMutable(stack)).orElseGet(() -> super.copy());
            }

            @Override
            public int getCapacity() {
                return (int)view.getCapacity();
            }

            @Override
            public ItemStack asStack() {
                commitChanges();
                // convert whatever it is into a stack
                return context.getItemVariant().toStack((int)context.getAmount());
            }

            @Override
            public MutableFluidContainer withAttributes(NbtCompound attributes) {
                // Attribute transfers not supported
                return this;
            }

            private void commitChanges() {
                long oldLevel = view.getAmount();
                FluidVariant oldFluid = view.getResource();

                long newLevel = getLevel();
                FluidVariant newFluid = FluidVariant.of(getFluid().getPhysical().getFluid());

                if (oldLevel != newLevel || !oldFluid.equals(newFluid)) {
                    try (var transaction = Transaction.openOuter()) {
                        // drain everything out
                        view.extract(oldFluid, oldLevel, transaction);
                        // insert new contents
                        if (!isEmpty()) {
                            storage.insert(newFluid, newLevel, transaction);
                        }
                        view = storage.exactView(newFluid);
                        // transaction
                        transaction.commit();
                    }
                }
            }
        }
    }

    public interface StorageMarshal extends Inventory, SingleSlotStorage<FluidVariant>, FluidStore {
        @Override
        default boolean isEmpty() {
            return getContents().isEmpty();
        }

        @Override
        default long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            SimpleFluid fluid = SimpleFluid.forVanilla(resource.getFluid());

            if (!isEmpty() && fluid != getContents().getFluid()) {
                return 0;
            }

            MutableFluidContainer inputContainer = VariantMarshal.unpackFluid(ItemVariant.of(Items.STONE), resource, maxAmount);
            deposit((int)maxAmount, inputContainer, null);
            return maxAmount - inputContainer.getLevel();
        }

        @Override
        default long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            if (SimpleFluid.forVanilla(resource.getFluid()) != getContents().getFluid()) {
                return 0;
            }

            MutableFluidContainer outputContainer = FluidContainer.UNLIMITED.toMutable(Items.STONE.getDefaultStack());
            drain((int)maxAmount, outputContainer, null);
            return outputContainer.getLevel();
        }

        @Override
        default boolean isResourceBlank() {
            return isEmpty();
        }

        @Override
        default FluidVariant getResource() {
            return packFluid(getContents());
        }

        @Override
        default long getAmount() {
            return getLevel();
        }

        @Override
        default long getCapacity() {
            return getContents().getCapacity();
        }
    }
}

package ivorius.psychedelicraft.fluid;

import java.util.List;
import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
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
                MutableFluidContainer contents = unpackFluid(context.getItemVariant());
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

                        if (context.exchange(packStack(stack), 1, transaction) == 1) {
                            return FluidVolumes.BUCKET;
                        }
                    }
                    return 0;
                }
            };
        });

    }

    public static ItemStack unpackStack(ItemVariant item) {
        ItemStack stack = item.getItem().getDefaultStack();
        stack.getOrCreateNbt().put("fluid", item.copyNbt());
        return stack;
    }

    public static ItemVariant packStack(ItemStack stack) {
        return ItemVariant.of(stack.getItem(), FluidContainer.getFluidTag(stack, false));
    }

    public static MutableFluidContainer unpackFluid(ItemVariant item) {
        ItemStack stack = unpackStack(item);
        return FluidContainer.of(stack).toMutable(stack);
    }

    public static FluidVariant packFluid(MutableFluidContainer contents) {
        return FluidVariant.of(contents.getFluid().getPhysical().getFluid(), contents.getAttributes());
    }

    public static MutableFluidContainer unpackFluid(ItemVariant container, FluidVariant contents, long level) {
        return FluidContainer.of(container.getItem())
            .toMutable(container.getItem().getDefaultStack())
            .withFluid(SimpleFluid.forVanilla(contents.getFluid()))
            .withLevel((int)level)
            .withAttributes(contents.copyNbt());
    }

    public static Optional<ViewBasedFluidContainer> probeContents(ItemStack stack) {
        final ItemStack[] currentStack = { stack.copy() };
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, new ContainerItemContext() {
            @Override
            public SingleSlotStorage<ItemVariant> getMainSlot() {
                return new SingleVariantStorage<>() {
                    @Override
                    protected ItemVariant getBlankVariant() {
                        return ItemVariant.blank();
                    }

                    @Override
                    protected long getCapacity(ItemVariant variant) {
                        return Long.MAX_VALUE;
                    }
                };
            }

            @Override
            public long exchange(ItemVariant newVariant, long maxAmount, TransactionContext transaction) {
                currentStack[0] = unpackStack(newVariant);
                return 1;
            }

            @Override
            public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
                return 0;
            }

            @Override
            public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
                return List.of();
            }
        });
        if (storage == null) {
            return Optional.empty();
        }
        StorageView<FluidVariant> view = storage.exactView(FluidVariant.blank());

        return Optional.of(new ViewBasedFluidContainer(currentStack, storage, view));
    }

    public static class ViewBasedFluidContainer extends MutableFluidContainer implements FluidContainer {
        private ItemStack[] currentStack;
        private Storage<FluidVariant> storage;
        private StorageView<FluidVariant> view;

        ViewBasedFluidContainer(ItemStack[] currentStack, Storage<FluidVariant> storage, StorageView<FluidVariant> view) {
            super(FluidContainer.UNLIMITED, SimpleFluid.forVanilla(view.getResource().getFluid()), (int)view.getAmount(), view.getResource().copyNbt());
            this.storage = storage;
            this.view = view;
        }

        @Override
        public MutableFluidContainer toMutable(ItemStack stack) {
            return probeContents(stack).orElse(this);
        }

        @Override
        public MutableFluidContainer copy() {
            return probeContents(asStack()).map(i -> (MutableFluidContainer)i).orElseGet(() -> super.copy());
        }

        @Override
        public ItemStack asStack() {
            return currentStack[0].copy();
        }

        @Override
        public int getLevel() {
            return (int)view.getAmount();
        }

        @Override
        public int getCapacity() {
            return (int)view.getCapacity();
        }

        @Override
        public SimpleFluid getFluid() {
            return SimpleFluid.forVanilla(view.getResource().getFluid());
        }

        @Override
        public NbtCompound getAttributes() {
            return attributes;
        }

        @Override
        public MutableFluidContainer withAttributes(NbtCompound attributes) {
            this.attributes = attributes;
            return this;
        }

        @Override
        public MutableFluidContainer drain(int amount) {
            try (var transaction = Transaction.openOuter()) {
                view.extract(view.getResource(), amount, transaction);
                transaction.commit();
            }
            return this;
        }

        @Override
        public int deposit(int amount, SimpleFluid fluid) {
            try (var transaction = Transaction.openOuter()) {
                storage.insert(FluidVariant.of(fluid.getPhysical().getFluid()), amount, transaction);
                transaction.commit();
            }
            return 0;
        }

        @Override
        public Item asItem() {
            return currentStack[0].getItem();
        }

        @Override
        public int getMaxCapacity() {
            return Integer.MAX_VALUE;
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

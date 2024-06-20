package ivorius.psychedelicraft.fluid.container;

import java.util.Map;

import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.FilledBucketItem;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.PSComponents;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Interop layer with Fabric's "experimental" transfer api.
 */
public final class VariantMarshal {
    public static void bootstrap() {
        FluidStorage.GENERAL_COMBINED_PROVIDER.register(context -> {
            ItemStack stack = context.getItemVariant().toStack();
            if (stack.get(PSComponents.FLUIDS) != null) {
                // F***k the fabric apis. Use something that actually works
                return new ItemFluidsStorage(context);
            }
            if (stack.isOf(Items.MILK_BUCKET)) {
                return new FullItemFluidStorage(context, Items.BUCKET, FluidVariant.of(PSFluids.MILK.getPhysical().getStandingFluid()), FluidConstants.BUCKET);
            }
            return null;
        });
        FilledBucketItem.registerDispenserBehaviour(Items.MILK_BUCKET);
    }

    public static final class FabricTransaction implements ItemFluids.Transaction {
        private ItemStack stack;
        private ItemFluids fluids;

        public FabricTransaction(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int capacity() {
            return (int)FluidTransferUtils.getCapacity(stack);
        }

        @Override
        public ItemFluids fluids() {
            if (fluids == null) {
                fluids = FluidTransferUtils.getContents(stack).map(contents -> {
                    return ItemFluids.of(contents.getRight(), contents.getLeft().intValue());
                }).orElse(ItemFluids.EMPTY);
            }
            return fluids;
        }

        @Override
        public ItemFluids withdraw(int amount) {
            var result = FluidTransferUtils.extract(stack, amount);
            if (result.getRight().isEmpty() && fluids.amount() > 0) {
                // Fabric's transfer api doesn't support partial withdrawl!!!!
                // We have to simulate it now. F$*#$
                ItemFluids.Transaction t = new ItemFluids.DirectTransaction(stack, capacity(), fluids);
                ItemFluids removed = t.withdraw(amount);
                stack = t.toItemStack();
                fluids = t.fluids();
                return removed;
            }
            stack = result.getLeft();
            fluids = null;
            return result.getRight().map(fluid -> {
                return ItemFluids.of(fluid.getRight(), fluid.getLeft().intValue());
            }).orElse(ItemFluids.EMPTY);
        }

        @Override
        public ItemFluids deposit(ItemFluids fluids, int maxAmount) {
            maxAmount = Math.min(maxAmount, fluids.amount());
            var result = FluidTransferUtils.deposit(stack, fluids.toVariant(), maxAmount);
            if (fluids().amount() < capacity() && result.getRight() == maxAmount) {
                // Fabric's transfer api doesn't support partial deposits!!!!
                // We have to simulate it now. F$*#$
                ItemFluids.Transaction t = new ItemFluids.DirectTransaction(stack, capacity(), this.fluids);
                ItemFluids remainder = t.deposit(fluids, maxAmount);
                stack = t.toItemStack();
                fluids = t.fluids();
                return remainder;
            }
            stack = result.getLeft();
            this.fluids = null;
            maxAmount -= result.getRight().intValue();
            return fluids.ofAmount(fluids.amount() - maxAmount);
        }

        @Override
        public ItemStack toItemStack() {
            return stack;
        }
    }

    interface FabricResovoir extends SingleSlotStorage<FluidVariant> {

        ItemFluids getContents();

        @Override
        default boolean isResourceBlank() {
            return getContents().isEmpty();
        }

        @Override
        default FluidVariant getResource() {
            return getContents().toVariant();
        }

        @Override
        default long getAmount() {
            return getContents().amount();
        }
    }

    public static final class ItemFluidsStorage implements FabricResovoir {
        private final ContainerItemContext context;

        public ItemFluidsStorage(ContainerItemContext context) {
            this.context = context;
        }

        @Override
        public ItemFluids getContents() {
            return ItemFluids.direct(getCurrentStack());
        }

        private ItemStack getCurrentStack() {
            return context.getItemVariant().toStack();
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            ItemFluids inputFluids = resource.getComponents().get(PSComponents.FLUIDS).orElse(null);
            if (inputFluids == null) {
                SimpleFluid fluid = SimpleFluid.forVanilla(resource.getFluid());
                inputFluids = ItemFluids.create(fluid, (int)maxAmount, Map.of());
            } else {
                inputFluids = inputFluids.ofAmount((int)maxAmount);
            }

            ItemFluids.Transaction t = new ItemFluids.DirectTransaction(getCurrentStack(), (int)getCapacity(), ItemFluids.direct(getCurrentStack()));

            inputFluids = t.deposit(inputFluids);

            if (context.exchange(ItemVariant.of(t.toItemStack()), 1, transaction) == 1) {
                return t.capacity();
            }
            return maxAmount - inputFluids.amount();
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            ItemFluids.Transaction t = new ItemFluids.DirectTransaction(getCurrentStack(), (int)getCapacity(), ItemFluids.direct(getCurrentStack()));
            ItemFluids withdrawn = t.withdraw((int)maxAmount);
            // What even is this for??
            /*if (context.exchange(ItemVariant.of(t.toItemStack()), 1, transaction) == 1) {
                return t.capacity();
            }*/
            return withdrawn.amount();
        }

        @Override
        public long getCapacity() {
            FluidCapacity capacity = getCurrentStack().get(PSComponents.FLUID_CAPACITY);
            return capacity == null ? 0 : capacity.capacity();
        }
    }
}

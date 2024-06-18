package ivorius.psychedelicraft.fluid.container;

import java.util.Map;

import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.PSComponents;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.ItemStack;

/**
 * Interop layer with Fabric's "experimental" transfer api.
 */
public final class VariantMarshal {
    public static void bootstrap() {
        FluidStorage.GENERAL_COMBINED_PROVIDER.register(context -> {
            ItemStack stack = context.getItemVariant().toStack();
            if (stack.get(PSComponents.FLUID_CAPACITY) != null) {
                return new ItemFluidsStorage(context);
            }
            return null;
        });
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
            stack = result.getLeft();
            fluids = null;
            return result.getRight().map(fluid -> {
                return ItemFluids.of(fluid.getRight(), fluid.getLeft().intValue());
            }).orElse(ItemFluids.EMPTY);
        }

        @Override
        public ItemFluids deposit(ItemFluids fluids, int maxAmount) {
            var result = FluidTransferUtils.deposit(stack, fluids.toVariant(), Math.min(maxAmount, fluids.amount()));
            stack = result.getLeft();
            this.fluids = null;
            return fluids.ofAmount(result.getRight().intValue());
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
            if (context.exchange(ItemVariant.of(t.toItemStack()), 1, transaction) == 1) {
                return t.capacity();
            }
            return withdrawn.amount();
        }

        @Override
        public long getCapacity() {
            FluidCapacity capacity = getCurrentStack().get(PSComponents.FLUID_CAPACITY);
            return capacity == null ? 0 : capacity.capacity();
        }
    }
}

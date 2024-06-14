package ivorius.psychedelicraft.fluid.container;

import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.MathHelper;

/**
 * @author Sollace
 * @since 3 Jan 2023
 */
public class Resovoir implements NbtSerialisable, VariantMarshal.FabricResovoir {
    private ItemFluids fluids = ItemFluids.EMPTY;
    private final int capacity;

    private final ChangeListener changeCallback;

    public Resovoir(int capacity, ChangeListener changeCallback) {
        this.capacity = capacity;
        this.changeCallback = changeCallback;
    }

    @Override
    public ItemFluids getContents() {
        return fluids;
    }

    public void setContents(ItemFluids fluids) {
        int amount = this.fluids.amount();
        this.fluids = fluids.ofAmount(Math.min(fluids.amount(), capacity));
        changeCallback.onLevelChange(this, this.fluids.amount() - amount);
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    public ItemStack deposit(ItemStack stack) {
        ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack);
        deposit(t, t.capacity());
        return t.toItemStack();
    }

    public int deposit(ItemFluids stack) {
        if (fluids.amount() >= capacity || stack.isEmpty() || !fluids.canCombine(stack)) {
            return 0;
        }

        int transferred = MathHelper.clamp(Math.max(0, capacity - fluids.amount()), 0, stack.amount());
        if (transferred > 0) {
            fluids = stack.ofAmount(fluids.amount() + transferred);
            changeCallback.onLevelChange(this, transferred);
        }

        return transferred;
    }

    public ItemFluids drain(int maxAmount) {
        ItemFluids extracted = fluids.ofAmount(Math.min(maxAmount, fluids.amount()));
        if (extracted.amount() > 0) {
            fluids = fluids.ofAmount(fluids.amount() - extracted.amount());
            changeCallback.onLevelChange(this, extracted.amount());
        }

        return extracted;
    }

    public int deposit(ItemFluids.Transaction from, int maxAmount) {
        if (fluids.amount() >= capacity || from.fluids().isEmpty() || !fluids.canCombine(from.fluids())) {
            return 0;
        }

        ItemFluids transferred = from.withdraw(MathHelper.clamp(Math.max(0, capacity - fluids.amount()), 0, maxAmount));
        fluids = transferred.ofAmount(fluids.amount() + transferred.amount());
        if (transferred.amount() > 0) {
            changeCallback.onLevelChange(this, transferred.amount());
        }

        return transferred.amount();
    }

    public int withdraw(ItemFluids.Transaction output, int maxAmount) {
        if (fluids.isEmpty() || output.fluids().amount() >= output.capacity() || !fluids.canCombine(output.fluids())) {
            return 0;
        }
        int initialAmount = fluids.amount();
        fluids = output.deposit(fluids, maxAmount);
        int transferred = initialAmount - fluids.amount();
        if (transferred > 0) {
            changeCallback.onLevelChange(this, -transferred);
        }
        return transferred;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        ItemFluids fluids = ItemFluids.of(resource, (int)maxAmount);
        if (resource.isBlank() || !fluids.canCombine(this.fluids)) {
            return 0;
        }

        int accepted = (int)Math.min(fluids.amount() - capacity, maxAmount);
        transaction.addCloseCallback((sender, result) -> {
            if (result.wasCommitted()) {
                this.fluids = fluids.ofAmount(this.fluids.amount() + accepted);
                changeCallback.onLevelChange(this, accepted);
            }
        });
        return accepted;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (maxAmount <= 0 || this.fluids.isEmpty()) {
            return 0;
        }
        ItemFluids fluids = ItemFluids.of(resource, (int)maxAmount);
        if (!fluids.canCombine(this.fluids)) {
            return 0;
        }
        int provided = (int)Math.min(maxAmount, this.fluids.amount());
        transaction.addCloseCallback((sender, result) -> {
            if (result.wasCommitted()) {
                this.fluids = this.fluids.ofAmount(this.fluids.amount() - provided);
                changeCallback.onLevelChange(this, -provided);
            }
        });
        return provided;
    }

    public void clear() {
        int amount = fluids.amount();
        fluids = ItemFluids.EMPTY;
        if (amount > 0) {
            changeCallback.onLevelChange(this, -amount);
        }
    }


    @Override
    public void toNbt(NbtCompound compound, WrapperLookup lookup) {
        compound.put("fluid", ItemFluids.CODEC.encodeStart(NbtOps.INSTANCE, fluids).getOrThrow());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void fromNbt(NbtCompound compound, WrapperLookup lookup) {
        if (compound.contains("stack", NbtElement.COMPOUND_TYPE)) {
            ItemStack stack = ItemStack.fromNbtOrEmpty(lookup, compound.getCompound("stack"));
            fluids = ItemFluids.fromCustom(stack);
        } else {
            fluids = ItemFluids.CODEC.decode(NbtOps.INSTANCE, compound.get("fluid")).result().map(pair -> pair.getFirst()).orElse(ItemFluids.EMPTY);
        }
    }

    public interface ChangeListener {
        void onLevelChange(Resovoir resovoir, int change);
    }

}

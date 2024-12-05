package ivorius.psychedelicraft.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.item.component.ItemFluids;

public class FluidMound {
    public static final Codec<FluidMound> CODEC = ItemFluids.CODEC.listOf().xmap(FluidMound::new, FluidMound::getFluids);

    private final List<ItemFluids> fluids = new ArrayList<>();

    public FluidMound(FluidMound fluids) {
        this(fluids.getFluids());
    }

    public FluidMound(Processable.Context context) {
        this(context.getAuxiliaryTanks().stream().map(tank -> tank.getContents()).toList());
    }

    public FluidMound(List<ItemFluids> fluids) {
        fluids.forEach(this::add);
    }

    public FluidMound() {}

    public List<ItemFluids> getFluids() {
        return fluids;
    }

    public ItemFluids get(int index) {
        return fluids.get(index);
    }

    public FluidMound addAll(FluidMound fluids) {
        fluids.fluids.forEach(this::add);
        return this;
    }

    public void add(ItemFluids fluids) {
        for (int i = 0; i < size(); i++) {
            ItemFluids into = get(i);
            if (into.canCombine(fluids)) {
                this.fluids.set(i, into.ofAmount(into.amount() + fluids.amount()));
                return;
            }
        }
        this.fluids.add(fluids);
    }

    public int remove(ItemFluids fluids) {
        try {
            int amountRemoved = 0;
            for (int i = 0; i < size(); i++) {
                ItemFluids into = get(i);
                if (into.canCombine(fluids)) {
                    int amountToRemove = Math.min(into.amount(), fluids.amount());
                    this.fluids.set(i, into.ofAmount(into.amount() - amountToRemove));
                    amountRemoved += amountToRemove;
                    if (amountRemoved >= fluids.amount()) {
                        break;
                    }
                }
            }

            return amountRemoved;
        } finally {
            this.fluids.removeIf(ItemFluids::isEmpty);
        }
    }

    public int size() {
        return fluids.size();
    }

    public boolean isEmpty() {
        return fluids.isEmpty();
    }

    public boolean removeMatch(FluidIngredient ingredient) {
        try {
            int amountRemoved = 0;
            for (int i = 0; i < fluids.size(); i++) {
                ItemFluids fluid = fluids.get(i);
                if (ingredient.test(fluid)) {
                    int amountToConsume = ingredient.level().orElse(fluid.amount());
                    int amountConsumed = Math.min(fluid.amount(), amountToConsume);
                    fluids.set(i, fluid.ofAmount(fluid.amount() - amountConsumed));
                    amountRemoved += amountConsumed;
                    if (amountRemoved >= amountToConsume) {
                        return true;
                    }
                }
            }

            return false;
        } finally {
            fluids.removeIf(ItemFluids::isEmpty);
        }
    }

    public FluidMound split(Predicate<ItemFluids> predicate) {
        FluidMound removed = new FluidMound(new ArrayList<>());
        this.fluids.removeIf(fluid -> {
            if (predicate.test(fluid)) {
                removed.add(fluid);
                return true;
            }
            return false;
        });
        return removed;
    }
}

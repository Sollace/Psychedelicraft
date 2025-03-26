package ivorius.psychedelicraft.recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;

public class FluidMound implements Iterable<ItemFluids> {
    public static final Codec<FluidMound> CODEC = ItemFluids.CODEC.listOf().xmap(FluidMound::of, FluidMound::getFluids);

    public static FluidMound of() {
        return new FluidMound();
    }

    public static FluidMound of(ItemFluids...fluids) {
        return of(List.of(fluids));
    }

    public static FluidMound of(Iterable<ItemFluids> fluids) {
        return of().addAll(fluids);
    }

    public static FluidMound of(Processable.Context context) {
        return of(context.getAuxiliaryTanks().stream().map(tank -> tank.getContents()).toList());
    }

    private final List<ItemFluids> fluids = new ArrayList<>();

    private FluidMound() {}

    public List<ItemFluids> getFluids() {
        return fluids;
    }

    public ItemFluids get(int index) {
        return fluids.get(index);
    }

    public FluidMound addAll(Iterable<ItemFluids> fluids) {
        fluids.forEach(this::add);
        return this;
    }

    public int getAmount(ItemFluids fluids) {
        for (int i = 0; i < size(); i++) {
            ItemFluids into = get(i);
            if (into.canCombine(fluids)) {
                return into.amount();
            }
        }
        return 0;
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

    public int totalSize() {
        return fluids.stream().mapToInt(ItemFluids::amount).sum();
    }

    public boolean isEmpty() {
        return fluids.isEmpty();
    }

    public int removeMatch(FluidIngredient ingredient) {
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
                        return amountRemoved;
                    }
                }
            }

            return 0;
        } finally {
            fluids.removeIf(ItemFluids::isEmpty);
        }
    }

    public FluidMound split(Predicate<ItemFluids> predicate) {
        FluidMound removed = FluidMound.of();
        this.fluids.removeIf(fluid -> {
            if (predicate.test(fluid)) {
                removed.add(fluid);
                return true;
            }
            return false;
        });
        return removed;
    }

    @Override
    public Iterator<ItemFluids> iterator() {
        return this.fluids.iterator();
    }
}

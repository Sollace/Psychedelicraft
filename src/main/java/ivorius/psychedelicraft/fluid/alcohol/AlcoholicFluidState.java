package ivorius.psychedelicraft.fluid.alcohol;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import ivorius.psychedelicraft.fluid.AlcoholicFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;

public record AlcoholicFluidState (int distillation, int maturation, int fermentation, boolean vinegar) implements Function<ItemFluids, ItemFluids> {
    private static final Interner<AlcoholicFluidState> INTERNER = Interners.newStrongInterner();
    public static final AlcoholicFluidState EMPTY = INTERNER.intern(new AlcoholicFluidState(0, 0, 0, false));
    public static final AlcoholicFluidState VINEGAR = INTERNER.intern(new AlcoholicFluidState(0, 0, 0, true));

    @Override
    public ItemFluids apply(ItemFluids stack) {
        var attributes = new HashMap<>(stack.attributes());
        apply(attributes);
        return stack.withAttributes(attributes);
    }

    public void apply(Map<String, Integer> attributes) {
        AlcoholicFluid.DISTILLATION.set(attributes, distillation);
        AlcoholicFluid.MATURATION.set(attributes, maturation);
        AlcoholicFluid.FERMENTATION.set(attributes, fermentation);
        AlcoholicFluid.VINEGAR.set(attributes, vinegar);
    }

    public boolean isDefault() {
        return distillation == 0 && maturation == 0 && fermentation == 0 && !vinegar;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int distillation;
        private int maturation;
        private int fermentation;
        private boolean vinegar;

        public AlcoholicFluidState.Builder distillation(int distillation) {
            this.distillation = distillation;
            return this;
        }

        public AlcoholicFluidState.Builder maturation(int maturation) {
            this.maturation = maturation;
            return this;
        }

        public AlcoholicFluidState.Builder fermentation(int fermentation) {
            this.fermentation = fermentation;
            return this;
        }

        public AlcoholicFluidState.Builder vinegar() {
            this.vinegar = true;
            return this;
        }

        public AlcoholicFluidState build() {
            return INTERNER.intern(new AlcoholicFluidState(distillation, maturation, fermentation, vinegar));
        }
    }
}
package ivorius.psychedelicraft.fluid.alcohol;

import java.util.function.Predicate;

import ivorius.psychedelicraft.fluid.AlcoholicFluid;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange.IntRange;

public record StatePredicate (
        IntRange fermentationRange,
        IntRange maturationRange,
        IntRange distillationRange,
        TriState vinegar
) implements Predicate<ItemStack> {
    public static final StatePredicate ANY_DISTILLED = StatePredicate.builder().distilled().build();
    public static final StatePredicate ANY_FERMENTED = StatePredicate.builder().fermented().build();
    public static final StatePredicate ANY_VINEGAR = StatePredicate.builder().vinegar().build();

    public interface Standard {
        StatePredicate ANY = StatePredicate.builder().vinegar(TriState.DEFAULT).build();
        StatePredicate BASE = StatePredicate.builder().undistilled().unfermented().unmatured().build();
        StatePredicate VINEGAR = StatePredicate.builder().vinegar().build();
        StatePredicate DISTILLED = StatePredicate.builder().distilled().build();
        StatePredicate MATURED = StatePredicate.builder().matured().build();
        StatePredicate FERMENTED_1 = StatePredicate.builder().fermentation(IntRange.exactly(1)).build();
        StatePredicate FERMENTED_2 = StatePredicate.builder().fermentation(IntRange.atLeast(2)).build();
    }

    @Override
    public boolean test(ItemStack stack) {
        return test(
                AlcoholicFluid.FERMENTATION.get(stack),
                AlcoholicFluid.DISTILLATION.get(stack),
                AlcoholicFluid.MATURATION.get(stack),
                AlcoholicFluid.VINEGAR.get(stack)
        );
    }

    public boolean test(int fermentation, int distillation, int maturation, boolean vinegar) {
        return fermentationRange.test(fermentation)
                && distillationRange.test(distillation)
                && maturationRange.test(maturation)
                && this.vinegar.orElse(vinegar) == vinegar;
    }

    public static StatePredicate.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private IntRange fermentationRange = IntRange.ANY;
        private IntRange maturationRange = IntRange.ANY;
        private IntRange distillationRange = IntRange.ANY;
        private TriState vinegar = TriState.FALSE;

        public StatePredicate.Builder vinegar() {
            return vinegar(TriState.TRUE);
        }

        public StatePredicate.Builder vinegar(TriState vinegar) {
            this.vinegar = vinegar;
            return this;
        }

        public StatePredicate.Builder fermentation(IntRange range) {
            fermentationRange = range;
            return this;
        }

        public StatePredicate.Builder fermented() {
            return fermentation(IntRange.atLeast(1));
        }

        public StatePredicate.Builder unfermented() {
            return fermentation(IntRange.exactly(0));
        }

        public StatePredicate.Builder maturation(IntRange range) {
            maturationRange = range;
            return this;
        }

        public StatePredicate.Builder matured() {
            return maturation(IntRange.atLeast(1));
        }

        public StatePredicate.Builder unmatured() {
            return maturation(IntRange.exactly(0));
        }

        public StatePredicate.Builder distillation(IntRange range) {
            distillationRange = range;
            return this;
        }

        public StatePredicate.Builder distilled() {
            return distillation(IntRange.atLeast(1));
        }

        public StatePredicate.Builder undistilled() {
            return distillation(IntRange.exactly(0));
        }

        public StatePredicate build() {
            return new StatePredicate(fermentationRange, maturationRange, distillationRange, vinegar);
        }
    }
}
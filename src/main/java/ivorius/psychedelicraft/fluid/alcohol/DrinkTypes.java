package ivorius.psychedelicraft.fluid.alcohol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.AbstractIterator;

import ivorius.psychedelicraft.fluid.AlcoholicFluid;
import ivorius.psychedelicraft.item.component.ItemFluids;

/**
 * @author Sollace
 * @since 2 Jan 2023
 */
public interface DrinkTypes {
    static DrinkTypes empty() {
        return List::of;
    }

    List<Entry> variants();

    default DrinkType find(ItemFluids fluids) {

        if (!variants().isEmpty()) {
            for (Entry variant : variants()) {
                if (variant.predicate.test(fluids)) {
                    return variant.value();
                }
            }
        }

        throw noMatch(fluids);
    }

    default Stream<State> streamStates() {
        return StreamSupport.stream(generateStateTable().spliterator(), false);
    }

    default Iterable<State> generateStateTable() {
        final int max = 2 * 3 * 16 * 16;
        Set<String> previouslyReturned = new HashSet<>();

        return () -> {
            return new AbstractIterator<>() {
                {
                    findMatch(17, 17, 3, false);
                }

                int index;
                @Nullable
                @Override
                protected State computeNext() {
                    if (variants().isEmpty()) {
                        endOfData();
                        return null;
                    }

                    while (true) {
                        int i = index;
                        index++;

                        boolean vinegar = i % 2 == 1;
                        i /= 2;
                        int distillation = i % 17;
                        i /= 16;
                        int maturation = i % 17;
                        i /= 16;
                        int fermentation = i % 3;

                        if (i > max) {
                            endOfData();
                            return null;
                        }

                        Entry match = findMatch(distillation, maturation, fermentation, vinegar);

                        if (previouslyReturned.add(match.value().getUniqueKey())) {
                            return new State(distillation, maturation, fermentation, vinegar, match);
                        }
                    }
                }

                private Entry findMatch(int distillation, int maturation, int fermentation, boolean vinegar) {
                    for (Entry variant : variants()) {
                        if (variant.predicate.test(fermentation, distillation, maturation, vinegar)) {
                            return variant;
                        }
                    }
                    throw noMatch(distillation, maturation, fermentation, vinegar);
                }
            };
        };
    }


    private static NullPointerException noMatch(ItemFluids stack) {
        return noMatch(
                AlcoholicFluid.DISTILLATION.get(stack),
                AlcoholicFluid.MATURATION.get(stack),
                AlcoholicFluid.FERMENTATION.get(stack),
                AlcoholicFluid.VINEGAR.get(stack)
        );
    }

    private static NullPointerException noMatch(int distillation, int maturation, int fermentation, boolean vinegar) {
        return new NullPointerException("No drink type specified for state { vinegar: " + vinegar + ", dist: " + distillation + ", ferm: " + fermentation + ", mat: " + maturation + " }");
    }

    static record Builder(List<Entry> variants) implements DrinkTypes {

        public Builder add(DrinkType variant, StatePredicate.Builder builder) {
            return add(variant, builder.build());
        }

        public Builder add(DrinkType variant, StatePredicate predicate) {
            variants.add(new Entry(variant, predicate));
            return this;
        }
    }

    static Builder builder() {
        return new Builder(new ArrayList<>());
    }

    record Entry (DrinkType value, StatePredicate predicate) { }

    record State (int distillation, int maturation, int fermentation, boolean vinegar, Entry entry)
        implements Function<ItemFluids, ItemFluids> {
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
    }
}

package ivorius.psychedelicraft.fluid.alcohol;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.AbstractIterator;

import ivorius.psychedelicraft.fluid.AlcoholicFluid;

/**
 * @author Sollace
 * @since 2 Jan 2023
 */
public interface DrinkTypes {
    static DrinkTypes empty() {
        return List::of;
    }

    List<Entry> variants();

    @Nullable
    default DrinkType find(ItemStack stack) {

        if (!variants().isEmpty()) {
            for (Entry variant : variants()) {
                if (variant.predicate.test(stack)) {
                    return variant.value();
                }
            }
        }

        return null;
    }

    default Stream<State> streamStates() {
        return StreamSupport.stream(generateStateTable().spliterator(), false);
    }

    default Iterable<State> generateStateTable() {
        final int max = 2 * 3 * 16 * 16;
        Set<String> previouslyReturned = new HashSet<>();
        return () -> {
            return new AbstractIterator<>() {
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
                        if (match != null && previouslyReturned.add(match.value().getUniqueKey())) {
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
                    return null;
                }
            };
        };
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

    record State (int distillation, int maturation, int fermentation, boolean vinegar, Entry entry) {
        public ItemStack apply(ItemStack stack) {
            stack = AlcoholicFluid.DISTILLATION.set(stack, distillation);
            stack = AlcoholicFluid.MATURATION.set(stack, maturation);
            stack = AlcoholicFluid.FERMENTATION.set(stack, fermentation);
            stack = AlcoholicFluid.VINEGAR.set(stack, vinegar);
            return stack;
        }

        public boolean isDefault() {
            return distillation == 0 && maturation == 0 && fermentation == 0 && !vinegar;
        }
    }
}

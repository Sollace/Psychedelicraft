package ivorius.psychedelicraft.fluid.alcohol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.util.math.MathHelper;

/**
 * @author Sollace
 * @since 2 Jan 2023
 */
public interface DrinkTypes {
    DrinkTypes EMPTY = new Builder(List.of(), Set.of());
    static DrinkTypes empty() {
        return EMPTY;
    }

    List<Variant> variants();

    default DrinkType find(ItemFluids fluids) {

        if (!variants().isEmpty()) {
            for (Variant variant : variants()) {
                if (variant.predicate.test(fluids)) {
                    return variant.value();
                }
            }
        }

        return variants().get(0).value();
    }

    default int findId(ItemFluids fluids) {
        for (int i = 0; i < variants().size(); i++) {
            if (variants().get(i).predicate().test(fluids)) {
                return i;
            }
        }

        return 0;
    }

    default AlcoholicFluidState findState(int id) {
        return variants().get(MathHelper.clamp(id, 0, variants().size())).predicate().state();
    }

    static record Builder(List<Variant> variants, Set<AlcoholicFluidState> states) implements DrinkTypes {

        public Builder add(DrinkType variant, StatePredicate.Builder builder) {
            return add(variant, builder.build());
        }

        public Builder add(DrinkType variant, StatePredicate predicate) {
            variants.add(new Variant(variant, predicate));
            Preconditions.checkArgument(states.add(predicate.state()), "Drink type overlap for " + variant + " and " + predicate);
            return this;
        }

        public Builder vinegar(DrinkType type) {
            return add(type, StatePredicate.VINEGAR);
        }

        public Builder firstFerment(DrinkType type) {
            return add(type, StatePredicate.FERMENTED_1);
        }

        public Builder secondFerment(DrinkType type) {
            return add(type, StatePredicate.FERMENTED_2);
        }

        public Builder distilled(DrinkType type) {
            return add(type, StatePredicate.FERMENTED_DISTILLED);
        }

        public Builder matured(DrinkType type) {
            return add(type, StatePredicate.FERMENTED_MATURED);
        }

        public Builder matureDistilled(DrinkType type) {
            return add(type, StatePredicate.FERMENTED_MATURED_DISTILLED);
        }
    }

    static Builder builder(DrinkType baseForm) {
        return new Builder(new ArrayList<>(), new HashSet<>()).add(baseForm, StatePredicate.BASE);
    }

    static Builder builder(String wort) {
        return builder(DrinkType.WORT.withVariation(wort));
    }

    static Builder builder(FluidAppearance wort) {
        return builder(DrinkType.WORT.withAppearance(wort));
    }

    record Variant (DrinkType value, StatePredicate predicate) { }

    record Entry(AlcoholicFluidState state, Variant variant) {}
}

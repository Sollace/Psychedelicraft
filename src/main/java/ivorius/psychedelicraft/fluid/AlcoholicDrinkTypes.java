package ivorius.psychedelicraft.fluid;

import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange.IntRange;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

/**
 * @author Sollace
 * @since 2 Jan 2023
 */
public record AlcoholicDrinkTypes (List<Entry<String>> names, List<Entry<Icons>> alcIcons) {
    static final AlcoholicDrinkTypes BEER = new AlcoholicDrinkTypes.Builder()
            .addName("beer", IntRange.ANY, IntRange.ANY)
                .addIcon(IntRange.ANY, IntRange.atMost(3), IntRange.atLeast(2), "clear_still", "clear_flow")
                .addIcon(IntRange.ANY, IntRange.between(4, 13), IntRange.ANY, "rum_semi_mature_still", "rum_semi_mature_flow")
                .addIcon(IntRange.ANY, IntRange.atLeast(14), IntRange.ANY, "rum_mature_still", "rum_mature_flow")
            .build();
    static final AlcoholicDrinkTypes RUM = new AlcoholicDrinkTypes.Builder()
            .addName("rum", IntRange.ANY, IntRange.ANY)
                .addIcon(IntRange.ANY, IntRange.atMost(3), IntRange.atLeast(2), "clear_still", "clear_flow")
                .addIcon(IntRange.ANY, IntRange.between(4, 13), IntRange.ANY, "rum_semi_mature_still", "rum_semi_mature_flow")
                .addIcon(IntRange.ANY, IntRange.atLeast(14), IntRange.ANY, "rum_mature_still", "rum_mature_flow")
            .build();
    static final AlcoholicDrinkTypes VODKA_WHISKEY = new AlcoholicDrinkTypes.Builder()
            .addName("vodka", IntRange.exactly(0), IntRange.atLeast(1))
            .addName("whisky", IntRange.atLeast(1), IntRange.atLeast(1))
                .addIcon(IntRange.ANY, IntRange.atMost(3), IntRange.atLeast(2), "clear_still", "clear_flow")
                .addIcon(IntRange.ANY, IntRange.between(4, 13), IntRange.ANY, "rum_semi_mature_still", "rum_semi_mature_flow")
                .addIcon(IntRange.ANY, IntRange.atLeast(14), IntRange.ANY, "rum_mature_still", "rum_mature_flow")
            .build();
    static final AlcoholicDrinkTypes BEER_VODKA_WHISKEY = new AlcoholicDrinkTypes.Builder()
            .addName("beer", IntRange.ANY, IntRange.exactly(0))
            .addName("vodka", IntRange.exactly(0), IntRange.atLeast(1))
            .addName("whisky", IntRange.atLeast(1), IntRange.atLeast(1))
                .addIcon(IntRange.ANY, IntRange.atLeast(3), IntRange.atLeast(2), "clear_still", "clear_flow")
                .addIcon(IntRange.ANY, IntRange.between(4, 13), IntRange.ANY, "rum_semi_mature_still", "rum_semi_mature_flow")
                .addIcon(IntRange.ANY, IntRange.atMost(14), IntRange.ANY, "rum_mature_still", "rum_mature_flow")
            .build();
    static final AlcoholicDrinkTypes JENEVER = new AlcoholicDrinkTypes.Builder()
            .addName("jenever", IntRange.ANY, IntRange.ANY)
                .addIcon(IntRange.ANY, IntRange.atMost(3), IntRange.atLeast(2), "clear_still", "clear_flow")
                .addIcon(IntRange.ANY, IntRange.between(4, 13), IntRange.ANY, "rum_semi_mature_still", "rum_semi_mature_flow")
                .addIcon(IntRange.ANY, IntRange.atLeast(14), IntRange.ANY, "rum_mature_still", "rum_mature_flow")
            .build();

    @Nullable
    public <T> Entry<T> getMatchedValue(ItemStack stack, AlcoholicFluid fluid, List<Entry<T>> values) {
        int fermentation = fluid.FERMENTATION.get(stack);
        int distillation = AlcoholicFluid.DISTILLATION.get(stack);
        int maturation = AlcoholicFluid.MATURATION.get(stack);

        for (Entry<T> alc : values) {
            if (alc.predicate.matches(fermentation, fluid.settings.fermentationSteps, distillation, maturation)) {
                return alc;
            }
        }

        return null;
    }

    @Nullable
    public Entry<String> getSpecialName(ItemStack stack, AlcoholicFluid fluid) {
        return getMatchedValue(stack, fluid, names);
    }

    @Nullable
    public Entry<Icons> getSpecialIcon(ItemStack stack, AlcoholicFluid fluid) {
        return getMatchedValue(stack, fluid, alcIcons);
    }

    public static class Builder {
        private final List<Entry<String>> names = new ArrayList<>();
        private final List<Entry<Icons>> alcIcons = new ArrayList<>();

        public Builder addName(String iconName, IntRange maturationRange, IntRange distillationRange) {
            names.add(new Entry<>(iconName, new StatePredicate(IntRange.ANY, maturationRange, distillationRange)));
            return this;
        }

        public Builder addIcon(IntRange fermentationRange, IntRange maturationRange, IntRange distillationRange, String stillIconName, String flowingIconName) {
            alcIcons.add(new Entry<>(new Icons(stillIconName, flowingIconName), new StatePredicate(fermentationRange, maturationRange, distillationRange)));
            return this;
        }

        public AlcoholicDrinkTypes build() {
            return new AlcoholicDrinkTypes(names, alcIcons);
        }
    }

    public record StatePredicate (
            IntRange fermentationRange,
            IntRange maturationRange,
            IntRange distillationRange
    ) {
        public boolean matches(int fermentation, int maxFermentation, int distillation, int maturation) {
            return fermentationRange.test(fermentation) && distillationRange.test(distillation) && maturationRange.test(maturation);
        }

        public static int getUnboxedMin(Integer min) {
            return min == null ? 0 : min.intValue();
        }
    }

    public record Icons (String still, String flowing) {}

    public record Entry<T> (T value, StatePredicate predicate) { }
}

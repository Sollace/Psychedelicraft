package ivorius.psychedelicraft.fluids;

import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange.IntRange;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sollace
 * @since 2 Jan 2023
 */
public record AlcoholicDrinkTypes (List<NamedAlcohol> names, List<AlcoholIcon> alcIcons) {
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

    public <M extends AlcoholMatcher> M getMatchedValue(ItemStack stack, AlcoholicFluid fluid, List<M> values) {
        int fermentation = fluid.getFermentation(stack);
        int distillation = fluid.getDistillation(stack);
        int maturation = fluid.getMaturation(stack);

        for (M alc : values) {
            if (alc.matches(fermentation, fluid.settings.fermentationSteps, distillation, maturation)) {
                return alc;
            }
        }

        return null;
    }

    public NamedAlcohol getSpecialName(ItemStack stack, AlcoholicFluid fluid) {
        return getMatchedValue(stack, fluid, names);
    }

    public AlcoholIcon getSpecialIcon(ItemStack stack, AlcoholicFluid fluid) {
        return getMatchedValue(stack, fluid, alcIcons);
    }

    public static class Builder {
        private final List<NamedAlcohol> names = new ArrayList<>();
        private final List<AlcoholIcon> alcIcons = new ArrayList<>();

        public Builder addName(String iconName, IntRange maturationRange, IntRange distillationRange) {
            names.add(new NamedAlcohol(iconName, maturationRange, distillationRange));
            return this;
        }

        public Builder addIcon(IntRange fermentationRange, IntRange maturationRange, IntRange distillationRange, String stillIconName, String flowingIconName) {
            alcIcons.add(new AlcoholIcon(fermentationRange, maturationRange, distillationRange, stillIconName, flowingIconName));
            return this;
        }

        public AlcoholicDrinkTypes build() {
            return new AlcoholicDrinkTypes(names, alcIcons);
        }
    }

    private static class AlcoholMatcher {
        public IntRange fermentationRange;
        public IntRange maturationRange;
        public IntRange distillationRange;

        public AlcoholMatcher(IntRange fermentationRange, IntRange maturationRange, IntRange distillationRange) {
            this.fermentationRange = fermentationRange;
            this.maturationRange = maturationRange;
            this.distillationRange = distillationRange;
        }

        public boolean matches(int fermentation, int maxFermentation, int distillation, int maturation) {
            return (fermentationRange.getMin() < 0 ? fermentation >= maxFermentation : rangeContains(fermentationRange, fermentation))
                    && rangeContains(distillationRange, distillation)
                    && rangeContains(maturationRange, maturation);
        }

        private static boolean rangeContains(IntRange range, int value) {
            return value >= range.getMin() && (range.getMax() < 0 || value <= range.getMax());
        }
    }

    public static class NamedAlcohol extends AlcoholMatcher {
        public String iconName;

        public NamedAlcohol(String iconName, IntRange maturationRange, IntRange distillationRange) {
            super(IntRange.between(-1, -1), maturationRange, distillationRange);
            this.iconName = iconName;
        }
    }

    public static class AlcoholIcon extends AlcoholMatcher {
        public String stillIconName;

        public String flowingIconName;

        public AlcoholIcon(IntRange fermentationRange, IntRange maturationRange, IntRange distillationRange, String stillIconName, String flowingIconName) {
            super(fermentationRange, maturationRange, distillationRange);
            this.stillIconName = stillIconName;
            this.flowingIconName = flowingIconName;
        }
    }
}

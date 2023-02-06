package ivorius.psychedelicraft.fluid;

import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

/**
 * @author Sollace
 * @since 2 Jan 2023
 */
public interface DrinkTypes {
    VariantSet<Icons> MATURABLE_ICONS = new IconSet()
            .add(IntRange.ANY, IntRange.atMost(3), IntRange.atLeast(2), "clear")
            .add(IntRange.ANY, IntRange.between(4, 13), IntRange.ANY, "rum_semi_mature")
            .add(IntRange.ANY, IntRange.atLeast(14), IntRange.ANY, "rum_mature");
    VariantSet<Icons> CLEAR_ICONS = new IconSet()
            .add(IntRange.ANY, IntRange.ANY, IntRange.ANY, "clear");

    VariantSet<String> BEER = new NameSet().add("beer", IntRange.ANY, IntRange.ANY);
    VariantSet<String> RUM = new NameSet().add("rum", IntRange.ANY, IntRange.ANY);

    VariantSet<String> VODKA_WHISKEY = new NameSet()
            .add("vodka", IntRange.exactly(0), IntRange.atLeast(1))
            .add("whisky", IntRange.atLeast(1), IntRange.atLeast(1));

    VariantSet<String> BEER_VODKA_WHISKEY = new NameSet()
            .add("beer", IntRange.ANY, IntRange.exactly(0))
            .add("vodka", IntRange.exactly(0), IntRange.atLeast(1))
            .add("whisky", IntRange.atLeast(1), IntRange.atLeast(1));

    VariantSet<String> MEAD = new NameSet().add("mead", IntRange.ANY, IntRange.ANY);
    VariantSet<String> JENEVER = new NameSet().add("jenever", IntRange.ANY, IntRange.ANY);
    VariantSet<String> RED_WINE = new NameSet().add("red_wine", IntRange.ANY, IntRange.ANY);
    VariantSet<String> RICE_WINE = new NameSet().add("rice_wine", IntRange.ANY, IntRange.ANY);

    interface VariantSet<T> {
        VariantSet<?> EMPTY = List::of;

        @SuppressWarnings("unchecked")
        static <T> VariantSet<T> empty() {
            return (VariantSet<T>)EMPTY;
        }

        List<Entry<T>> variants();

        @Nullable
        default T find(ItemStack stack) {
            for (Entry<T> alc : variants()) {
                if (alc.predicate.test(stack)) {
                    return alc.value();
                }
            }

            return null;
        }
    }

    static class NameSet implements VariantSet<String> {
        private final List<Entry<String>> names = new ArrayList<>();

        public NameSet add(String iconName, IntRange maturationRange, IntRange distillationRange) {
            names.add(new Entry<>(iconName, new StatePredicate(IntRange.atLeast(1), maturationRange, distillationRange)));
            return this;
        }

        @Override
        public List<Entry<String>> variants() {
            return names;
        }
    }

    static class IconSet implements VariantSet<Icons> {
        private final List<Entry<Icons>> alcIcons = new ArrayList<>();

        public IconSet add(IntRange fermentationRange, IntRange maturationRange, IntRange distillationRange, String icon) {
            alcIcons.add(new Entry<>(new Icons(icon + "_still", icon + "_flow"), new StatePredicate(fermentationRange, maturationRange, distillationRange)));
            return this;
        }

        @Override
        public List<Entry<Icons>> variants() {
            return alcIcons;
        }
    }

    record StatePredicate (
            IntRange fermentationRange,
            IntRange maturationRange,
            IntRange distillationRange
    ) implements Predicate<ItemStack> {
        @Override
        public boolean test(ItemStack stack) {
            return fermentationRange.test(AlcoholicFluid.FERMENTATION.get(stack))
                && distillationRange.test(AlcoholicFluid.DISTILLATION.get(stack))
                && maturationRange.test(AlcoholicFluid.MATURATION.get(stack));
        }

        public void applyTo(ItemStack stack) {
            AlcoholicFluid.FERMENTATION.set(stack, midPoint(fermentationRange, 2));
            AlcoholicFluid.DISTILLATION.set(stack, midPoint(distillationRange, 16));
            AlcoholicFluid.MATURATION.set(stack, midPoint(maturationRange, 16));
        }

        private static int midPoint(IntRange range, int defMax) {
            int min = unbox(range.getMin(), 0);
            int max = unbox(range.getMax(), defMax);
            return (int)MathHelper.lerp(0.5, min, max);
        }

        private static int unbox(Integer value, int def) {
            return value == null ? def : value.intValue();
        }
    }

    public record Icons (String still, String flowing) {}

    public record Entry<T> (T value, StatePredicate predicate) { }
}

package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.*;
import java.util.stream.Stream;

import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.util.MathUtils;

/**
 * Created by lukas on 14.11.14.
 */
public class HallucinationTypes {
    public static final int ENTITIES = 0;
    public static final int DESATURATION = 1;
    public static final int SUPER_SATURATION = 2;
    public static final int SLOW_COLOR_ROTATION = 3;
    public static final int QUICK_COLOR_ROTATION = 4;
    public static final int BIG_WAVES = 5;
    public static final int SMALL_WAVES = 6;
    public static final int WIGGLE_WAVES = 7;
    public static final int PULSES = 8;
    public static final int SURFACE_FRACTALS = 9;
    public static final int DISTANT_WORLD_DEFORMATION = 10;
    public static final int SHATTERING_FRACTALS = 11;
    public static final int BLOOM = 101;
    public static final int COLOR_BLOOM = 102;
    public static final int COLOR_CONTRAST = 103;

    private static final List<Integer> COLOR = List.of(DESATURATION, SUPER_SATURATION, SLOW_COLOR_ROTATION, QUICK_COLOR_ROTATION, PULSES, SURFACE_FRACTALS, BLOOM, COLOR_BLOOM, COLOR_CONTRAST);
    private static final List<Integer> MOVEMENT = List.of(BIG_WAVES, SMALL_WAVES, WIGGLE_WAVES, DISTANT_WORLD_DEFORMATION, SHATTERING_FRACTALS);
    private static final List<Integer> CONTEXTUAL = List.of(ENTITIES);
    public static final List<Integer> ALL = Stream.of(COLOR, MOVEMENT, CONTEXTUAL).flatMap(List::stream).toList();

    private final List<Category> categories = List.of(
            new Category(COLOR, Drug.COLOR_HALLUCINATION_STRENGTH),
            new Category(MOVEMENT, Drug.MOVEMENT_HALLUCINATION_STRENGTH),
            new Category(CONTEXTUAL, Drug.CONTEXTUAL_HALLUCINATION_STRENGTH)
    );

    public float getTotal(DrugProperties properties) {
        float totalHallucinationValue = 0;
        for (Category type : categories) {
            type.currentValue = MathUtils.nearValue(type.currentValue, type.getDesiredValue(properties), 0.01F, 0.01F);
            totalHallucinationValue += type.currentValue;
        }
        return totalHallucinationValue;
    }

    public float getMultiplier(int hallucination) {
        float value = 1;
        for (Category c : categories) {
            if (c.hallucinations.contains(hallucination)) {
                value *= MathUtils.inverseLerp(c.currentValue, 0, 0.5F);
            }
        }
        return value;
    }

    private static class Category {
        public final List<Integer> hallucinations;
        public float currentValue;
        private final Drug.AggregateModifier modifier;

        public Category(List<Integer> hallucinations, Drug.AggregateModifier modifier) {
            this.hallucinations = new ArrayList<>(hallucinations);
            this.modifier = modifier;
        }

        public float getDesiredValue(DrugProperties properties) {
            return properties.getModifier(modifier);
        }
    }
}

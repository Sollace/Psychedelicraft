package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.*;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
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

    private static final IntList COLOR = IntList.of(DESATURATION, SUPER_SATURATION, SLOW_COLOR_ROTATION, QUICK_COLOR_ROTATION, PULSES, SURFACE_FRACTALS, BLOOM, COLOR_BLOOM, COLOR_CONTRAST);
    private static final IntList MOVEMENT = IntList.of(BIG_WAVES, SMALL_WAVES, WIGGLE_WAVES, DISTANT_WORLD_DEFORMATION, SHATTERING_FRACTALS);
    private static final IntList CONTEXTUAL = IntList.of(ENTITIES);
    public static final IntList ALL = IntList.of(Stream.of(COLOR, MOVEMENT, CONTEXTUAL).flatMapToInt(IntList::intStream).toArray());

    private static final List<Category> CATEGORIES = List.of(
            new Category(COLOR, Drug.COLOR_HALLUCINATION_STRENGTH),
            new Category(MOVEMENT, Drug.MOVEMENT_HALLUCINATION_STRENGTH),
            new Category(CONTEXTUAL, Drug.CONTEXTUAL_HALLUCINATION_STRENGTH)
    );

    private final Object2FloatMap<Category> values = new Object2FloatOpenHashMap<>();

    public float update(DrugProperties properties) {
        float totalHallucinationValue = 0;
        for (Category category : CATEGORIES) {
            totalHallucinationValue += values.computeFloat(category, (c, currentValue) -> {
                return MathUtils.nearValue(currentValue == null ? 0 : currentValue, c.getDesiredValue(properties), 0.01F, 0.01F);
            });
        }
        return totalHallucinationValue;
    }

    public float getMultiplier(int hallucination) {
        float value = 1;
        for (Category c : CATEGORIES) {
            if (c.hallucinations.contains(hallucination)) {
                value *= MathUtils.project(values.getFloat(c), 0, 0.5F);
            }
        }
        return value;
    }

    private record Category(IntList hallucinations, Attribute modifier) {
        public float getDesiredValue(DrugProperties properties) {
            return properties.getModifier(modifier);
        }
    }
}

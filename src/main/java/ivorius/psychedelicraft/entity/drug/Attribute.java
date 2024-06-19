package ivorius.psychedelicraft.entity.drug;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record Attribute(float initial, Combiner combiner) {
    public float get(DrugProperties properties) {
        return get(initial, properties);
    }

    public float get(float value, DrugProperties properties) {
        for (Drug drug : properties.getAllDrugs()) {
            value = combiner.combine(value, drug.get(this));
        }
        return value;
    }

    public static Function<DrugProperties, float[]> createColorModification(BiConsumer<Drug, float[]> modifier) {
        return properties -> {
            float[] initial = {1, 1, 1, 0};
            for (Drug drug : properties.getAllDrugs()) {
                modifier.accept(drug, initial);
            }
            return initial;
        };
    }

    public interface Combiner {
        Combiner MUL = (a, b) -> a * b;
        Combiner SUM = (a, b) -> a + b;
        Combiner INVERSE_MUL = (a, b) -> a + (1 - a) * b;

        float combine(float a, float b);
    }
}